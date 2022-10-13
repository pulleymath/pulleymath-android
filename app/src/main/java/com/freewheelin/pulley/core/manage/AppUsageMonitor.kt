package com.freewheelin.pulley.core.manage

import android.app.ActivityManager
import android.app.Instrumentation
import android.util.Log
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.LogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.timerTask

interface AppUsageMonitorListener {
    fun monitoringTick()
}

object AppUsageMonitor {

    var accumulatedUsageTime: Long = 0

    var accumulatedStudyTime: Long = 0

    var timer: Timer? = null

    var listeners: ArrayList<AppUsageMonitorListener> = arrayListOf()


    var isSynchronizing = false

    val saveDuration: Long = 15 * 1000

    var lastSaveDuration = Date()

    var isForeground: Boolean = true

    var isStudying = false



    fun startAppUsage(listener: AppUsageMonitorListener? = null) {
        if(listener != null) listeners.add(listener)

        timer?.cancel()
        timer = null
        timer = Timer()
        val task = timerTask {
//            Log.d("===APP USAGE MONITOR===", "MONITOR")
            if(isForeground) {
                accumulatedUsageTime += 1

                if(isStudying)
                    accumulatedStudyTime += 1
            }
            addMonitorTimeIfNeed() // 공부시간 기록
            listeners.forEach { it.monitoringTick() }
        }
        timer?.schedule(task, 1000, 1000)
    }

    fun finishAppUsage() {
        listeners.clear()
        finishStudy()
    }

    fun startStudy(listener: AppUsageMonitorListener? = null) {
        if(listener != null) listeners.add(listener)

        isStudying = true
    }

    fun finishStudy(listener: AppUsageMonitorListener? = null) {
        listeners.remove(listener)
        isStudying = false
    }

    fun addMonitorTimeIfNeed() {
        val user = user ?: return
        if((isSynchronizing
                || lastSaveDuration.time + saveDuration > Date().time)
                || !isForeground
        ) {
            return
        }

        isSynchronizing = true
        val appUsageTime = accumulatedUsageTime
        val studyTime = accumulatedStudyTime
        val param: Parameter = Parameter(
                "totalStudyTime" to appUsageTime,
                "onlyStudyTime" to studyTime,
                "studentID" to user.studentID
        )
        // TODO dummy post studytime 짜증나서 접어둠
//        API_V2.postStudyTime(param).enqueue(object: Callback<Void> {
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                isSynchronizing = false
//            }
//
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                accumulatedUsageTime = 0
//                accumulatedStudyTime = 0
//                isSynchronizing = false
//                lastSaveDuration = Date()
//            }
//
//        })
    }
}

