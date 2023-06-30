package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.manage.AndroidID
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.revision2023.model.request.GuestSignInRequest
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.Preferences
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }
    private val anonymousRepository by lazy { AnonymousRepository.instance }

    val user = userRepository.user

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }
    fun fetchUser(cb: (User) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            withContext(Dispatchers.Main) {
                cb(user)
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
}