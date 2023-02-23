package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent

class UnivAdditionalLearningWebClientClickEventListener(
    private val listener: () -> Unit
) {
    @JavascriptInterface
    fun onBackBtn() {
        listener()
    }
}