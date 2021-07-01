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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object VersionManager {
    enum class Required { NOT, MINOR, MAJOR}

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

    fun isNeedToForceUpdate(): Required {
        Log.d("버전체크", "appVersion=${appVersionCode}, required=${info?.requiredMinVersion?:-1}")
        return if (info == null) {
//            LogUtils.assert(false, "info is null")
            // 토큰을 가지고 버전체크시 여기
            Required.NOT
        } else if (appVersionCode < info!!.requiredMinVersion) {
            Required.MAJOR
        } else if (appVersionCode < info!!.versionCode) {
            Required.MINOR
        } else {
            Required.NOT
        }
    }


    fun requestVersionInfo(
            activity: Activity,
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
                dialog.show()
            }

            override fun onResponse(call: Call<Template<VersionInfo>>, response: Response<Template<VersionInfo>>) {
                val responseInfo = response.body()?.data

                when(response.code()) {
                    200 -> {
                        info = responseInfo
                        cb(isNeedToForceUpdate(), info)
                    }
                    401 -> {
                        user?.token = ""
                        user?.commit("VersionManager")

                        cb(isNeedToForceUpdate(), null)
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
