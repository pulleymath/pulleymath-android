package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class VideoPlayerViewModel : BaseViewModel(), LifecycleObserver {
    val assessmentRepository: AssessmentRepository by lazy { AssessmentRepository.instance }
    val currentProblem by lazy { assessmentRepository.currentProblem }
    var currentMedia: AssessmentSolution? = null

    var isSubmitBtnActive = MutableLiveData(false)
    var responseMediaId: Int? = null

    @SuppressLint("CheckResult")
    fun makeMediaLog() {
        val media = currentMedia ?: return
        val problemId = currentProblem.value?.id ?: return
        val studentId = user?.studentID ?: return

        val mediaLog = AssessmentMediaLog(problemId, media.id, media.media_file_id, studentId)
        responseMediaId = null
        assessmentRepository.makeMediaLog(mediaLog)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "makeMediaLog =>${res.data}")

                res.data?.let { resMediaId ->
                    responseMediaId = resMediaId
                }
            }, { error ->
                Log.e(javaClass.simpleName, "makeMediaLog error=${error.localizedMessage}")
            })
    }

    @SuppressLint("CheckResult")
    fun finishMediaLog(cb: (()-> Unit)) {
        val mediaNull = currentMedia == null
        val problemNull = currentProblem.value == null
        val studentIdNull = user?.studentID == null
        val responseMediaIdNull = responseMediaId == null

        if (mediaNull || problemNull || studentIdNull || responseMediaIdNull) {
            cb()
            return
        }
        val media = currentMedia ?: return
        val problemId = currentProblem.value?.id ?: return
        val studentId = user?.studentID ?: return

        val mediaLog = AssessmentMediaLog(problemId, media.id, media.media_file_id, studentId)

        assessmentRepository.finishMediaLog(responseMediaId!!, mediaLog)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "finishMediaLog =>${res.data}")

                res.data.let { it ->
                    Handler(Looper.getMainLooper()).post {
                        cb()
                    }

                }
            }, { error ->
                Log.e(javaClass.simpleName, "finishMediaLog error=${error.localizedMessage}")
                Handler(Looper.getMainLooper()).post {
                    cb()
                }
            })
    }
}