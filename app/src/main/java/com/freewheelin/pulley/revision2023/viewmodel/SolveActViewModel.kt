package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SolveActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val isStartChallengeInProgress = MutableLiveData<Boolean>(false)
    val enableTargetService = MutableLiveData<Boolean>(false)

    fun sendSubmitLog(pieceId: Int?, note: String, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postSubmitLog(pieceId, note)
            if (logResponse.isChallengeCourse.not()) return@launch
            callback()
        }
    }

    suspend fun postSubmitLog(pieceId: Int?, note: String): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "문제풀이",
            itemName = "채점",
            itemValue = "pieceID=${pieceId}",
            itemNote = note,

        )
    }

    fun sendAddSimilarLog(content: Content?, problem: Problem?, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postAddSimilarLog(content?.pieceID, problem?.id)
            if (logResponse.isChallengeCourse.not()) return@launch
            callback()
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

}