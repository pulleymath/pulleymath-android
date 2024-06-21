package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface

class ChatBotClientClickEventListener(
    private val listener: () -> Unit
) {
    @JavascriptInterface
    fun onClose() {
        listener()
    }
}