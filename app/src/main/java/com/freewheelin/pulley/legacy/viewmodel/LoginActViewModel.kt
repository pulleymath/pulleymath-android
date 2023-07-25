package com.freewheelin.pulley.legacy.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.model.challenge.StartChallenge
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
}