package com.freewheelin.pulley.core.manage

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
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

    val saveDuration10Sec: Long = 10 * 1000

    var lastSaveDuration = Date()

    var isForeground: Boolean = true

    var isStudying = false

    fun startAppUsage(listener: AppUsageMonitorListener? = null) {
        if(listener != null) listeners.add(listener)

        timer?.cancel()
        timer = null
        timer = Timer()
        val task = timerTask {
            if(isForeground) {
                accumulatedUsageTime += 1

                if(isStudying)
                    accumulatedStudyTime += 1
            }
            if (lastSaveDuration.time + saveDuration10Sec < Date().time) {
                postUsageTime()
            }
            listeners.forEach { it.monitoringTick() }
        }
        timer?.schedule(task, 1000, 1000)
    }

    fun postUsageTime() {
        val user = user ?: return
        if (accumulatedUsageTime == 0L) return

        val appUsageTime = accumulatedUsageTime
        val studyTime = accumulatedStudyTime

        val param: Parameter = Parameter(
            "totalStudyTime" to appUsageTime,
            "onlyStudyTime" to studyTime,
            "studentID" to user.studentID
        )
        API_V2.postStudyTime(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                accumulatedUsageTime = 0
                accumulatedStudyTime = 0
                lastSaveDuration = Date()
            }

        })
    }
    fun finishAppUsage() {
        listeners.clear()
        finishStudy()
    }

    fun startStudy(listener: AppUsageMonitorListener? = null) {
        if(listener != null) listeners.add(listener)
        lastSaveDuration = Date()
        isStudying = true
    }

    fun finishStudy(listener: AppUsageMonitorListener? = null) {
        listeners.remove(listener)
        isStudying = false
    }
}

