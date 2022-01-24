package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import kotlinx.android.synthetic.main.dialog_pulley_plus_price.*

class PulleyPlusPriceDialog: Dialog {

    constructor(context: Context): super(context) {
        setContentView(R.layout.dialog_pulley_plus_price)

        closeBtn.setOnClickListener {
            dismiss()
        }

        Glide.with(context)
            .load(URL.풀리플러스가격이미지)
            .into(priceIv)

        buyBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.풀리플러스구매)
            context.startActivity(intent)
        }
    }
}