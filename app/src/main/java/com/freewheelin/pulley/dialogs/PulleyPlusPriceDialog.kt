package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast

class PulleyPlusPriceDialog(context: Context) : Dialog(context) {

    var closeBtn: ImageButton
    var priceIv: ImageView
    var buyBtn: Button
    init {
        setContentView(R.layout.dialog_pulley_plus_price)
        closeBtn = findViewById(R.id.closeBtn)
        priceIv = findViewById(R.id.priceIv)
        buyBtn = findViewById(R.id.buyBtn)

        closeBtn.setOnClickListener {
            dismiss()
        }
        Glide.with(context)
            .load(URL.풀리플러스가격이미지)
            .into(priceIv)
        buyBtn.setOnClickListener {
            IntentUtils.openWebLink(context, URL.풀리플러스다이얼로그구매, context.packageManager)
        }
    }
}