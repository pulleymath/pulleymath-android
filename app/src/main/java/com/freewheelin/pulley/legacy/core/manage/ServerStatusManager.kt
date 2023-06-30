package com.freewheelin.pulley.legacy.core.manage

import android.app.Activity
import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.model.ServerStatus
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.legacy.utils.Preferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

object ServerStatusManager {
    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm") }

    fun requestInspectionFlag(): ServerStatus? {
        try {
            var inspectionUrl = when (Preferences.onServerAPI.get()) {
                Network.Server.live.toString() -> URL.SERVER_INSPECTION
                Network.Server.staging.toString() -> URL.STAGING_SERVER_INSPECTION
                else -> URL.SERVER_INSPECTION
            }
            if (BuildConfig.FLAVOR == "beta") inspectionUrl = URL.STAGING_SERVER_INSPECTION

            val data = Jsoup
                .connect(inspectionUrl)
                .ignoreContentType(true)
                .execute()
                .body()

            if (data != null && data.isNotEmpty()) {
                Gson().fromJson(data, ServerStatus::class.java).let { status ->
                    return if (isServerUnderInspection(status)) {
                        status
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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