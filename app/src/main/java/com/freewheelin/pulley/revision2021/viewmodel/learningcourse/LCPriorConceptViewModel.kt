package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPriorConceptInfo
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2021.repository.LCPriorConceptRepository
import com.freewheelin.pulley.revision2021.utils.KoreanUtil
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LCPriorConceptViewModel : BaseViewModel(), LifecycleObserver {

    private val reviewRepository by lazy { LCPriorConceptRepository() }
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val priorConceptInfoList by lazy { MutableLiveData<List<LCPriorConceptInfo>>() }

    @SuppressLint("CheckResult")
    fun fetchPriorConcept(lessonTitle: String, chapterId: Int?) {
        if (chapterId == null) return
        val studentId = user?.studentID ?: return

        reviewRepository.fetchPriorConcept(chapterId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchPriorConcept =>${response.data}")
                response.data?.let {
                    it.sortedBy { it.learningCoursePriorConceptId }.let { list ->
                        priorConceptInfoList.postValue(list)
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchPriorConcept error=${error.localizedMessage}")
            })
    }

    @SuppressLint("CheckResult")
    fun completedPriorConcept(reviewId: Int, studentId: String) {
        reviewRepository.completedReview(reviewId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "completedReview =>${response.data}")
                response.data?.let {
                }
            }, { error ->
                Log.e(javaClass.simpleName, "completedReview error=${error.localizedMessage}")
            })
    }

    @SuppressLint("CheckResult")
    fun createLearningCourseOnStudentId(chapterId: Int, callback: () -> Unit) {
        val studentId = MyApplication.user?.studentID ?: return

        studyRepository.createLearningCourse(chapterId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "createLearningCourseOnStudentId =>${response.data}")
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${error.localizedMessage}")
            })
    }

}