package com.freewheelin.pulley.activities.learning.tabFragment.main.marketing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.bases.underMinHeight
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.utils.toPx
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

object MarketingManager {

    const val PREF_NAME = "marketing_pref"
    const val KEY_PREFIX = "no_show_"

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm") }

    val URL = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/marketing/marketing.json"
    val URL_BETA = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/marketing/marketing_beta.json"

    fun getInfo(context: Context, mainProfile: MainProfile, callback:(marketing: Marketing?)->Unit) {

        val url = if(BuildConfig.FLAVOR == "beta") URL_BETA else URL

        Log.d("마케팅", "url=$url")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
                if(data != null && data.isNotEmpty()) {
                    Gson().fromJson(data, Marketing::class.java).let { marketing ->
                        if(isShow(context, mainProfile, marketing)) {
                            withContext(Dispatchers.Main) {
                                callback(marketing)
                            }
                        }
                    }
                }
            }catch (e:Exception) {
                Log.d("마케팅에러", "error=${e.localizedMessage}")
            }
        }
    }

    fun isShow(context: Context, mainProfile:MainProfile, marketing:Marketing) : Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var nowDate = Calendar.getInstance()

        val key = "$KEY_PREFIX@@${marketing.marketingCode}"
        val saveDate = pref.getString(key, null)
        if (!saveDate.isNullOrEmpty()) {
            val saveDateParsed = sdf.parse(saveDate)
            val dateDiff = (nowDate.time.time - saveDateParsed.time).toFloat() / (60 * 60 * 24 * 1000).toFloat()
            if (dateDiff < 7) return false
        }
        filterBanner(mainProfile, marketing)
        return marketing.banners.isNotEmpty()
    }

    fun filterBanner(mainProfile:MainProfile, marketing:Marketing) {
        val studentSegment = getStudentSegment(user?.grade)
        val userSegment = if(user?.hasPulleyPlus == true) UserSegment.Paid else UserSegment.Free
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        val current = sdf.format(System.currentTimeMillis())
        Log.d("마케팅", "현재시간 $current")

        var limit = marketing.banners.size

        for(idx in limit-1 downTo 0) {
            val banner = marketing.banners.get(idx)
            val studentCheck = banner.studentSegment.contains(studentSegment) || banner.studentSegment.contains(StudentSegment.All)
            val userCheck = banner.userSegment.contains(userSegment) || banner.userSegment.contains(UserSegment.All)
            val dateCheck = current <= banner.endDate && current >= banner.startDate
            Log.d("마케팅", "banner.endDate = ${banner.endDate}, banner.startDate = ${banner.startDate}")

            if(!studentCheck || !userCheck || !dateCheck) {
                marketing.banners.remove(banner)
            }
        }
    }

    fun getStudentSegment(grade:Grade?) : StudentSegment {
        return when(grade) {
            Grade.BeforeHigh -> StudentSegment.Middle
            Grade.High_1 -> StudentSegment.High1
            Grade.High_2 -> StudentSegment.High2
            Grade.High_3 -> StudentSegment.High3
            else -> StudentSegment.N
        }
    }

    fun setNoShow(context: Context, marketing:Marketing){
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val today = sdf.format(System.currentTimeMillis())
        val key = "$KEY_PREFIX@@${marketing.marketingCode}"
        pref.edit().putString(key, today).apply()
    }

    fun setMarketingBanner(context: Context, mainProfile: MainProfile) {
        getInfo(context, mainProfile) { marketing ->
            if(marketing != null && isShow(context, mainProfile, marketing)) {
                val dialog = MarketingDialog(context, marketing)
                dialog.setCancelable(false)
                dialog.show()
            }
        }
    }
}