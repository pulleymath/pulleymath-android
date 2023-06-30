package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.marketing

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.underMinHeight
import com.freewheelin.pulley.legacy.core.manage.PieceManager
import com.freewheelin.pulley.databinding.DialogMarketingBinding
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.IntentUtils
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import java.lang.Exception

class MarketingDialog(context: Context, val marketing:Marketing): Dialog(context), MarketingPager.BannerInterface {

    private val binding: DialogMarketingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_marketing, null, false)
    }
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        initUI()
    }


    private fun initUI() {
        Log.d("마케팅", "data=$marketing")
        binding.apply {

            val pagerAdapter = MarketingPager(marketing.banners, this@MarketingDialog)
            marketingPager.adapter = pagerAdapter

            if (marketing.banners.size > 1) {
                TabLayoutMediator(pagerIndicator, marketingPager) { tab, position ->
                    tab.setIcon(R.drawable.banner_tab_selector)
                }.attach()
            }
            Log.d("마케팅", "screen height=${context.resources.configuration.screenHeightDp}")

            val customRatio = 0.7f
            val pagerHeight = DisplayUtils.getScreenHeight(context) * customRatio

            var pagerParams = marketingPager.layoutParams
            pagerParams.height = pagerHeight.toInt()
            pagerParams.width = (pagerHeight / 560 * 720).toInt()

            var buttomParams = marketingButtonLayout.layoutParams
            buttomParams.width = (pagerHeight / 560 * 720).toInt()
            buttomParams.height = (pagerHeight / 560 * 64).toInt()

            btnMarketingClose.setOnClickListener {
                dismiss()
            }

            btnMarketingNoShow.setOnClickListener {
                MarketingManager.setNoShow(context!!, marketing)
                dismiss()
            }
        }
    }

    override fun openBanner(urlString: String) {
        binding.loadingContainer.visibility = View.VISIBLE

        if(urlString.startsWith("http")) {
            IntentUtils.openWebLink(context, urlString, context.packageManager)

            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                binding.loadingContainer.visibility = View.GONE
                dismiss()
            }
        } else if(urlString.startsWith("tabindex://")){
            val tabIndex = urlString.replace("tabindex://","").toInt()
            val intent = Intent(PieceManager.EVENT_MOVE_TAB)
            intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, tabIndex)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

            CoroutineScope(Dispatchers.Main).launch {
                binding.loadingContainer.visibility = View.GONE
                dismiss()
            }
        }
    }
}