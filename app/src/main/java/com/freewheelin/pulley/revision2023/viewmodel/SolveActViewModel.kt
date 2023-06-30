package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.SolveActRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolveActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val solveActRepository = SolveActRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    val isStartChallengeInProgress = MutableLiveData<Boolean>(false)
//    val isStartChallengeBookPiece = MutableLiveData<Boolean>(false)
    val isOnlyStartChallengePiece = MutableLiveData<Boolean>(false)  // 스타트챌린지 문제집만 true
//    val isStartChallengeOrStartChallengeRewardPiece = MutableLiveData<Boolean>(false) // 스타트챌린지 문제집과 보상으로 받은 문제집 둘다 true
    val enableTargetService = MutableLiveData<Boolean>(false)
    val selectedContent = MutableLiveData<Content>()

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
            println("asoaso startchallenge log [addSimilar]")
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

    fun fetchUser(cb: (User) -> Unit) {
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
}