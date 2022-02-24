package com.freewheelin.pulley.core.manage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.bases.isNetworkConnected
import com.freewheelin.pulley.bases.user

import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.Preferences
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_daebak.*
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

object VersionManager {
    enum class Required { NOT, MINOR, MAJOR }
    enum class CompareVersion { UP, DOWN, SAME } // 앱의 버전상태를 최신버전과 비교하여 알려준다

    var info: VersionInfo? = null
    val appVersionCode: Int = BuildConfig.VERSION_CODE
    val appVersion: String = BuildConfig.VERSION_NAME


    fun isNeedToUpdate(): Boolean? {
        if(info == null) {
            Log.e("VersionManager", "isNeedToUpdate() info is null")
            return null
        } else {
            return appVersionCode < info!!.versionCode
        }
    }

    fun isNeedToForceUpdate(storeVersion: Triple<Int, Int, Int>?): Required {
        Log.d("버전체크", "appVersion=${appVersionCode}, required=${info?.requiredMinVersion?:-1}, latest=${info?.versionCode}")
        val versionInfo = info ?: return Required.NOT

        if (storeVersion == null) {
            return if (appVersionCode < versionInfo.requiredMinVersion) {
                Required.MAJOR
            } else if (appVersionCode < versionInfo.versionCode) {
                Required.MINOR
            } else {
                Required.NOT
            }
        } else {
            // 현재 버전이 스토어 버전보다 낮고, 현재 버전이 s3JsonVersion에서 강제업데이트를 요구하는 버전보다 낮으면 MAJOR
            // 현재버전이 스토어 버전보다 낮으면 MINOR
            val parsedAppVersion = parseVersion(appVersion) ?: return Required.NOT
            val cv = compareVersion(parsedAppVersion, storeVersion)
            return when (cv) {
                CompareVersion.SAME -> Required.NOT
                else -> {
                    when {
                        appVersionCode < versionInfo.requiredMinVersion -> Required.MAJOR
                        appVersionCode < versionInfo.versionCode -> Required.MINOR
                        else -> Required.NOT
                    }
                }
            }
        }
    }

    // 앱의 버전상태를 최신버전과 비교하여 알려준다
    private fun compareVersion(appVersion: Triple<Int, Int, Int>, latestVersion: Triple<Int, Int, Int>): CompareVersion {
        if (appVersion.first == latestVersion.first && appVersion.second == latestVersion.second && appVersion.third == latestVersion.third) {
            return CompareVersion.SAME
        }
        if (appVersion.first < latestVersion.first) {
            return CompareVersion.DOWN
        }
        if (appVersion.first == latestVersion.first && appVersion.second < latestVersion.second) {
            return CompareVersion.DOWN
        }
        if (appVersion.first == latestVersion.first && appVersion.second == latestVersion.second && appVersion.third < latestVersion.third) {
            return CompareVersion.DOWN
        }
        // 약간 문제있는 상황 ex)  앱버전이 웹 주소에서 파싱해온 스토어의 버전보다 높은경우
        return CompareVersion.UP
    }

    fun requestVersionInfo(
            activity: Activity,
            cb: (update: Required, info: VersionInfo?) -> Unit) {
        thread {
            val version = requestAppStoreVersionInfo()
            println("버전, app store html version : ${version}")
            val storeVersion = parseVersion(version)
            getAndroidVersionInfoAtPulleyCommonS3(activity, storeVersion, cb)
        }

    }

    private fun getAndroidVersionInfoAtPulleyCommonS3(activity: Activity,
                                                      storeVersion: Triple<Int, Int, Int>?,
                                                      cb: (update: Required, info: VersionInfo?) -> Unit) {
        API_V2.getAndroidVersionInfo().enqueue(object : Callback<Template<VersionInfo>> {
            override fun onFailure(call: Call<Template<VersionInfo>>, t: Throwable) {
                val dialog = if (!activity.isNetworkConnected) {
                    DialogUtils.networkErrDialog(activity)
                } else {
                    DialogUtils.serverErrDialog(activity)
                }

                dialog.setCancelable(false)
                dialog.rightBtn.setOnClickListener {
                    activity.finishAndRemoveTask()
                }
                if(!activity.isFinishing) {
                    dialog.show()
                }
            }

            override fun onResponse(call: Call<Template<VersionInfo>>, response: Response<Template<VersionInfo>>) {
                val responseInfo = response.body()?.data

                when(response.code()) {
                    200 -> {
                        info = responseInfo
                        cb(isNeedToForceUpdate(storeVersion), info)
                    }
                    401 -> {
                        user?.token = ""
                        user?.commit("VersionManager")

                        cb(isNeedToForceUpdate(storeVersion), null)
                    }
                    else -> {
                        val dialog = if (!activity.isNetworkConnected) {
                            DialogUtils.networkErrDialog(activity)
                        } else {
                            DialogUtils.serverErrDialog(activity)
                        }

                        dialog.setCancelable(false)
                        dialog.rightBtn.setOnClickListener {
                            activity.finishAndRemoveTask()
                        }
                        dialog.show()
                    }
                }
            }
        })

    }

    private fun parseVersion(version: String?): Triple<Int, Int, Int>? {
        version?.let {
            val splitedStr = it.split(".")
            if (splitedStr.size == 3) {
                val releaseVersion = splitedStr[0].toInt()
                val majorVersion = splitedStr[1].toInt()
                val minorVersion = splitedStr[2].toInt()
                return Triple(releaseVersion, majorVersion, minorVersion)
            }
        }
        return null
    }

    fun requestAppStoreVersionInfo(): String? {
        val appStoreUrl = "https://play.google.com/store/apps/details?id=com.freewheelin.pulley&hl=en&gl=US"
        try {
            val doc = Jsoup.connect(appStoreUrl).get()
            val currentVersionDiv = doc.select(".BgcNfc")
            val currentVersion = doc.select("div.hAyfc div span.htlgb")
            for (index in currentVersionDiv.indices) {
                val text = currentVersionDiv[index].text()
                if (text.equals("Current Version")) {
                    return currentVersion[index].text()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

//    fun getVersionUpdateDialog(activity: SplashActivity, info: VersionInfo): Dialog? {
//        if(isNeedUpdateDialog(info, suspendVersionInfo )) {
//            val dialog = DialogUtils.makeDialog(activity, info.updateTitle, info.updateContent, "일주일간 보지 않기", "업데이트")
//            dialog.setCancelable(false)
//
//            if(info.requiredMinVersion > appVersionCode) {
//                dialog.leftBtn.text = "취소"
//                dialog.leftBtn.setOnClickListener {
//                    dialog.dismiss()
//                    activity.finishAndRemoveTask()
//                }
//            } else {
//                dialog.leftBtn.setOnClickListener {
//                    info.suspendStartDate = Date()
//                    dialog.dismiss()
//                    activity.moveActivity()
//                }
//            }
//
//            dialog.rightBtn.setOnClickListener {
//                dialog.dismiss()
//                val intent = getUpdateIntent()
//                activity.startActivity(intent)
//            }
//
//            return dialog
//        } else {
//            return null
//        }
//    }

    fun getUpdateIntent(): Intent? {
        val info = this.info ?: return null
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                    info.updateURL)
            setPackage("com.android.vending")
        }
        return intent
    }

    fun isNeedUpdateDialog(info: VersionInfo?, suspendInfo: VersionInfo?):Boolean {
        if(info == null || info.versionCode <= appVersionCode)
            return false
        else {
            return ((info.requiredMinVersion > appVersionCode)
                    || (suspendInfo == null)
                    || (suspendInfo.suspendStartDate == null)
                    || (suspendInfo.versionCode != info.versionCode)
                    || (DateTimeUtils.getDayDifferences(suspendInfo.suspendStartDate!!, Date()) >= 7))
        }
    }
}

class VersionInfo(
        val requiredMinVersion: Int,
        val versionCode: Int,
        val version: String,
        val updateURL: String,
        val updateTitle: String,
        val updateContent: String,
        val updateImageUrl: String
) {
    var suspendStartDate: Date? = null
        set(value) {
            field = value
            commit()
        }

    fun commit() {
        val gson = Gson()
        val json = gson.toJson(this)
        Preferences.versionDataString.set(json)
    }
}
