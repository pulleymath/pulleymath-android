package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.OnBoardingItem
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class SplashActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
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
        if (::goLoginActCallback.isInitialized) {
            goLoginActCallback()
        }

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
                println("Error::tokenRefresh , ${error.localizedMessage}")
                expiredCb()
            })
    }
    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler + exceptionHandler) {
            val user = userRepository.getUser()
            withContext(Dispatchers.Main) {
                cb(user)
            }
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

    private val onBoardLiveUrl = "https://pulley-new-bucket.s3.ap-northeast-2.amazonaws.com/management/on_boarding/on_boarding_android.json"
    private val onBoardStagingUrl = "https://pulley-new-bucket.s3.ap-northeast-2.amazonaws.com/management/on_boarding/on_boarding_android_staging.json"

    fun getOnBoardItems(successCb: (List<String>) -> Unit, deniedCb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler + exceptionHandler) {
            val url = when (Preferences.onServerAPI.get()) {
                Network.Server.live.toString() -> onBoardLiveUrl
                Network.Server.staging.toString() -> onBoardStagingUrl
                Network.Server.dev.toString() -> onBoardStagingUrl
                else -> onBoardStagingUrl
            }
            println("온보딩 :url :${url}")
            val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
            if(data != null && data.isNotEmpty()) {
                println("온보딩 : data not null")
                println("온보딩 : data : ${data}")
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val current = sdf.format(System.currentTimeMillis())

                Gson().fromJson(data, OnBoardingItem::class.java).let { item ->
                    val dateCheck = current <= item.endDate && current >= item.startDate
                    val imageSize = item.images.size
                    println("온보딩 : dateCheck : ${dateCheck}")
//                    withContext(Dispatchers.Main) {
                        if (dateCheck && item.images.isNotEmpty()) successCb(item.images)
                        else deniedCb()
//                    }
                }
            } else {
                println("온보딩 : data null or error")
                deniedCb()
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
}