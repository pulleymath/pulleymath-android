package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.repository.WhaleSpaceLoginRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhaleSpaceLoginViewModel(application: Application): BaseAndroidViewModel(application) {
    private val whaleSpaceLoginRepository = WhaleSpaceLoginRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)

    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    fun fetchUser(cb: (UserV4) -> Unit) {
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
    fun sendCode(code: String, cb: (ResponseBody<SignInAppToken>) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val response = whaleSpaceLoginRepository.sendCode(code)
            withContext(Dispatchers.Main) {
                cb(response)
            }
        }
    }
    fun putFcmToken(token: String) {
        compositeDisposable += API_APP.putToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ ->
                Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
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
            itemName = if (user != null) "성공" else "실패"
        )
    }
}