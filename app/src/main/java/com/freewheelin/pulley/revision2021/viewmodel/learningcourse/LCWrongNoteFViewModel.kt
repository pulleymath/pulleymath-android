package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteMapRepository
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class LCWrongNoteFViewModel(application: Application): BaseAndroidViewModel(application) {

    private val lcwrongNoteRepository: LCWrongNoteRepository by lazy { LCWrongNoteRepository() }
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)

    val noteCard by lazy { MutableLiveData<LCWrongNoteMapCard>() }
    val currQuizImage by lazy { MutableLiveData<String>("") }
    val btnText by lazy { MutableLiveData("채점하기") }
    var hintExist: Boolean = true

    var tempConceptSolutionViewFlag: Boolean? = false
    val showConceptSolutionView by lazy { MutableLiveData<Boolean>(false) }

    val remainingHintSize by lazy { MutableLiveData<Int>(0) }
    val currentAnswerOfSingle by lazy { MutableLiveData<String>("") }

    fun init(item: LCWrongNoteMapCard) {
        noteCard.postValue(item)
        currQuizImage.postValue(item.quizImageUrl)
        remainingHintSize.value = item.hints.size
        hintExist = item.hints.isNotEmpty()
    }

    fun hasMoreHint(): Boolean {
        noteCard.value?.let {
            if (it.hints.isEmpty()) return false
            if (it.hints.last().hintImageUrl == currQuizImage.value) {
                return false
            }
        }

        return true
    }

    fun quizScoring(callback: (scoring: LCPatternScoring) -> Unit) {
        if (currentAnswerOfSingle.value == "") return
        val userAnswer = ScoringReq(currentAnswerOfSingle.value!!)
        noteCard.value?.let { note ->
            val patternQuizId = note.refPatternQuizId
            val studentId = user?.studentID ?: return

            if (note.isCorrect != null) return@let
            compositeDisposable += patternRepository.patternQuizScoring(patternQuizId, studentId, "WRONG_PATTERN_QUIZ", userAnswer)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ response ->
                    Log.d(javaClass.simpleName, "quizScoring =>${response.data}")
                    response.data?.let {
                        note.isCorrect = it.isCorrect
                        noteCard.postValue(note)
                        CoroutineScope(Dispatchers.Main).launch {
                            callback(it)
                        }
                    }
                }, { error ->
                    Log.e(javaClass.simpleName, "quizScoring error=${error.localizedMessage}")
                })
        }
    }

    fun setNextHint(remainingHintSize: Int) {
        noteCard.value?.hints?.let {
            val currIndexes = it.mapIndexedNotNull { index, lcPatternQuizHint ->
                if (lcPatternQuizHint.hintImageUrl == currQuizImage.value) { index }
                else { null }
            }

            when {
                currIndexes.isEmpty() -> {
                    currQuizImage.postValue(it[0].hintImageUrl)
                    this.remainingHintSize.postValue(remainingHintSize)
                }
                else -> {
                    val currHint = it[currIndexes[0] + 1]
                    currQuizImage.postValue(currHint.hintImageUrl)
                    this.remainingHintSize.postValue(remainingHintSize)
                }
            }
        }
    }
    fun resetQuizImage() {
        noteCard.value?.let { quiz ->
            currQuizImage.postValue(quiz.quizImageUrl)
        }
    }

    fun setCurrentAnswer(answer: String?) {
        val ans = answer ?: ""
        currentAnswerOfSingle.postValue(ans)
    }
}


