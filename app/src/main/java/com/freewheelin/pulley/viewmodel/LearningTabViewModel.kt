package com.freewheelin.pulley.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.revision2023.model.challenge.StartChallenge
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel

class LearningTabViewModel(application: Application): BaseAndroidViewModel(application) {

    val showStampGl = MutableLiveData<Boolean>(false)
    val showStampAnim = MutableLiveData<Boolean>(true)
    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }

}