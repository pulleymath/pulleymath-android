package com.freewheelin.pulley.revision2023.utils.listeners

import android.webkit.JavascriptInterface
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent

class CookingWebClientClickEventListener(
    private val listener: () -> Unit
) {
    @JavascriptInterface
    fun clickListener(idOrClass: String) {
        val largeRedPlayButton = "ytp-large-play-button ytp-button ytp-large-play-button-red-bg"
        val thumbnailImage = "ytp-cued-thumbnail-overlay-image"
        if (idOrClass == largeRedPlayButton || idOrClass == thumbnailImage) {
            listener()
        }
    }
}