package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.MockExamSummary
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.SolveActRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2021.repository.PdfRepository
import com.freewheelin.pulley.revision2023.model.ChatBotInfo
import com.freewheelin.pulley.revision2023.model.ChatBotInfoImage
import com.freewheelin.pulley.revision2023.model.ChatBotInfoImageType
import com.freewheelin.pulley.revision2023.model.ChatBotInitViewType
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.model.StudyMemoRequest
import com.freewheelin.pulley.revision2023.repository.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolveActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val solveActRepository = SolveActRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
//    private val pdfRepository: PdfRepository by lazy { PdfRepository() }
    private val memoRepository: MemoRepository = MemoRepository(getApplication<Application>().applicationContext, viewModelScope)

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    val mainProfileV4 = userRepository.mainProfileV4

    val isStartChallengeInProgress = MutableLiveData<Boolean>(false)
//    val isStartChallengeBookPiece = MutableLiveData<Boolean>(false)
    val isOnlyStartChallengePiece = MutableLiveData<Boolean>(false)  // 스타트챌린지 문제집만 true
//    val isStartChallengeOrStartChallengeRewardPiece = MutableLiveData<Boolean>(false) // 스타트챌린지 문제집과 보상으로 받은 문제집 둘다 true
    val enableTargetService = MutableLiveData<Boolean>(false)
    val selectedContent = MutableLiveData<Content>()
    val selectedProblemOb = MutableLiveData<Problem>()

    var alreadyHaveMemoOnThisProblem = false
    var alreadyHaveMemoOnThisSolution = false
    var isMemoDrawAStrokeAtLeastOnceAsProblem = false
    var isMemoDrawAStrokeAtLeastOnceAsSolution = false
    var isAllMemoRemovedOnProblem = false
    var isAllMemoRemovedOnSolution = false
    var memoDebugViewCount = 0

    var chatBotInfo: ChatBotInfo? = null

    fun sendSubmitLog(pieceId: Int?, note: String, size: Int, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            println("asoaso 채점 log [size:${size}]")
            val logResponse = postSubmitLog(pieceId, note, size)
            if (logResponse.isChallengeCourse.not()) return@launch
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            println("asoaso startchallenge log [score]")
            updateChallenge(startChallenge)
            callback()
        }
    }

    suspend fun postSubmitLog(pieceId: Int?, note: String, size: Int): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "문제풀이",
            itemName = "채점",
            itemValue = "pieceID=${pieceId}",
            itemNote = "${note},${size}",
        )
    }

    var startChallengeCompletedCallback: () -> Unit = {}
    var pendingStartChallengeCompletedCallback: () -> Unit = {}
    fun sendAddSimilarLog(content: Content?, problem: Problem?, callback: (Challenge) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postAddSimilarLog(content?.pieceID, problem?.id)
            if (logResponse.isChallengeCourse.not()) return@launch
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            updateChallenge(startChallenge)
            startChallengeCompletedCallback = pendingStartChallengeCompletedCallback
        }
    }

    suspend fun postAddSimilarLog(pieceId: Int?, problemId: Int?): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "문제풀이화면",
            itemName = "유사문제",
            itemValue = "pieceID=${pieceId},problemID=${problemId}",
            itemNote = null,

        )
    }
    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }

    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
    fun getTest (type: Test.TestType, cb: (Test) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = solveActRepository.getDailyTest(type.rawText)
            withContext(Dispatchers.Main) {
                cb(user)
            }
        }
    }
    fun getReviewProblems(type: String, studyIDs: List<Int>, cb: (Piece) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val piece = solveActRepository.getReviewProblems(type, studyIDs)
            withContext(Dispatchers.Main) {
                cb(piece)
            }
        }
    }
    fun fetchMockSummary(mockId: Int, assignId: Int?, cb: (MockExamSummary?) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val summary = legacyV2Repository.fetchMockSummary(mockId, assignId ?: -999)
            cb(summary)
        }
    }


    fun fetchMemos(assignId: Int) {
        val studentId = userInRepo.value?.studentID ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memoCount = memoRepository.countMemo(studentId, assignId)
//            val latest = if (memoCount < 1) null else memoRepository.getLatestTimestamp(studentId)
//            memoRepository.fetchMemo(studentId = studentId, mainId = assignId,
//                memoCase = StudyMemoCase.PATTERN_LEARNING_PROBLEM,
//                subId = null, latest = latest)
        }
    }

    fun getMemoFromParams(assignId: Int, problemId: Int, case: StudyMemoCase, cb: (StudyMemo?) -> Unit) {
        val studentId = userInRepo.value?.studentID ?: return
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memo = memoRepository.getFromParams(studentId, assignId, problemId, case)
            withContext(Dispatchers.Main) {
                cb(memo)
            }
        }
    }


    suspend fun memoDebugger(assignId: Int, problemId: Int, size: Int): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.MEMO,
            itemCategory = "문제풀이뷰",
            itemName = "assignId=${assignId}",
            itemValue = "problemId=${problemId}",
            itemNote = "size=${size}",
        )
    }

    fun saveMemo(memoByteArray: ByteArray, assignId: Int, problemId: Int, screenWidth: Int, case: StudyMemoCase, context: Context) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val memoBase64: String = Base64.encodeToString(memoByteArray, Base64.DEFAULT) ?: return@launch
            val req = StudyMemoRequest(case, assignId, problemId, screenWidth, memoBase64)

            if (case == StudyMemoCase.PATTERN_LEARNING_PROBLEM) {
                // 획이 존재하면 저장, 메모존재시 추가획 없지만 다 지워졌다면 저장
                if (isMemoDrawAStrokeAtLeastOnceAsProblem) {
                    saveMemo(req)
                    memoDebugger(assignId, problemId, memoByteArray.size)
                    if (memoDebugViewCount > 10) {
                        withContext(Dispatchers.Main) {
                            DaebakToast.show(context, "저장 완료 type1")
                        }
                    }
                } else if (alreadyHaveMemoOnThisProblem && isAllMemoRemovedOnProblem) {
                    saveMemo(req)
                    memoDebugger(assignId, problemId, memoByteArray.size)
                    if (memoDebugViewCount > 10) {
                        withContext(Dispatchers.Main) {
                            DaebakToast.show(context, "저장 완료 type2")

                        }
                    }
                } else {
                    println("추가된 메모가 없음. 저장하지 않음. type3")
                    memoDebugger(assignId, problemId, memoByteArray.size)
                    if (memoDebugViewCount > 10) {
                        withContext(Dispatchers.Main) {
                            DaebakToast.show(context, "추가된 메모가 없음. 저장하지 않음. type3")

                        }
                    }
                }


            } else if (case == StudyMemoCase.PATTERN_LEARNING_SOLUTION) {
                if (isMemoDrawAStrokeAtLeastOnceAsSolution) saveMemo(req)
                else if (alreadyHaveMemoOnThisSolution && isAllMemoRemovedOnSolution) saveMemo(req)
                else println("aspasp file solution에 메모가 없는상태로 추정 저장하지 않음.")
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
        println("aspasp 메모 저장 완료")

    }

    fun makeChatBotInfo(problem: Problem) {
        chatBotInfo = ChatBotInfo(ChatBotInitViewType.SOLVE_PROBLEM, listOf(ChatBotInfoImage(ChatBotInfoImageType.SOLVE_PROBLEM, 0, listOf(problem.getProblemUrl()))))
    }
}