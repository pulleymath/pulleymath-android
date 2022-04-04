package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogCompleteBinding

class CompleteDialog(context: Context, val title: String, val guide: String): Dialog(context) {
    val binding: DialogCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_complete, null, false)
    }
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        binding.titleTv.text = title
        binding.guideTv.text = guide
    }


    fun showFor(duration: Long = 2000, cb: (() -> Unit)? = null) {
        show()
        Handler().postDelayed({
            dismiss()
            if (cb != null)
                cb()
        }, duration)
    }
}