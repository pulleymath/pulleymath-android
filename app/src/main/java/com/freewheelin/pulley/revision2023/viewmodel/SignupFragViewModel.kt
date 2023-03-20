package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.AuthRepository
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupFragViewModel(application: Application): BaseAndroidViewModel(application) {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    val enableTargetService = MutableLiveData<Boolean>(false)

    fun sendCreateDummyUser(email: String, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            createDummyUser(email)
            callback()
        }
    }

    suspend fun createDummyUser(email: String) {
        userRepository.createDummyUser(
            email = email
        )
    }
    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            withContext(Dispatchers.Main) {
                cb(tempToken.token)
            }
        }
    }
}