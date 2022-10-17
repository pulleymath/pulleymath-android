package com.freewheelin.pulley.core.manage

import android.annotation.SuppressLint
import android.util.Log
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.repository.LearningCourseRepository
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

interface AppUsageMonitorListener {
    fun monitoringTick()
}

object AppUsageMonitor {

    var accumulatedUsageTime: Long = 0

    var accumulatedStudyTime: Long = 0

    var accumulatedConceptLearningTime: Long = 0

    var timer: Timer? = null

    var listeners: ArrayList<AppUsageMonitorListener> = arrayListOf()


    var isSynchronizing = false

    val saveDuration: Long = 15 * 1000

    val saveDuration10Sec: Long = 9 * 1000

    var lastSaveDuration = Date()

    var isForeground: Boolean = true

    var isStudying = false
    var isConceptLearning = false
    var learningChapterId: Int? = null

    fun startConceptLearningUsage() {
        timer?.cancel()
        timer = null
        timer = Timer()
        val task = timerTask {
            if (isForeground && isConceptLearning) {
                accumulatedConceptLearningTime += 1
                if (lastSaveDuration.time + saveDuration10Sec < Date().time) {
                    postConceptLearningTime()
                }
            }
        }
        timer?.schedule(task, 1000, 1000)
    }

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

    fun startConceptLearning(chapterId: Int?) {
        learningChapterId = chapterId
        isConceptLearning = true
        lastSaveDuration = Date()
    }

    fun finishConceptLearning() {
        postConceptLearningTime()

        learningChapterId = null
        isConceptLearning = false
        timer?.cancel()
        timer = null
    }

    fun pauseConceptLearning() {
//        learningChapterId = null
        isConceptLearning = false
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

        API_V2.postStudyTime(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                isSynchronizing = false
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                accumulatedUsageTime = 0
                accumulatedStudyTime = 0
                isSynchronizing = false
                lastSaveDuration = Date()
            }

        })
    }

    private val courseRepository: LearningCourseRepository by lazy { LearningCourseRepository() }

    @SuppressLint("CheckResult")
    fun postConceptLearningTime() {
        val param: Parameter = Parameter(
            "type" to "STUDY_TIME",
            "time" to accumulatedConceptLearningTime
        )

        val studentId = user?.studentID ?: return
        val chapterId = learningChapterId ?: return
        courseRepository.postUserConceptLearningTime(studentId, chapterId, param)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "postUserConceptLearningTime =>${response.data}")
                lastSaveDuration = Date()
                accumulatedConceptLearningTime = 0
            }, { error ->
                lastSaveDuration = Date()
                Log.e(javaClass.simpleName, "postUserConceptLearningTime error=${error.localizedMessage}")
            })
    }
}

