package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isSPYMode
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TestActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }
    val user = userRepository.user
    val schoolTypeInRepo = userRepository.schoolType

    fun getRecommendLevelText(level: Int): String {
        return when(level) {
            0 -> "더 쉽게"
            1 -> "수준에 맞게"
            2 -> "더 어렵게"
            else -> {
                "더 어렵게"
            }
        }
    }
    fun getRecommendRangeText(chapter: Int): String {
        return when(chapter) {
            0 -> "최근 공부한 범위"
            1 -> "수능 전범위"
            2 -> "내가 선택한 과목"
            else -> {
                "내가 선택한 과목"
            }
        }
    }

}