package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent

class WebClientFinishClickEventListener(
    private val listener: () -> Unit
) {
    @JavascriptInterface
    fun onBackBtn() {
        listener()
    }
}