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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MockListActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }

    val user = userRepository.user
    val schoolTypeInRepo = userRepository.schoolType

}