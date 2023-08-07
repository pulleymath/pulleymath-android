package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengeCompletedViewModel(application: Application): BaseAndroidViewModel(application) {

    private val challengeRepository by lazy { ChallengeRepository.instance }

    val showStampGl = MutableLiveData<Boolean>(false)
    val showStampAnim = MutableLiveData<Boolean>(true)
    val subtitle = MutableLiveData<String>()
    lateinit var onExitClickCallback: (() -> Unit)
    lateinit var challenge: Challenge
    var isDelayedShowNextBtn: Boolean = false
    var completedCourseId: Int = 0

    fun exitBtn() {
        onExitClickCallback()
    }

    fun getChallengeCompleteInfo() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val newSubTitle = getSubtitle()
            subtitle.postValue(newSubTitle.message)
        }
    }
    private suspend fun getSubtitle(): HighlightMessage {
        return challengeRepository.getChallengeCompletedDialogSubtitle()
    }

    fun askForRedeemOfChallenge(userChallengeId: Int, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val res = challengeRepository.askForRedeemOfChallenge(userChallengeId)
            _errorAction.postValue(CoroutineExceptionType.NONE)
            if (res.error != null) {
                println("[[[[[ERROR askForRedeemOfChallenge]]]]]")
                responseFailed(getApplication<Application>().applicationContext, Throwable("${res.error} ${res.message}"))
                return@launch
            }
            withContext(Dispatchers.Main) {
                cb()
            }
            res.data?.let {
                challengeRepository.updateChallengeList(it)
            }

        }
    }
}