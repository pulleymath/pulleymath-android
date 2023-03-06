package com.freewheelin.pulley.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.StartChallenge
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LearningTabViewModel(application: Application): BaseAndroidViewModel(application) {

    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    fun fetchUser(cb: (User) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
    fun fetchUserChallenges() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            challengeRepository.getChallengesOnStatus(ChallengeUserStatus.ING)
        }
    }
}