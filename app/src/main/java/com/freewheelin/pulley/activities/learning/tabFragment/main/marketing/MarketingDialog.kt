package com.freewheelin.pulley.activities.learning.tabFragment.main.marketing

import android.app.Dialog
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
import com.freewheelin.pulley.bases.underMinHeight
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.databinding.DialogMarketingBinding
import com.freewheelin.pulley.utils.toPx
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

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

            if (context.underMinHeight) {
                val ratio = 0.5f

                var pagerParams = marketingPager.layoutParams
                pagerParams.width = (720 * ratio).toPx().toInt()
                pagerParams.height = (560 * ratio).toPx().toInt()

                var buttomParams = marketingButtonLayout.layoutParams
                buttomParams.width = (720 * ratio).toPx().toInt()
                buttomParams.height = (64 * ratio).toPx().toInt()
            }

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
            val uri = Uri.parse(urlString)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)

            CoroutineScope(Dispatchers.Default).launch {
                delay(2000)
                withContext(Dispatchers.Main) {
                    binding.loadingContainer.visibility = View.GONE
                    dismiss()
                }
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