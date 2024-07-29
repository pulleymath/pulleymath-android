package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface

class ChatBotClientClickEventListener(
    private val onCloseListener: () -> Unit,
    private val errorCloseListener: () -> Unit,
    private val analyzedMemoListener: () -> String = { "" },
    private val isMemoExistListener: () -> Boolean = { false },
) {
    @JavascriptInterface
    fun onClose() {
        onCloseListener()
    }

    @JavascriptInterface
    fun errorClose() {
        errorCloseListener()
    }

    @JavascriptInterface
    fun analyzedMemo(): String {
        return analyzedMemoListener()
    }
    @JavascriptInterface
    fun isMemoExist(): Boolean {
        return isMemoExistListener()
    }
}