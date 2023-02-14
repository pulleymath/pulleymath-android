package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2021.repository.LCCookingRepository
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.utils.ViewTransition
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LCTutorialViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val cookingRepository = LCCookingRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val tutorialImages = listOf(
//        R.drawable.android_concept_learning_tutorial_1,
//        R.drawable.android_concept_learning_tutorial_2,
        R.drawable.android_concept_learning_tutorial_3,
        R.drawable.android_concept_learning_tutorial_4,
        R.drawable.android_concept_learning_tutorial_5,
        R.drawable.android_concept_learning_tutorial_6,
        R.drawable.android_concept_learning_tutorial_7,
        R.drawable.android_concept_learning_tutorial_8,
        R.drawable.android_concept_learning_tutorial_9,
        R.drawable.android_concept_learning_tutorial_10,
        R.drawable.android_concept_learning_tutorial_11, // 이거 클릭했을때 예제채점
        R.drawable.android_concept_learning_tutorial_12,
        R.drawable.android_concept_learning_tutorial_13,
        R.drawable.android_concept_learning_tutorial_14,
        R.drawable.android_concept_learning_tutorial_15,
        R.drawable.android_concept_learning_tutorial_16, // 여기서 클릭했을때 유형학습 채점
        R.drawable.android_concept_learning_tutorial_17,
        R.drawable.android_concept_learning_tutorial_18,
        R.drawable.android_concept_learning_tutorial_19,
        R.drawable.android_concept_learning_tutorial_20,
    )

    val transitionList = listOf(
//        ViewTransition.Instant,
//        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.SlideFromDown,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.SlideFromRight,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.SlideFromRight,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.Instant,
        ViewTransition.SlideFromRight,
        ViewTransition.Instant,
        ViewTransition.Instant,
    )
    val sequence by lazy { MutableLiveData(0) }


    fun sequencePlus1() {
        sequence.value?.let {
            sequence.postValue(it + 1)
        }
    }
    fun sequenceMinus1() {
        sequence.value?.let {
            sequence.postValue(it - 1)
        }
    }

    fun isSeqOver(seq: Int): Boolean {
        return seq >= tutorialImages.size
    }
    fun answerApiCall(seq: Int) {
        if (seq == 11) {
            cookingQuizScoring {}
        } else if (seq == 16) {
            patternQuizScoring {}
        }
    }

    fun cookingQuizScoring(callback: (Boolean) -> Unit) {
        val studentId = user?.studentID ?: return
        val scoringReq = ScoringReq("1")
        val exerciseQuizId = 1561 // 컨텐츠와 서버에서 협의된 튜토리얼 퀴즈 id

        compositeDisposable += cookingRepository.scoringCookingQuiz(exerciseQuizId, studentId, scoringReq)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "scoringCookingQuiz =>${response.data}")
                response.data?.let {

                }
            }, { error ->
                Log.e(javaClass.simpleName, "scoringCookingQuiz error=${error.localizedMessage}")
            })
    }

    fun patternQuizScoring(callback: (scoring: LCPatternScoring) -> Unit) {
        val userAnswer = ScoringReq("3")
        val patternQuizId = 690
        val studentId = user?.studentID ?: return
        compositeDisposable += patternRepository.patternQuizScoring(patternQuizId, studentId, userAnswer = userAnswer)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->

            }, { error ->
                Log.e(javaClass.simpleName, "quizScoring error=${error.localizedMessage}")
            })
    }

    fun createLearningCourseOnStudentId(callback: () -> Unit) {
        val chapterId = 175
        val studentId = MyApplication.user?.studentID ?: return

        compositeDisposable +=studyRepository.createLearningCourse(chapterId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete { callback() }
            .doOnError {
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${it.localizedMessage}")
            }.subscribe()
    }
}