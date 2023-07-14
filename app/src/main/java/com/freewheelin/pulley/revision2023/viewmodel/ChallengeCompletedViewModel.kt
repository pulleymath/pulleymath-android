package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}