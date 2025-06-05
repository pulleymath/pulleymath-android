package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.repository.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class TerminalViewModel(application: Application) : BaseAndroidViewModel(application),
    LifecycleObserver {
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

    fun fetchUserFromSSCoaching(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
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

    fun putFirebaseToken (token: String) {
        compositeDisposable += API_APP.putToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ _ ->
                Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
            }, { /* error */ })
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

}