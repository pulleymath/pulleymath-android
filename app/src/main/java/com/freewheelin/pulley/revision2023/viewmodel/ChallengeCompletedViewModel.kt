package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData

class ChallengeCompletedViewModel(application: Application): BaseAndroidViewModel(application) {

    val showStampGl = MutableLiveData<Boolean>(false)
    val showStampAnim = MutableLiveData<Boolean>(true)
    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }
}