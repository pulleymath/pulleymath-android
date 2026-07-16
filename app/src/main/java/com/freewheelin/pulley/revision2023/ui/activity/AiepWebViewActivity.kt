package com.freewheelin.pulley.revision2023.ui.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.StartActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.viewmodel.AiepWebViewViewModel
import com.google.firebase.messaging.FirebaseMessaging

/**
 * 교육청(AIEP) 로그인/이용 WebView.
 *
 * - 로그인 모드: webapp의 /link/aiep(교육청 선택 → SSO 로그인)를 앱 내 WebView로 진행하고,
 *   로그인 완료 복귀 URL(pulleymath:// 또는 /link/aiep?token=..., ?target=aiep&token=...)을
 *   인터셉트해 토큰을 검증한다. 검증 성공 시 같은 화면에서 webapp 모드로 전환.
 * - webapp 모드: 웹앱 연동 계약에 따라 /token-signin/{풀리수학토큰}으로 진입해
 *   웹이 localStorage 세션을 수립하고 홈으로 이동한다. 진입 시마다 최신 토큰으로 재로드.
 *
 * 웹앱 연동 계약(웹 측 가이드):
 * - 진입: ${webAppUrl}/token-signin/{token} — 토큰은 path 파라미터, 로그·히스토리 노출 금지
 * - JS 브리지: addJavascriptInterface(obj, "android") — onClose()/errorClose()/logout()
 * - webapp 모드에서 app(-staging).pulleymath.com 외 도메인은 Custom Tab으로
 * - UA 마커 "PulleyWebView/1.0"로 웹 측이 웹뷰 분기 처리
 *
 * 화면 방향: 앱 전체는 landscape 고정이나, 이 화면만 웹 반응형에 맞춰
 * 모바일(폰)=portrait / 태블릿=landscape로 동작한다.
 */
class AiepWebViewActivity : AppCompatActivity() {

    private val viewModel: AiepWebViewViewModel by viewModels()

    private lateinit var rootView: FrameLayout
    private lateinit var webView: WebView
    private lateinit var errorView: LinearLayout

    private var mode: String = MODE_LOGIN
    private var lastBackAt = 0L

    // 영상 전체화면(onShowCustomView) 상태
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            filePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
            filePathCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱 전체는 landscape 고정이지만, AIEP 웹뷰는 웹 반응형에 맞춰
        // 모바일(폰)=portrait / 태블릿=landscape로 동작한다.
        // 판별은 smallestWidth(sw600dp) 기반 R.bool.isPortrait — 현재 화면 방향과
        // 무관한 값이라, 앞 화면들이 강제한 landscape 때문에 폰이 태블릿으로 오인되지 않는다.
        requestedOrientation = if (resources.getBoolean(R.bool.isPortrait)) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        setContentView(R.layout.activity_aiep_web_view)

        rootView = findViewById(R.id.aiep_root)
        webView = findViewById(R.id.aiep_web_view)
        errorView = findViewById(R.id.aiep_error_view)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val tappableBottom = insets.getInsets(WindowInsetsCompat.Type.tappableElement()).bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, tappableBottom)
            insets
        }

        mode = savedInstanceState?.getString(STATE_MODE)
            ?: intent.getStringExtra(EXTRA_MODE)
            ?: MODE_LOGIN

        setupWebView()
        setupBackNavigation()
        observeViewModel()
        findViewById<Button>(R.id.aiep_retry_btn).setOnClickListener { retry() }

        // 웹 세션은 localStorage 기반으로 유지되지만, 토큰 갱신 정책상
        // 진입 시마다 최신 토큰으로 다시 로드한다 (웹 측 가이드 권장)
        loadByMode()
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true // 웹 세션이 localStorage 기반 — 필수
            textZoom = 100 // 시스템 글꼴 크기에 따른 레이아웃 깨짐 방지
            mediaPlaybackRequiresUserGesture = false // 개념학습 영상(HLS) 자동재생
            userAgentString = "$userAgentString $WEBVIEW_UA_MARKER"
            // 보안 기본값 유지: mixed content 차단, 파일 접근 비활성화
            allowFileAccess = false
            allowContentAccess = false
        }
        CookieManager.getInstance().setAcceptCookie(true)
        webView.addJavascriptInterface(AndroidBridge(), JS_BRIDGE_NAME)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                return handleNavigation(uri)
            }

            // SPA(pushState) 라우팅은 shouldOverrideUrlLoading에 걸리지 않으므로 여기서 감지
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                if (mode == MODE_WEBAPP && url?.let { Uri.parse(it).path } == UNAUTHORIZED_PATH) {
                    onSessionExpired()
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    Log.e(TAG, "onReceivedError url=${sanitize(request.url)} code=${error?.errorCode} desc=${error?.description}")
                    showError()
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val intent = fileChooserParams?.createIntent() ?: return false
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    filePathCallback = null
                    false
                }
            }

            // 영상·유튜브 전체화면 버튼 대응
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                rootView.addView(
                    view,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
                webView.isVisible = false
                setSystemBarsVisible(false)
            }

            override fun onHideCustomView() {
                hideCustomView()
            }
        }
    }

    private fun hideCustomView() {
        val view = customView ?: return
        rootView.removeView(view)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        webView.isVisible = true
        setSystemBarsVisible(true)
    }

    private fun setSystemBarsVisible(visible: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            when {
                customView != null -> hideCustomView() // 전체화면 영상 닫기
                webView.canGoBack() -> webView.goBack()
                mode == MODE_WEBAPP -> {
                    // webapp 모드는 사실상 메인 화면: 두 번 눌러 종료
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastBackAt < BACK_EXIT_INTERVAL_MS) {
                        finishAffinity()
                    } else {
                        lastBackAt = now
                        Toast.makeText(this@AiepWebViewActivity, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> finish() // 로그인 모드: 네이티브 로그인 화면으로 복귀
            }
        }
    }

    private fun observeViewModel() {
        viewModel.errorAction.observe(this) { type ->
            when (type) {
                CoroutineExceptionType.NONE, CoroutineExceptionType.Cancellation -> Unit
                else -> {
                    Log.e(TAG, "errorAction=$type mode=$mode")
                    showError()
                }
            }
        }
    }

    private fun loadByMode() {
        hideError()
        when (mode) {
            MODE_WEBAPP -> loadWebApp()
            else -> webView.loadUrl("${Network.homePageUrl}$AIEP_LINK_PATH")
        }
    }

    private fun loadWebApp() {
        val token = MyApplication.token
        if (token.isNullOrEmpty()) {
            onSessionExpired()
            return
        }
        // 주의: 토큰이 URL path에 포함되므로 이 URL은 로그에 남기지 않는다
        webView.loadUrl("${Network.webAppUrl}$TOKEN_SIGNIN_PATH/$token")
    }

    /** @return true면 WebView 로드를 막고 네이티브에서 처리 */
    private fun handleNavigation(uri: Uri): Boolean {
        // 1) 교육청 로그인 완료 복귀 URL → 토큰 인터셉트
        extractAiepToken(uri)?.let { token ->
            onLoginTokenReceived(token)
            return true
        }
        if (uri.scheme == "http" || uri.scheme == "https") {
            return when (mode) {
                MODE_WEBAPP -> handleWebAppNavigation(uri)
                // 로그인 모드: 교육청 SSO 리다이렉트(pulleymath.com → IdP → SP)가
                // 여러 도메인을 오가므로 모두 WebView 안에서 진행
                else -> false
            }
        }
        // http/https 외 스킴은 외부 앱으로
        openExternal(uri)
        return true
    }

    private fun handleWebAppNavigation(uri: Uri): Boolean {
        // 인증 실패 시 웹이 /unauthorized로 이동 → 세션 만료 처리
        if (uri.path == UNAUTHORIZED_PATH) {
            onSessionExpired()
            return true
        }
        // webapp 도메인만 WebView 안에서 서빙, 그 외(마케팅 사이트, 유튜브 등)는 Custom Tab으로
        if (isWebAppHost(uri)) return false
        openCustomTab(uri)
        return true
    }

    private fun isWebAppHost(uri: Uri): Boolean {
        val webAppHost = Uri.parse(Network.webAppUrl).host ?: return false
        return uri.host == webAppHost
    }

    private fun extractAiepToken(uri: Uri): String? {
        val token = runCatching { uri.getQueryParameter("token") }.getOrNull()
        if (token.isNullOrEmpty()) return null
        val isAppScheme = uri.scheme == APP_SCHEME
        val isAiepPath = uri.path?.startsWith(AIEP_LINK_PATH) == true
        val isAiepTarget = runCatching { uri.getQueryParameter("target") }.getOrNull() == "aiep"
        // 토큰은 앱 스킴 또는 pulleymath.com 도메인에서만 수락 (외부 사이트의 위조 URL 차단)
        return if (isAppScheme || (isPulleyHost(uri) && (isAiepPath || isAiepTarget))) token else null
    }

    private fun isPulleyHost(uri: Uri): Boolean {
        val host = uri.host ?: return false
        return host == "pulleymath.com" || host.endsWith(".pulleymath.com")
    }

    private fun onLoginTokenReceived(token: String) {
        Log.d(TAG, "onLoginTokenReceived")
        viewModel.signInWithToken(token) {
            putFcmToken()
            mode = MODE_WEBAPP
            loadWebApp()
        }
    }

    private fun onSessionExpired() {
        Log.d(TAG, "onSessionExpired")
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user?.commit(TAG)
        startActivity(Intent(this, StartActivity::class.java))
        finishAffinity()
    }

    /**
     * 웹앱 프로필 메뉴의 로그아웃 처리.
     * 웹이 서버 로그아웃 API 호출과 웹 세션(localStorage) 정리를 모두 마친 뒤 호출하므로,
     * 네이티브는 서버를 다시 호출하지 않고 자체 보관 중인 세션·자동 로그인 정보만 정리한 뒤
     * 앱 시작 화면(StartActivity)으로 복귀한다. (네이티브 로그아웃과 동일한 정리 범위)
     */
    private fun onLoggedOut() {
        Log.d(TAG, "onLoggedOut")
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user = null
        MyApplication.isAppFirstLaunch = true
        Preferences.userDataString.set("")
        startActivity(Intent(this, StartActivity::class.java))
        finishAffinity()
    }

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "openExternal failed uri=$uri")
        }
    }

    private fun openCustomTab(uri: Uri) {
        try {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
        } catch (e: ActivityNotFoundException) {
            openExternal(uri)
        }
    }

    /** 토큰이 포함될 수 있는 URL의 로그 노출 방지 */
    private fun sanitize(uri: Uri): String {
        val path = uri.path.orEmpty()
        val safePath = if (path.startsWith(TOKEN_SIGNIN_PATH)) "$TOKEN_SIGNIN_PATH/***" else path
        return "${uri.host}$safePath"
    }

    private fun putFcmToken() {
        if (MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                val token = task.result
                if (task.isSuccessful && token?.isNotEmpty() == true) {
                    viewModel.putFcmToken(token)
                }
            }
        }
    }

    private fun retry() {
        hideError()
        // webapp 모드는 항상 최신 토큰으로 재진입
        if (mode == MODE_WEBAPP || webView.url.isNullOrEmpty()) loadByMode() else webView.reload()
    }

    private fun showError() {
        errorView.isVisible = true
    }

    private fun hideError() {
        errorView.isVisible = false
    }

    // singleTask: 홈 → 재진입 시 Splash가 다시 webAppIntent를 보내도
    // 기존 인스턴스가 재사용된다. 보던 페이지를 유지하고 불필요한 재로그인 로드를 막는다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_LOGIN
        val isBrowsingWebApp = mode == MODE_WEBAPP && !webView.url.isNullOrEmpty() && !errorView.isVisible
        if (newMode == MODE_WEBAPP && isBrowsingWebApp) {
            return // 이미 webapp 사용 중 — 보던 화면 그대로 유지
        }
        mode = newMode
        loadByMode()
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush() // 인증 쿠키(auth-token) 영속화
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_MODE, mode)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    /** 웹앱 연동 계약: window.android.onClose()/errorClose()/logout() */
    private inner class AndroidBridge {
        @JavascriptInterface
        fun onClose() {
            Log.d(TAG, "bridge onClose")
            runOnUiThread { finish() }
        }

        @JavascriptInterface
        fun errorClose() {
            Log.d(TAG, "bridge errorClose")
            runOnUiThread { onSessionExpired() }
        }

        // 웹이 서버 로그아웃·웹 세션 정리를 마친 뒤 호출 → 네이티브 세션 정리 후 시작 화면으로 복귀.
        // JS 스레드에서 호출되므로 화면 전환은 UI 스레드로 넘긴다.
        @JavascriptInterface
        fun logout() {
            Log.d(TAG, "bridge logout")
            runOnUiThread { onLoggedOut() }
        }
    }

    companion object {
        private const val TAG = "AiepWebViewActivity"

        private const val EXTRA_MODE = "EXTRA_MODE"
        private const val STATE_MODE = "STATE_MODE"
        private const val MODE_LOGIN = "login"
        private const val MODE_WEBAPP = "webapp"

        private const val APP_SCHEME = "pulleymath"
        private const val AIEP_LINK_PATH = "/link/aiep"

        // 웹앱 연동 계약 (웹 측 가이드와 협의된 값)
        private const val TOKEN_SIGNIN_PATH = "/token-signin"
        private const val UNAUTHORIZED_PATH = "/unauthorized"
        private const val JS_BRIDGE_NAME = "android"
        private const val WEBVIEW_UA_MARKER = "PulleyWebView/1.0"

        private const val BACK_EXIT_INTERVAL_MS = 2500L

        /** 교육청 로그인부터 시작 (로그인 화면의 aiep 버튼 진입점) */
        fun loginIntent(context: Context): Intent =
            Intent(context, AiepWebViewActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_LOGIN)

        /** 이미 검증된 세션으로 webapp 진입 (딥링크 검증 후 / 자동로그인) */
        fun webAppIntent(context: Context): Intent =
            Intent(context, AiepWebViewActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_WEBAPP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}
