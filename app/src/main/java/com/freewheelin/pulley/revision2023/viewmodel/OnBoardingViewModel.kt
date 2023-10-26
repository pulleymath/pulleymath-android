package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.OnBoardingItem
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.text.SimpleDateFormat

class OnBoardingViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }
    lateinit var goLoginActCallback: () -> Unit

//    val imageList = MutableLiveData<List<String>>()

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
    fun fetchMainProfile(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            userRepository.getMainProfileV4()
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }

}