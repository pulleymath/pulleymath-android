package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_banner.*

class BannerDialog(context: Context, title: String): Dialog(context) {
    init {
        setContentView(R.layout.dialog_banner)
        titleTv.text = title
        rightBtn.setOnClickListener { dismiss() }
        leftBtn.setOnClickListener { dismiss() }
    }

}
