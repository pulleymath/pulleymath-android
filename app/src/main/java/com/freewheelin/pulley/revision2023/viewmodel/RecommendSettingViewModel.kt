package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecommendSettingViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    lateinit var setStep: (SnackTestRecommendSettingDialog.ViewType) -> Unit
    lateinit var replaceStep: (SnackTestRecommendSettingDialog.ViewType) -> Unit
    lateinit var removeStep: (Fragment) -> Unit
    lateinit var onExitClickCallback: (() -> Unit)
    fun exitBtn() {
        onExitClickCallback()
    }

}