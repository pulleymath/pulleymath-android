package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import com.freewheelin.pulley.utils.DialogUtils
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class VideoPlayerViewModel : BaseViewModel(), LifecycleObserver {
    val affiliatedTestRepository: AffiliatedTestRepository by lazy { AffiliatedTestRepository.instance }
    val currentProblem by lazy { affiliatedTestRepository.currentProblem }
    var currentMedia: AffiliatedSolution? = null

    var isSubmitBtnActive = MutableLiveData(false)
    var responseMediaId: Int? = null

    @SuppressLint("CheckResult")
    fun makeMediaLog() {
        val media = currentMedia ?: return
        val problemId = currentProblem.value?.id ?: return
        val studentId = user?.studentID ?: return

        val mediaLog = AffiliatedMediaLog(problemId, media.id, media.media_file_id, studentId)
        responseMediaId = null
        affiliatedTestRepository.makeMediaLog(mediaLog)
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

        val mediaLog = AffiliatedMediaLog(problemId, media.id, media.media_file_id, studentId)

        affiliatedTestRepository.finishMediaLog(responseMediaId!!, mediaLog)
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