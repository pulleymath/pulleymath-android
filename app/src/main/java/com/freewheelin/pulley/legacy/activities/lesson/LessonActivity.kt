package com.freewheelin.pulley.legacy.activities.lesson

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ActivityLessonBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity.Companion.lessonFinished
import java.lang.Exception

class LessonActivity : BaseActivity() {

//    val API_LESSON_DOMAIN =
//        if(Preferences.onTestAPI.get()) "https://dev.pulleymath.com" else "https://pulleymath.com"

    val API_LESSON_DOMAIN = when (Preferences.onServerAPI.get().toString()) {
        Network.Server.live.toString() -> "https://pulleymath.com"
        Network.Server.staging.toString() -> "https://dev.pulleymath.com"
        Network.Server.dev.toString() -> "https://dev.pulleymath.com"
        else -> "https://pulleymath.com"
    }

    val lessonPath = "$API_LESSON_DOMAIN/pplink"
    val lessonLink = "$lessonPath?token=${user?.token}"

    val enableHost = arrayOf("https://pulleymath.com", "https://dev.pulleymath.com", "https://pagecall.net", "https://app.pagecall.net", "https://console.pagecall.net")

    private var _filePathCallback: ValueCallback<Array<Uri>>? = null

    private val filterActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // WebView 내부에서 파일 선택자를 열기 위한 로직
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                var results: Array<Uri>? = null

                it.data?.let { data ->
                    data.dataString?.let { dataString ->
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
                _filePathCallback?.onReceiveValue(results)
            } else {
                // 에러 또는 선택된 파일이 없더라도 반드시 초기화 해주어야 한다.
                // 그렇지 않으면 다시 파일 선택자가 열리지 않는다.
                _filePathCallback?.onReceiveValue(null)
            }

            _filePathCallback = null
        }

    lateinit var audioManager: AudioManager
    private val binding: ActivityLessonBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_lesson, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setWebView()

        // Declare an audio manager
        volumeControlStream = AudioManager.STREAM_MUSIC
        audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private fun setWebView() {
        with(binding.webView) {
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

                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources)
                    }

                    // 파일 업로드를 위한 설정
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?, // 여기 null 넘어오는 케이스 있음
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        if (_filePathCallback != null) {
                            _filePathCallback!!.onReceiveValue(null)
                            _filePathCallback = null
                        }

                        try {
                            _filePathCallback = filePathCallback
                            val intent = Intent()
                            intent.apply {
                                action = android.content.Intent.ACTION_GET_CONTENT
                                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, fileChooserParams!!.acceptTypes)
                            }

                            filterActivityLauncher.launch(intent)
                        } catch (e: Exception) {
                            _filePathCallback!!.onReceiveValue(null)
                            _filePathCallback = null
                        }

                        return true
                    }
                }
            }

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(lessonLink) // android 와 ios 일 경우만 웹뷰에서 헤더가 제거된다.
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        Log.d(javaClass.simpleName, "host check =========> ${binding.webView.url}")

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyDown(keyCode, event)
//            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
        } else if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.url?.startsWith(lessonPath) == true) {
//            setResult(lessonFinished, intent)
            finish()
        } else if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.url?.contains("pagecall.net") == true){
            binding.webView.loadUrl(lessonLink)
        } else if (keyCode == KeyEvent.KEYCODE_BACK)  {
            binding.webView.goBack()
        }

        return true
//        return super.onKeyDown(keyCode, event)
    }

    inner class LessonClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val hostUrl = Uri.parse(url).host
            Log.d(javaClass.simpleName,"host check =========> $hostUrl")
            if ( enableHost.contains(hostUrl)  ) {
                return false
            }
            return false
        }
    }
}