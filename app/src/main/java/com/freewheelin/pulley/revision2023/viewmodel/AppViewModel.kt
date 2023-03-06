package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user
    val joinedChallengeList = challengeRepository.joinedChallengeList


    fun fetchUserChallenges() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            fetchChallengeWithIng()
        }
    }
    suspend fun fetchChallengeWithIng() {
        challengeRepository.getChallengesOnStatus(ChallengeUserStatus.ING)
    }

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }
}