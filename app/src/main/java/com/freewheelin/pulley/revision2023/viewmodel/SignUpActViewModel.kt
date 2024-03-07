package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val anonymousRepository by lazy { AnonymousRepository.instance }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user
    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }

    fun requestGuestSignUp(req: RequestSignup, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            anonymousRepository.guestSignUp(req)
            cb()
        }
    }

    fun requestUserSignUp(signup: RequestSignup, successCallback: () -> Unit) {
        API_V1.signup(signup).enqueue(object: Callback<Template<String?>> {
            override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                when(response.code()) {
                    200 -> successCallback()
                    else -> {
                        setLoading(false)
                        DialogUtils.showDialog(getApplication<Application>().applicationContext, "회원가입 실패", "회원가입이 정상적으로 진행되지 않았습니다\n다시 시도해 주세요!!")
                    }
                }
            }

            override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                DialogUtils.showServerErr(getApplication<Application>().applicationContext)
            }
        })
    }
    fun requestSignUpReward(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            delay(200)
            userRepository.requestRewardSignUp()
            cb()
        }
    }
    fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    fun sendLoginLog(user: UserV4?, attemptedEmail: String, loginSuccess: Boolean) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            postLog(user, attemptedEmail, loginSuccess)
        }
    }
    suspend fun postLog(user: UserV4?, attemptedEmail: String, loginSuccess: Boolean): V2LogUserResponse {
        return legacyV2Repository.postLoginLog(
            studentID = user?.studentID,
            email = attemptedEmail,
            itemName = if (loginSuccess) "성공" else "실패"
        )
    }
}