package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiepWebViewViewModel(application: Application) : BaseAndroidViewModel(application) {
    private val userRepository by lazy { UserRepository.instance }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)

    /**
     * 교육청(AIEP) SSO 복귀 URL에서 받은 토큰을 검증하고 로그인 상태를 확정한다.
     * 검증 실패(401 등)는 contentExceptionHandler → errorAction으로 전파된다.
     */
    fun signInWithToken(token: String, onSuccess: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            MyApplication.token = token
            val user = userRepository.getUser() // 토큰 검증 겸 유저 조회 (실패 시 예외)
            MyApplication.user = user
            MyApplication.token = user.token
            user.commit("AiepWebViewActivity")
            userRepository.getMainProfileV4()
            // 로그인 로그는 실패해도 로그인 흐름을 막지 않는다
            runCatching {
                legacyV2Repository.postLoginLog(
                    studentID = user.studentID,
                    email = user.accountEmail,
                    itemName = "성공"
                )
            }
            _isLoading.postValue(false)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun putFcmToken(token: String) {
        compositeDisposable += API_APP.putToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _ ->
                Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
            }, { /* error */ })
    }
}
