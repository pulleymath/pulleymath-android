package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogBannerBinding

class BannerDialog(context: Context, title: String): Dialog(context) {
    val binding: DialogBannerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_banner, null, false)
    }
    init {
        setContentView(binding.root)
        binding.titleTv.text = title
        binding.rightBtn.setOnClickListener { dismiss() }
        binding.leftBtn.setOnClickListener { dismiss() }
    }

}
