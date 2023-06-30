package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnackTestFragViewModel(application: Application): BaseAndroidViewModel(application) {

    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }

    fun getRecommendLevelText(level: Int): String {
        return when(level) {
            0 -> "더 쉽게"
            1 -> "수준에 맞게"
            2 -> "더 어렵게"
            else -> {
                "더 어렵게"
            }
        }
    }
    fun getRecommendRangeText(chapter: Int): String {
        return when(chapter) {
            0 -> "최근 공부한 범위"
            1 -> "수능 전범위"
            2 -> "내가 선택한 과목"
            else -> {
                "내가 선택한 과목"
            }
        }
    }
}