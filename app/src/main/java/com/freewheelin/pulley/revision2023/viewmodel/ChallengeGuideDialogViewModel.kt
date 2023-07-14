package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeGuideDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeGuideDialog.*

class ChallengeGuideDialogViewModel(application: Application): BaseAndroidViewModel(application) {

    val guideTxt = MutableLiveData<String>()
    val nextTxt = MutableLiveData<String>()
    val exitTxt = MutableLiveData<String>()
    val highlightTxt = MutableLiveData<String>()
    val pullingImage = MutableLiveData<PullingImage>()
    val showButtons = MutableLiveData<Boolean>()
    val showDismissButtons = MutableLiveData<Boolean>()
    val showSprinkleView = MutableLiveData<Boolean>()

//    var guideText: String = ""
//    var nextText: String = ""
//    var exitText: String = ""
    var highlightText: String? = null
//    var pullingIvSrc: PullingImage = PullingImage.Normal
//    var showBottomButtons: Boolean = false
//    var showBottomDismissButtons: Boolean = false
//    var showSprinkle: Boolean = false
    var canDismissOutSide: Boolean = true
}