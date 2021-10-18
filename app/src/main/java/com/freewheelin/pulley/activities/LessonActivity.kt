package com.freewheelin.pulley.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_LESSON_DOMAIN
import kotlinx.android.synthetic.main.activity_lesson.*

class LessonActivity : BaseActivity() {

    var lessonLink = "$API_LESSON_DOMAIN/pplink?token=${user?.token}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        setWebView()
    }

    private fun setWebView() {
        with(webView) {
            webViewClient = LessonClient()
            settings.apply {
                javaScriptEnabled = true
                setSupportMultipleWindows(false) // no open windows
                javaScriptCanOpenWindowsAutomatically = false // no open windows by script
                loadWithOverviewMode = true // allow meta tag
                useWideViewPort = true // allow adjust screen size
                setSupportZoom(false) // disallow support zoom
                builtInZoomControls = false // disallow zoom controll
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL // 이거 머냐?
                cacheMode = WebSettings.LOAD_NO_CACHE // no browser cache
                domStorageEnabled = true // allow local storage
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            }

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(lessonLink) // android 와 ios 일 경우만 웹뷰에서 헤더가 제거된다.
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        with(webView) {
            if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
                goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    inner class LessonClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (Uri.parse(url).host == API_LESSON_DOMAIN) {
                return false
            }
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                startActivity(this)
            }
            return true
        }
    }
}