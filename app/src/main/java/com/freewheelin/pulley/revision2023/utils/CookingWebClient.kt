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
        view?.evaluateJavascript(addPauseListener(), null)
    }
    fun addPauseListener(): String {
//        아래에 있는 console.log('pauseListener'); 를 빼면 onpause가 동작하지 않는다. 이유를 모르겠음
        val js = """
            javascript:
            console.log('pauseListener');
            const videoQuery = document.querySelector('video');
            videoQuery.onpause = () => {
                const endScreen = document.getElementsByClassName('html5-endscreen')[0];
                if (endScreen) { endScreen.style.display = 'none' }
            }
        """.trimMargin()
        return js
    }
    fun addMyClickCallBackJs(): String {
        var js = "javascript:"
        js += "function clickListener(event){" +
                "if(event.target.className == null){androidInterface.clickListener(event.target.id)}" +
                "else{androidInterface.clickListener(event.target.className)}" +
                "const overlayElement = document.getElementsByClassName('ytp-pause-overlay-container')[0];" +
                "setTimeout(() => { " +
                    "if (overlayElement) { overlayElement.style.display = 'none' }" +
                " }, 100);" +
                "const videoElement = document.querySelector('video');" +
                "videoElement.onended = (event) => {" +
//                    "console.log('aspasp video ended');" +
                    "const endScreen = document.getElementsByClassName('html5-endscreen')[0];" +
                    "if (endScreen) { endScreen.style.display = 'none' }" +
                "}" +
                // 여기에 videoElement.onpause를 달면 동작하지 않아서 위에 addPauseListener로 뺌
            "}"
        js += "document.addEventListener(\"click\",clickListener,true);"
        return js
    }
}