package com.freewheelin.pulley.revision2023.utils

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

class UnivAdditionalLearningWebClient(
    private val pageFinishedCallback: () -> Unit
): WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        println("Univ Additional Learning page Finished url : ${url}")
        pageFinishedCallback()
//        view?.evaluateJavascript(addMyClickCallBackJs(), null)
    }

    fun addMyClickCallBackJs(): String {
        var js = "javascript:"
        js += "function clickListener(event){" +
            "if(event.target.className == null){androidInterface.clickListener(event.target.id)}" +
            "else{androidInterface.clickListener(event.target.className)}}"
        js += "document.addEventListener(\"click\",clickListener,true);"
        return js
    }
}