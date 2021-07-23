package com.freewheelin.pulley.core.manage

import android.app.Activity
import android.util.Log
import com.freewheelin.pulley.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.model.ServerStatus
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

object ServerStatusManager {
    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm") }

    fun requestInspectionFlag(): ServerStatus? {
        val data = Jsoup
            .connect(URL.SERVER_INSPECTION)
            .ignoreContentType(true)
            .execute()
            .body()
        if (data != null && data.isNotEmpty()) {

            try {
                Gson().fromJson(data, ServerStatus::class.java).let { status ->
                    return if (isServerUnderInspection(status)) {
                        status
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun isServerUnderInspection(status: ServerStatus): Boolean {
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        val current = sdf.format(System.currentTimeMillis())
        val startDate = status.checkStart
        val endDate = status.endStart
        val isServerUnderInspection = current <= endDate && current >= startDate
        return isServerUnderInspection
    }

    suspend fun setServerInspectionDialog(activity: Activity) {
        val status = requestInspectionFlag()

        withContext(Dispatchers.Main) {
            status?.let {
                val dialog = ServerInspectionDialog(activity, status)
                dialog.setCancelable(false)
                dialog.show()
            }
        }
    }
}