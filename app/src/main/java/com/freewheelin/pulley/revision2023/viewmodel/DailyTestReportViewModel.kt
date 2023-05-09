package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyTestReportViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user
    val schoolType = userRepository.schoolType


}