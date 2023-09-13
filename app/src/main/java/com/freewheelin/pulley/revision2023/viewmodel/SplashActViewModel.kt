package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.OnBoardingItem
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.UnknownHostException
import java.text.SimpleDateFormat

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
            val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
            if(data != null && data.isNotEmpty()) {
                println("온보딩 : data not null")
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val current = sdf.format(System.currentTimeMillis())

                Gson().fromJson(data, OnBoardingItem::class.java).let { item ->
                    val dateCheck = current <= item.endDate && current >= item.startDate
                    val imageSize = item.images.size
                    println("온보딩 : dateCheck : ${dateCheck}")
                    withContext(Dispatchers.Main) {
                        if (dateCheck && item.images.isNotEmpty()) successCb(item.images)
                        else deniedCb()
                    }
                }
            } else {
                println("온보딩 : data null or error")
                deniedCb()
            }
        }
    }
    suspend fun asd (): String? = withContext(Dispatchers.IO) {
        val url = when (Preferences.onServerAPI.get()) {
            Network.Server.live.toString() -> onBoardLiveUrl
            Network.Server.staging.toString() -> onBoardStagingUrl
            Network.Server.dev.toString() -> onBoardStagingUrl
            else -> onBoardStagingUrl
        }
        val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
        return@withContext data

    }
}