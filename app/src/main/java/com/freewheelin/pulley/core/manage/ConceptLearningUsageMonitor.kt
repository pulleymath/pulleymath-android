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

object ConceptLearningUsageMonitor {

    var accumulatedConceptLearningTime: Long = 0
    var timer: Timer? = null

    val saveDuration10Sec: Long = 10 * 1000
    var lastSaveDuration = Date()

    var isForeground: Boolean = true

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
        isConceptLearning = false
    }

    private val courseRepository: LearningCourseRepository by lazy { LearningCourseRepository() }

    @SuppressLint("CheckResult")
    fun postConceptLearningTime() {
        if (accumulatedConceptLearningTime == 0L) return
        val param: Parameter = Parameter(
            "type" to "STUDY_TIME",
            "time" to accumulatedConceptLearningTime
        )

        val studentId = user?.studentID ?: return
        val chapterId = learningChapterId ?: return
        lastSaveDuration = Date()
        courseRepository.postUserConceptLearningTime(studentId, chapterId, param)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "postUserConceptLearningTime =>${response.data}")
                accumulatedConceptLearningTime = 0
            }, { error ->
                Log.e(javaClass.simpleName, "postUserConceptLearningTime error=${error.localizedMessage}")
            })
    }
}

