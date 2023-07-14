package com.freewheelin.pulley.revision2023.ui.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityPurchaseWebViewBinding
import com.freewheelin.pulley.databinding.ActivityWhaleSpaceLoginWebViewBinding
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.affiliatedTest.component.CommunityJavascriptInterface
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.utils.listeners.WebClientFinishClickEventListener
import java.io.File
import java.net.URISyntaxException

class WhaleSpaceLoginWebViewActivity : AppCompatActivity() {

    val binding: ActivityWhaleSpaceLoginWebViewBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_whale_space_login_web_view, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            webView.apply {

//                webChromeClient = object : WebChromeClient() {
//                    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
//                        val alertMessage = message ?: " --- "
//                        DialogUtils.purchaseAlertDialog(this@WhaleSpaceLoginWebViewActivity, alertMessage,
//                            { result?.cancel() }, { result?.confirm() })
//                        return true
//                    }
//
//                    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
//                        val alertMessage = message ?: "등록된 카드로 결제하시겠습니까?"
//                        DialogUtils.purchaseConfirmDialog(this@WhaleSpaceLoginWebViewActivity, alertMessage,
//                            { result?.cancel() }, { result?.confirm() })
//                        return true
//                    }
//                }
                settings.apply {
                    javaScriptEnabled = true
//                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
//                    allowFileAccess = true
                    val agent = userAgentString.replace("Android", "APPNAME Android")
                    userAgentString = agent

//                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                    val cookieManager = CookieManager.getInstance()
//                    cookieManager.setAcceptCookie(true)
//                    cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
                }

//                addJavascriptInterface(WebClientFinishClickEventListener {
//                    val purchaseReceiverIntent = Intent(PurchaseWebViewActivity.PURCHASE_SUCCESS)
//                    LocalBroadcastManager.getInstance(this@WhaleSpaceLoginWebViewActivity).sendBroadcast(purchaseReceiverIntent)
//                    val profileReceiverIntent = Intent(UserManager.EVENT_USER_MODIFYING)
//                    LocalBroadcastManager.getInstance(this@WhaleSpaceLoginWebViewActivity).sendBroadcast(profileReceiverIntent)
//                    finish()
//                }, "androidInterface")

//                viewModel.getTempToken { shortToken ->
//
// /                }

                webViewClient = WebViewClient()
                loadUrl("https://auth.whalespace.io/oauth2/v1.1/authorize?response_type=code&client_id=HGooZch3UpTdhnKgH_5o&redirect_uri=http://100.100.8.51:9000/v1/signin/oauth/code/whalespace&state=fjdkslfjs238198942")


//                addJavascriptInterface(CommunityJavascriptInterface(this@PurchaseWebViewActivity), "AndroidFunction");
            }
        }

    }
}