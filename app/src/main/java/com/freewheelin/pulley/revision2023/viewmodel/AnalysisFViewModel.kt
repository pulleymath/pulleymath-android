package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalysisFViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    private val anonymousRepository by lazy { AnonymousRepository.instance }

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }
}