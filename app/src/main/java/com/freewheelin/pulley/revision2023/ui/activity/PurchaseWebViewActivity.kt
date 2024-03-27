package com.freewheelin.pulley.revision2023.ui.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.assessment.component.CommunityJavascriptInterface
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityPurchaseWebViewBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.utils.listeners.WebClientFinishClickEventListener
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseWebViewModel
import com.freewheelin.pulley.legacy.utils.DialogUtils
import java.io.File
import java.net.URISyntaxException


class PurchaseWebViewActivity : AppCompatActivity() {

    val binding: ActivityPurchaseWebViewBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_purchase_web_view, null, false)
    }
    private val viewModel: PurchaseWebViewModel by viewModels()

    companion object {
        const val OFFER_ID = "OFFER_ID"
        const val PURCHASE_SUCCESS = "PURCHASE_SUCCESS"

        @JvmStatic
        fun getIntent(context: Context, offerId: Int): Intent {
            return Intent(context, PurchaseWebViewActivity::class.java).apply {
                putExtra(OFFER_ID, offerId)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(binding.root)
        val offerId = intent.getIntExtra(OFFER_ID, -1)
        val encodedUri = "/shop/${offerId}/plus"
        addBackBtnCallback()
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.webView.apply {
            webViewClient = object: WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    clearHistory();
                    clearCache(true);
                    clearCache(this@PurchaseWebViewActivity)
                    super.onPageFinished(view, url)
                }
                fun clearCache(context: Context, file: File? = null) {
                    var dir: File? = file ?: this@PurchaseWebViewActivity.cacheDir ?: return
                    val children = dir?.listFiles()
                    try {
                        children?.forEach {
                            if (it.isDirectory) { clearCache(context, it) }
                            else { it.delete() }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (!URLUtil.isNetworkUrl(url) && !URLUtil.isJavaScriptUrl(url)) {
                        val uri = try {
                            Uri.parse(url)
                        } catch (e: Exception) {
                            return false
                        }

                        return when (uri.scheme) {
                            "intent" -> {
                                startSchemeIntent(url)
                            }

                            else -> {
                                return try {
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    true
                                } catch (e: Exception) {
                                    false
                                }
                            }
                        }
                    } else {
                        return false
                    }
                }

                private fun startSchemeIntent(url: String): Boolean {
                    val schemeIntent: Intent = try {
                        Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    } catch (e: URISyntaxException) {
                        return false
                    }
                    try {
                        startActivity(schemeIntent)
                        return true
                    } catch (e: ActivityNotFoundException) {
                        val packageName = schemeIntent.getPackage()

                        if (!packageName.isNullOrBlank()) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$packageName")
                                )
                            )
                            return true
                        }
                    }
                    return false
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    val alertMessage = message ?: " --- "
                    DialogUtils.purchaseAlertDialog(this@PurchaseWebViewActivity, alertMessage,
                        { result?.cancel() }, { result?.confirm() })
                    return true
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    val alertMessage = message ?: "등록된 카드로 결제하시겠습니까?"
                    DialogUtils.purchaseConfirmDialog(this@PurchaseWebViewActivity, alertMessage,
                        { result?.cancel() }, { result?.confirm() })
                    return true
                }
            }
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                allowFileAccess = true
                val agent = userAgentString.replace("Android", "APPNAME Android")
                userAgentString = agent

                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
            }

            addJavascriptInterface(WebClientFinishClickEventListener {
                val purchaseReceiverIntent = Intent(PURCHASE_SUCCESS)
                LocalBroadcastManager.getInstance(this@PurchaseWebViewActivity).sendBroadcast(purchaseReceiverIntent)
                val profileReceiverIntent = Intent(UserManager.EVENT_USER_MODIFYING)
                LocalBroadcastManager.getInstance(this@PurchaseWebViewActivity).sendBroadcast(profileReceiverIntent)
                finish()
            }, "androidInterface")

            viewModel.getTempToken { shortToken ->
                val purchaseUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&is_mobile=true&uri=${encodedUri}"
                loadUrl(purchaseUrl)
            }


            // TODO interface를 통해서 success를 날려주면 좋을것같음
            addJavascriptInterface(CommunityJavascriptInterface(this@PurchaseWebViewActivity), "AndroidFunction");
        }
    }

    private fun hideSystemUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    var backBtnTime: Long = 0
    fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            val webView = binding.webView
            val curTime = System.currentTimeMillis()
            val gapTime: Long = curTime - backBtnTime
            if (webView.canGoBack()) {
                webView.goBack()
            } else if (gapTime in 0..2500) {
                finish()
            } else {
                backBtnTime = curTime
                Toast.makeText(this@PurchaseWebViewActivity, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}