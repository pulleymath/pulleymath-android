package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteMapRepository
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.model.StudyMemoRequest
import com.freewheelin.pulley.revision2023.repository.MemoRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class LCWrongNoteFViewModel(application: Application): BaseAndroidViewModel(application) {

    private val lcwrongNoteRepository: LCWrongNoteRepository by lazy { LCWrongNoteRepository() }
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)

    private val memoRepository: MemoRepository = MemoRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    val userInRepo = userRepository.user

    val noteCard by lazy { MutableLiveData<LCWrongNoteMapCard>() }
    val currQuizImage by lazy { MutableLiveData<String>("") }
    val btnText by lazy { MutableLiveData("채점하기") }
    var hintExist: Boolean = true

    var tempConceptSolutionViewFlag: Boolean? = false
    val showConceptSolutionView by lazy { MutableLiveData<Boolean>(false) }

    val remainingHintSize by lazy { MutableLiveData<Int>(0) }
    val currentAnswerOfSingle by lazy { MutableLiveData<String>("") }

    var alreadyHaveMemoOnThisQuiz = false
    var isMemoDrawAStrokeAtLeastOnceAsQuiz = false
    var isAllMemoRemovedOnQuiz = false

    var currChapterId: Int = -1
    fun init(item: LCWrongNoteMapCard, chapterId: Int) {
        noteCard.postValue(item)
        currChapterId = chapterId
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

    fun getMemoFromParams(mainId: Int, subId: Int, case: StudyMemoCase, cb: (StudyMemo?) -> Unit) {
        val studentId = userInRepo.value?.studentID ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memo = memoRepository.getFromParams(studentId, mainId, subId, case)
            withContext(Dispatchers.Main) {
                cb(memo)
            }
        }
    }
    fun saveMemo(memoByteArray: ByteArray, case: StudyMemoCase, screenWidth: Int) {
        val userQuizSolvingHistoryId = noteCard.value?.userQuizSolvingHistoryId ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memoBase64: String = Base64.encodeToString(memoByteArray, Base64.DEFAULT) ?: return@launch
            val req = StudyMemoRequest(case, currChapterId, userQuizSolvingHistoryId, screenWidth, memoBase64)

            if (case == StudyMemoCase.CONCEPT_LEARNING_WRONG_PROBLEM) {
                // 획이 존재하면 저장, 메모존재시 추가획 없지만 다 지워졌다면 저장
                if (isMemoDrawAStrokeAtLeastOnceAsQuiz) saveMemo(req)
                else if (alreadyHaveMemoOnThisQuiz && isAllMemoRemovedOnQuiz) saveMemo(req)
                else println("aspasp file quiz에 메모가 없는상태로 추정 저장하지 않음.")

            }
        }
    }
    suspend fun saveMemo(req: StudyMemoRequest) {
        val studentId = userInRepo.value?.studentID ?: return
        val prevMemo = memoRepository.getFromParams(studentId, req.mainId, req.subId, req.memoCase)
        if (prevMemo == null) {
            val id = memoRepository.getMaxId() + 1
            memoRepository.upsert(StudyMemo(id, studentId, req.mainId, req.subId, req.width, req.memoCase, req.os, req.file))
        } else {
            val newMemo = StudyMemo(prevMemo.id, studentId, req.mainId, req.subId, req.width, req.memoCase, req.os, req.file)
            memoRepository.upsert(newMemo)
        }
        memoRepository.uploadMemo(req)
        println("aspasp subId: ${req.subId} : 메모 저장 완료")

    }

}


