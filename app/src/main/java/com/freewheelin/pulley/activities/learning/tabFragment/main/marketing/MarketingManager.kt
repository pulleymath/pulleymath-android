package com.freewheelin.pulley.activities.learning.tabFragment.main.marketing

import android.content.Context
import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.utils.Preferences
import com.google.gson.Gson
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

    fun getInfo(context: Context, callback:(marketing: Marketing?)->Unit) {

        val url = when (Preferences.onServerAPI.get()) {
            Network.Server.live.toString() -> URL
            Network.Server.staging.toString() -> URL_BETA
            Network.Server.dev.toString() -> URL_BETA
            else -> URL
        }
        Log.d("마케팅", "url=$url")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
                if(data != null && data.isNotEmpty()) {
                    Gson().fromJson(data, Marketing::class.java).let { marketing ->
                        if(isShow(context, marketing)) {
                            withContext(Dispatchers.Main) {
                                callback(marketing)
                            }
                        }
                    }
                }
            } catch (e:Exception) {
                Log.d("마케팅에러", "getInfo error=${e.localizedMessage}")
            }
        }
    }

    fun isShow(context: Context, marketing:Marketing) : Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var nowDate = Calendar.getInstance()

        val key = "$KEY_PREFIX@@${marketing.marketingCode}"
        val saveDate = pref.getString(key, null)
        if (!saveDate.isNullOrEmpty()) {
            val saveDateParsed = sdf.parse(saveDate)
            val dateDiff = (nowDate.time.time - saveDateParsed.time).toFloat() / (60 * 60 * 24 * 1000).toFloat()
            if (dateDiff < 7) return false
        }
        filterBanner(marketing)
        return marketing.banners.isNotEmpty()
    }
//    1643026270780 - 1642987740000 = 38530780
    fun filterBanner(marketing:Marketing) {
        val studentSegment = getStudentSegment(user?.grade)
        val userSegment = user?.serviceType!!
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        val current = sdf.format(System.currentTimeMillis())

        var limit = marketing.banners.size

        for(idx in limit-1 downTo 0) {
            val banner = marketing.banners.get(idx)
            val studentCheck = banner.studentSegment.contains(studentSegment) || banner.studentSegment.contains(StudentSegment.All)
            val userCheck = banner.userSegment.contains(userSegment) || banner.userSegment.contains(PaidServiceType.ALL)
            val dateCheck = current <= banner.endDate && current >= banner.startDate
            Log.d("마케팅", "banner.studentSegment = ${banner.studentSegment}")
            Log.d("마케팅", "banner.userSegment = ${banner.userSegment}, userSegment = ${userSegment}")
            Log.d("마케팅", "banner.startDate = ${banner.startDate}, 현재시간 = ${current}, banner.endDate = ${banner.endDate}")
            Log.d("마케팅", "studentCheck : ${studentCheck}, userCheck:${userCheck}, dateCheck: ${dateCheck}, result? : ${!studentCheck || !userCheck || !dateCheck}")

            if(!studentCheck || !userCheck || !dateCheck) {
                marketing.banners.remove(banner)
            }
        }
    }

    fun getStudentSegment(grade:Grade?) : StudentSegment {
        return when(grade) {
            Grade.Middle_1 -> StudentSegment.Middle1
            Grade.Middle_2 -> StudentSegment.Middle2
            Grade.Middle_3 -> StudentSegment.Middle3
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

    fun setMarketingBanner(context: Context) {
        getInfo(context) { marketing ->
            if(marketing != null && isShow(context, marketing)) {
                val dialog = MarketingDialog(context, marketing)
                dialog.setCancelable(false)
                dialog.show()
            }
        }
    }
}