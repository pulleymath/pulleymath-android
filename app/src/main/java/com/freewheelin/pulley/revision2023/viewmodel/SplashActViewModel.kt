package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.responseFailed
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException

class SplashActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    lateinit var goLoginActCallback: () -> Unit

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        throwable.printStackTrace()

        println("throwable : $throwable")

        when (throwable) {
            is HttpException -> {
                println("throwable HttpException 2 : ${throwable.code()} / ${throwable.message}")
            }
        }
        goLoginActCallback()

    }

    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler + exceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
}