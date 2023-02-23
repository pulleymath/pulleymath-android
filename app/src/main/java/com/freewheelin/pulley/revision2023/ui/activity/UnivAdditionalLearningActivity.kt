package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityLessonBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.utils.CookingChromeClient
import com.freewheelin.pulley.revision2023.utils.CookingWebClient
import com.freewheelin.pulley.revision2023.utils.UnivAdditionalLearningWebClient
import com.freewheelin.pulley.revision2023.utils.listeners.CookingWebClientClickEventListener
import com.freewheelin.pulley.revision2023.utils.listeners.UnivAdditionalLearningWebClientClickEventListener
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.Preferences
import com.freewheelin.pulley.utils.PulleyEvent
import java.lang.Exception

class UnivAdditionalLearningActivity : BaseActivity() {

    val API_ADDITIONAL_LEARNING_DOMAIN = when (Preferences.onServerAPI.get()) {
        Network.Server.live.toString() -> "https://pulleymath.com/exam/study?token=${user?.token}"
        Network.Server.staging.toString() -> "https://dev.pulleymath.com/exam/study?token=${user?.token}"
        Network.Server.dev.toString() -> "https://dev.pulleymath.com/exam/study?token=${user?.token}"
        else -> "https://pulleymath.com"
    }

//    val enableHost = arrayOf("https://pulleymath.com", "https://dev.pulleymath.com")

    private val binding: ActivityLessonBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_lesson, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setWebView()
    }

    private fun setWebView() {
        with(binding.webView) {
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
            webViewClient = UnivAdditionalLearningWebClient {
                // pageFinishedCallback event
            }

            addJavascriptInterface(UnivAdditionalLearningWebClientClickEventListener {
                finish()
            }, "androidInterface")

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(API_ADDITIONAL_LEARNING_DOMAIN)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        Log.d(javaClass.simpleName, "host check =========> ${binding.webView.url}")

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyDown(keyCode, event)
//            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
        } else if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.url?.startsWith(API_ADDITIONAL_LEARNING_DOMAIN) == true) {
            finish()
        } else if (keyCode == KeyEvent.KEYCODE_BACK)  {
            binding.webView.goBack()
        }

        return true
//        return super.onKeyDown(keyCode, event)
    }

//    inner class AdditionalLearaningClient : WebViewClient() {
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            val hostUrl = Uri.parse(url).host
//            Log.d(javaClass.simpleName,"host check =========> $hostUrl")
//            if ( enableHost.contains(hostUrl)  ) {
//                return false
//            }
//            return false
//        }
//    }
}