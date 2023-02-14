package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeGuideDialog

class ChallengeGuideDialogViewModel(application: Application): BaseAndroidViewModel(application) {

    val guideTxt = MutableLiveData<String>()
    val nextTxt = MutableLiveData<String>()
    val exitTxt = MutableLiveData<String>()
    val highlightTxt = MutableLiveData<String>()
    val pullingImage = MutableLiveData<ChallengeGuideDialog.PullingImage>()
    val showButtons = MutableLiveData<Boolean>()

}