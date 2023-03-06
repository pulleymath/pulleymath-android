package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.affiliatedTest.component.CommunityJavascriptInterface
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityPurchaseWebViewBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide2Fragment
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseWebViewModel
import com.freewheelin.pulley.utils.Preferences

class PurchaseWebViewActivity : AppCompatActivity() {

    val binding: ActivityPurchaseWebViewBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_purchase_web_view, null, false)
    }
    private val viewModel: PurchaseWebViewModel by viewModels()

    companion object {
//        const val FOCUS_ON_TOTAL_LABEL = "FOCUS_ON_TOTAL_LABEL"
//        const val CHALLENGE_PATTERN_FINISHED = 302
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, PurchaseWebViewActivity::class.java).apply {
//                putExtra(FOCUS_ON_TOTAL_LABEL, isFocus)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val token = user?.token ?: "-1"

        val API_PURCHASE_DOMAIN = when (Preferences.onServerAPI.get()) {
            Network.Server.live.toString() -> "https://pulleymath.com?token=${token}"
            Network.Server.staging.toString() -> "https://dev.pulleymath.com?token=${token}"
            Network.Server.dev.toString() -> "https://dev.pulleymath.com?token=${token}"
            else -> "https://pulleymath.com"
        }

        binding.backBtn.setOnClickListener {
            setResult(PurchaseGuide2Fragment.common, intent)
            finish()
        }
        binding.webView.apply {
//            webViewClient
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
//                allowFileAccess = true
            }
            loadUrl(API_PURCHASE_DOMAIN)

            // TODO interface를 통해서 success를 날려주면 좋을것같음
            addJavascriptInterface(CommunityJavascriptInterface(this@PurchaseWebViewActivity), "AndroidFunction");
        }
    }
}