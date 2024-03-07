package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.manage.AndroidID
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.model.request.GuestSignInRequest
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val anonymousRepository by lazy { AnonymousRepository.instance }

    val user = userRepository.user

    fun updateUser(user: UserV4) {
        userRepository.updateUser(user)
    }
    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
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

    fun requestGuestSignIn(cb: (String) -> Unit) {
        val uuid = getDeviceUUID()
        println("asoaso uuid : ${uuid}")
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val req = GuestSignInRequest(uuid)
            val res = anonymousRepository.guestSignIn(req)
            cb(res.token)
        }
    }
    private fun getDeviceUUID(): String {
        return AndroidID.getCreatedUUID(getApplication<Application>().applicationContext)
    }

    private fun getFcmToken(cb: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            if (token?.isNotEmpty() == true) {
                cb(token)
            }
        })
    }
    fun setLoading(isShow: Boolean) {
        _isLoading.postValue(isShow)
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