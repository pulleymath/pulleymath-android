package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository

class TeacherUtilityViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType


    lateinit var onExitClickCallback: (() -> Unit)

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()

    fun exitBtn() {
        onExitClickCallback()
    }
}