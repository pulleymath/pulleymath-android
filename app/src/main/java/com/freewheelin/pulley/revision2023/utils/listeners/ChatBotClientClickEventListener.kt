package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface

class ChatBotClientClickEventListener(
    private val onCloseListener: () -> Unit,
    private val errorCloseListener: () -> Unit,
) {
    @JavascriptInterface
    fun onClose() {
        onCloseListener()
    }

    @JavascriptInterface
    fun errorClose() {
        errorCloseListener()
    }
}