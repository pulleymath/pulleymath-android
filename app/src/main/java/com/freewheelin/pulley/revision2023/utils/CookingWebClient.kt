package com.freewheelin.pulley.revision2023.utils

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

class CookingWebClient(
    private val urlLoadingCallback: (String?) -> Unit,
    private val pageFinishedCallback: () -> Unit
): WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        urlLoadingCallback(url)
//        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//        (activity as LearningCourseActivity).hidePencilcasePanel()
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        // 페이지 로드 후 javascript단에서 함수를 생성해서 document를 컨트롤하는 자동재생 로직임
//                        webView.loadUrl("javascript:(function() { document.getElementsByClassName('ytp-large-play-button ytp-button')[0].click(); })()");

        pageFinishedCallback()
//        binding.rightViewProgress.visibility = View.GONE
        view?.evaluateJavascript(addMyClickCallBackJs(), null)
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