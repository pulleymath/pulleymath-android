package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.QuizFormat
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2021.repository.PdfRepository
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.model.StudyMemoRequest
import com.freewheelin.pulley.revision2023.repository.MemoRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

class PatternQuizViewModel(application: Application): BaseAndroidViewModel(application) {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)

    private val userRepository by lazy { UserRepository.instance }
    private val memoRepository: MemoRepository = MemoRepository(getApplication<Application>().applicationContext, viewModelScope)
    val userInRepo = userRepository.user

    var currQuizIndex = -1
    var quizSize = -1
    val patternQuiz by lazy { MutableLiveData<LCPatternQuiz>() }

//    val currBaseConceptImage by lazy { MutableLiveData<String>() }
    val currQuizImage by lazy { MutableLiveData<String>() }

    val isQuizMainConcept by lazy { MutableLiveData<Boolean>(false) }
    val showConceptSolutionView by lazy { MutableLiveData<Boolean>(false) }

    val remainingHintSize by lazy { MutableLiveData<Int>(0) }
    var hintExist: Boolean = true

    val btnText by lazy { MutableLiveData("채점하기") }
    val currQuizFormat by lazy { MutableLiveData(QuizFormat.Single) }

    var tempConceptSolutionViewFlag: Boolean? = false
    val currentAnswerOfSingle by lazy { MutableLiveData<String>("") }
    var preventScoringBtnDoubleClickFlag = false

    var alreadyHaveMemoOnThisQuiz = false
    var isMemoDrawAStrokeAtLeastOnceAsQuiz = false
    var isAllMemoRemovedOnQuiz = false


    fun initQuiz(quiz: LCPatternQuiz, currQuizIndex: Int, quizSize: Int) {
        patternQuiz.postValue(quiz)
        this.currQuizIndex = currQuizIndex
        this.quizSize = quizSize
        currQuizImage.postValue(quiz.quizImageUrl)

//        currBaseConceptImage.postValue(quiz.concepts[0].conceptImageUrl)
        hintExist = quiz.hints.isNotEmpty()
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
                        quiz.isFirstTry = true
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

    fun sendQuizScoringLog(callback: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog()
            if (logResponse.isChallengeCourse.not()) return@launch
            callback()
        }
    }

    suspend fun postLog(): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "문제풀이",
            itemName = "채점",
            itemValue = null,
            itemNote = "개념학습-유형",
        )
    }

    fun getMemoFromParams(chapterId: Int, patternQuizId: Int, case: StudyMemoCase, cb: (StudyMemo?) -> Unit) {
        val studentId = userInRepo.value?.studentID ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memo = memoRepository.getFromParams(studentId, chapterId, patternQuizId, case)
            withContext(Dispatchers.Main) {
                cb(memo)
            }
        }
    }
    fun saveMemo (memoByteArray: ByteArray, chapterId: Int, screenWidth: Int) {
        val pqId = patternQuiz.value?.patternQuizId ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {

            val memoBase64: String = Base64.encodeToString(memoByteArray, Base64.DEFAULT) ?: return@launch
            val req = StudyMemoRequest(
                mainId = chapterId,
                subId = pqId,
                memoCase = StudyMemoCase.CONCEPT_LEARNING_TYPE_PROBLEM,
                file = memoBase64,
                width = screenWidth
            )
            if (isMemoDrawAStrokeAtLeastOnceAsQuiz) saveMemo(req)
            else if (alreadyHaveMemoOnThisQuiz && isAllMemoRemovedOnQuiz) saveMemo(req)
            else println("qweqwe patternquiz에 메모 변경이 없는것으로 추정 pqId :${pqId}")
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
        println("qweqwe pattern quiz 메모 저장 완료 pqId:${req.subId}")
    }
}