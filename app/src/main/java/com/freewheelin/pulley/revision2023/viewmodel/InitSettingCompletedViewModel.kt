package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitSettingCompletedViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    val signupMessage = MutableLiveData<HighlightMessage>()

    fun getAppSignupMessage() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val message = getSignupMessage()
            signupMessage.postValue(message)
        }
    }
    private suspend fun getSignupMessage(): HighlightMessage {
        return userRepository.getSignupMessage()
    }
}