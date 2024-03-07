package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    fun updateSchoolType(grade: Int) {
        val schoolType = Grade.init(grade).schoolType
        userRepository.updateSchoolType(schoolType)
    }
    fun fetchMainProfile(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            userRepository.getMainProfileV4()
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }
}