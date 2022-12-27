package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.QuizFormat
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class PatternQuizViewModel: BaseViewModel(), LifecycleObserver {
    private val patternRepository: LCPatternRepository by lazy { LCPatternRepository() }

    var currQuizIndex = -1
    var quizSize = -1
    val patternQuiz by lazy { MutableLiveData<LCPatternQuiz>() }

    val currBaseConceptImage by lazy { MutableLiveData<String>() }
    val currQuizImage by lazy { MutableLiveData<String>() }

    val isQuizMainConcept by lazy { MutableLiveData<Boolean>(false) }
    val showConceptSolutionView by lazy { MutableLiveData<Boolean>(false) }

    val remainingHintSize by lazy { MutableLiveData<Int>(0) }

    val btnText by lazy { MutableLiveData("채점하기") }
    val currQuizFormat by lazy { MutableLiveData(QuizFormat.Single) }

    var tempConceptSolutionViewFlag: Boolean? = false
    val currentAnswerOfSingle by lazy { MutableLiveData<String>("") }
    var preventScoringBtnDoubleClickFlag = false


    fun initQuiz(quiz: LCPatternQuiz, currQuizIndex: Int, quizSize: Int) {
        patternQuiz.postValue(quiz)
        this.currQuizIndex = currQuizIndex
        this.quizSize = quizSize
        currQuizImage.postValue(quiz.quizImageUrl)

        currBaseConceptImage.postValue(quiz.concepts[0].conceptImageUrl)
        remainingHintSize.postValue(quiz.hints.size)
    }

    fun hasMoreHint(): Boolean {
        patternQuiz.value?.let {
            if (it.hints.isEmpty()) return false
            if (it.hints.last().hintImageUrl == currQuizImage.value) {
                return false
            }
        }

        return true
    }

    fun setNextHint(remainingHintSize: Int) {
        patternQuiz.value?.hints?.let {
            val currIndexes = it.mapIndexedNotNull { index, lcPatternQuizHint ->
                if (lcPatternQuizHint.hintImageUrl == currQuizImage.value) { index }
                else { null }
            }

            when {
                currIndexes.isEmpty() -> {
                    currQuizImage.postValue(it[0].hintImageUrl)
                    this.remainingHintSize.postValue(remainingHintSize)
//                    if (it.lastIndex == 0) {
//                        isHintButtonDisabled.postValue(true)
//                    }
                }
                else -> {
                    val currHint = it[currIndexes[0] + 1]
                    currQuizImage.postValue(currHint.hintImageUrl)
                    this.remainingHintSize.postValue(remainingHintSize)

//                    if (currIndexes[0] + 1 == it.lastIndex) {
//                        isHintButtonDisabled.postValue(true)
//                    }
                }
            }
        }
    }

    fun resetQuizImage() {
        patternQuiz.value?.let { quiz ->
            currQuizImage.postValue(quiz.quizImageUrl)
        }
    }
    fun isShortFormat(): Boolean {
        return currQuizFormat.value == QuizFormat.Short
    }

    fun isNotShortFormat(): Boolean {
        return currQuizFormat.value != QuizFormat.Short
    }

    fun quizScoring(callback: (scoring: LCPatternScoring) -> Unit) {
        if (currentAnswerOfSingle.value == "") return
        val userAnswer = ScoringReq(currentAnswerOfSingle.value!!)
        patternQuiz.value?.let { quiz ->
            val patternQuizId = quiz.patternQuizId
            val studentId = user?.studentID ?: return
            if (quiz.isCorrect != null) return@let
            compositeDisposable += patternRepository.patternQuizScoring(patternQuizId, studentId, userAnswer = userAnswer)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ response ->
                    Log.d(javaClass.simpleName, "quizScoring =>${response.data}")
                    response.data?.let {
                        quiz.isCorrect = it.isCorrect
                        patternQuiz.postValue(quiz)
                        CoroutineScope(Dispatchers.Main).launch {
                            callback(it)
                        }
                    }
                }, { error ->
                    Log.e(javaClass.simpleName, "quizScoring error=${error.localizedMessage}")
                })
        }
    }

    fun quizLastIndex(): Int {
        return quizSize - 1
    }

    fun setCurrentAnswer(answer: String?) {
        val ans = answer ?: ""
        currentAnswerOfSingle.postValue(ans)
    }



}