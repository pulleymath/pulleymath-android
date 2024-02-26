package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2021.repository.LCCookingRepository
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.ViewTransition
import com.freewheelin.pulley.revision2023.SchoolType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LCTutorialViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val cookingRepository = LCCookingRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val transitionList = listOf(
        ViewTransition.Instant,
        ViewTransition.Instant,
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

    fun getImageListBySchoolType(): List<Int> {
        return when (schoolType) {
            SchoolType.ELEMENTARY -> {
                studyRepository.elementarySchoolTutorialImages
            }
            SchoolType.MIDDLE -> {
                studyRepository.middleSchoolTutorialImages
            }
            else -> {
                studyRepository.highSchoolTutorialImages
            }
        }
    }

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
        return seq >= getImageListBySchoolType().size
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
        val patternQuizId = 690 // 컨텐츠와 서버에서 협의된 튜토리얼 패턴 퀴즈 id
        val studentId = user?.studentID ?: return
        compositeDisposable += patternRepository.patternQuizScoring(patternQuizId, studentId, userAnswer = userAnswer)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->

            }, { error ->
                Log.e(javaClass.simpleName, "quizScoring error=${error.localizedMessage}")
            })
    }

    fun createLearningCourseOnStudentId() {
        val chapterId = 175 // 컨텐츠와 서버에서 협의된 튜토리얼 챕터 id
        val studentId = MyApplication.user?.studentID ?: return

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            studyRepository.suspendCreateLearningCourse(chapterId, studentId)
        }
    }
}