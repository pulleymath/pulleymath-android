package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.affiliatedTest.component.CommunityJavascriptInterface
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityPurchaseWebViewBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseGuideActivity.Companion.purchaseSuccess
import com.freewheelin.pulley.revision2023.utils.listeners.WebClientFinishClickEventListener
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseWebViewModel
import com.freewheelin.pulley.utils.DialogUtils
import java.io.File
import java.lang.Exception

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

        binding.backBtn.setOnClickListener {
            setResult(PurchaseGuideActivity.common, intent)
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
//                allowFileAccess = true
            }
            addJavascriptInterface(WebClientFinishClickEventListener {
                setResult(purchaseSuccess)
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
}