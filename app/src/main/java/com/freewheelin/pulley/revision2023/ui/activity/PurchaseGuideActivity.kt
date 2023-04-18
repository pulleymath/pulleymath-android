package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.isMobile
import com.freewheelin.pulley.bases.isMobileUI
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityPurchaseGuideBinding
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide2Fragment
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.*

class PurchaseGuideActivity : AppCompatActivity() {
    private val viewModel: PurchaseGuideViewModel by viewModels()

    val binding: ActivityPurchaseGuideBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_purchase_guide, null, false)
    }
    private lateinit var getResult: ActivityResultLauncher<Intent>

    companion object {
        const val purchaseSuccess = 200
        const val common = 400
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, PurchaseGuideActivity::class.java).apply {
//                putExtra(OFFER_ID, offerId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(binding.root)
        addBackBtnCallback()
        initActivityResult()

        binding.apply {
            lifecycleOwner = this@PurchaseGuideActivity
            viewModel.fetchGuides()

            initUiIfTablet()
            initUiIfMobile()
            compareTv.setOnClickListener {
                viewModel.step.postValue(1)
            }
            backBtn.setOnClickListener {
                onBackBtn()
            }
        }
        viewModel.apply {
            guideOffers.observe(this@PurchaseGuideActivity) {
                it.forEach { offer ->
                    binding.apply {
                        val view = when (offer.productSubType) {
                            PaidServiceType.BASIC_C -> if(isTablet) basicCIv else mobileBasicCIv
                            PaidServiceType.BASIC_P -> if(isTablet) basicPIv else mobileBasicPIv
                            PaidServiceType.STANDARD -> if(isTablet) standardIv else mobileStandardIv
                            PaidServiceType.PREMIUM -> if(isTablet) premiumIv else mobilePremiumIv
                            else -> null
                        }
                        view?.let { iv ->
                            Glide.with(this@PurchaseGuideActivity)
                                .load(offer.imageUrl)
                                .into(iv)
                        }
                    }
                }
            }
            backgroundColor.observe(this@PurchaseGuideActivity) {
                binding.apply {
                    val color = Color.parseColor("#${it}")
                    val view = if (isTablet) { bodyCl } else { backgroundTopView }
                    view.setBackgroundColor(color)
                    view.invalidate()
                }
            }
            step.observe(this@PurchaseGuideActivity) { step ->
                when (step) {
                    0 -> {
                        binding.apply {
                            bodyCl.visibleIf(isTablet)
                            mobileScrollRoot.visibleIf(isMobile)
                            compareTv.visibleIf(true)
                            compareFl.hide { }
                            compareScrollView.hide {  }
                        }
                    }
                    1 -> {
                        binding.apply {
                            bodyCl.visibleIf(false)
                            mobileScrollRoot.visibleIf(false)
                            compareTv.visibleIf(false)
                            if (isTablet) {
                                compareFl.showTransition(300, ViewTransition.SlideFromRight)
                            } else {
                                compareScrollView.showTransition(300, ViewTransition.SlideFromRight)
                            }
                        }

                    }
                    else -> {}
                }
            }
        }
    }

    private fun initUiIfTablet() = with(binding) {
        if (isTablet) {
            premiumClickView.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.PREMIUM) }
            standardIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.STANDARD) }
            basicPIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.BASIC_P) }
            basicCIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.BASIC_C) }
        }
    }
    private fun initUiIfMobile() = with(binding) {
        if (isMobile) {
            mobilePremiumClickView.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.PREMIUM) }
            mobileStandardIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.STANDARD) }
            mobileBasicPIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.BASIC_P) }
            mobileBasicCIv.setOnClickListener { openPurchaseGuideWebView(PaidServiceType.BASIC_C) }
        }
    }

    private fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            onBackBtn()
        }
    }
    private fun onBackBtn() {
        if (viewModel.step.value == 0) {
            finish()
        } else {
            viewModel.step.postValue(0)
        }
    }
    private fun openPurchaseGuideWebView(type: PaidServiceType) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "그랜드오픈2023", "구독제상품선택", "클릭 $type")

        viewModel.guideOffers.value?.find { it.productSubType == type }?.let {
            getResult.launch(PurchaseWebViewActivity.getIntent(this@PurchaseGuideActivity, it.offerId))
        }
    }
    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(it.resultCode) {
                purchaseSuccess -> {
                    finish()
                }
                common -> {}
            }
        }
    }
    private fun hideSystemUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

}