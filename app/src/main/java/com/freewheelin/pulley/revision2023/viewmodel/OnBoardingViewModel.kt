package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.OnBoardingItem
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.google.gson.Gson
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class OnBoardingViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
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
        if (::goLoginActCallback.isInitialized) {
            goLoginActCallback()
        }
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
    fun sendLoginLog(user: UserV4?, attemptedEmail: String) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            postLog(user, attemptedEmail)
        }
    }
    suspend fun postLog(user: UserV4?, attemptedEmail: String): V2LogUserResponse {
        return legacyV2Repository.postLoginLog(
            studentID = user?.studentID,
            email = attemptedEmail,
            itemName = if (user != null) "성공" else "실패",
            isAutoLogin = true
        )
    }
    fun refreshAutoLoginToken(successCb: () -> Unit, expiredCb: () -> Unit) {
        compositeDisposable += userRepository.refreshToken()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                res.data?.let {
                    MyApplication.user?.token = it.token
                    MyApplication.token = it.token
                }

                successCb()
            }, { error ->
                expiredCb()
            })
    }
}