package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.response.CourseSummary
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LCCourseEndDialogViewModel: BaseViewModel(), LifecycleObserver {
    private val repository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val courseSummary by lazy { MutableLiveData<CourseSummary>() }

    val showSprinkleView by lazy { MutableLiveData<Boolean>(false) }

    @SuppressLint("CheckResult")
    fun fetchCourseSummary(cid: Int?) {
        val chapterId = cid ?: return
        val studentId = user?.studentID ?: return
        repository.fetchCourseSummary(chapterId ,studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchCourseSummary =>${response.data}")
                response.data?.let { summary ->
                    courseSummary.postValue(summary)
                    showSprinkleView.postValue(summary.isCourseCompleted)
                }
            }, { error ->

                Log.e(javaClass.simpleName, "fetchCourseSummary error=${error.localizedMessage}")
            })
    }
}