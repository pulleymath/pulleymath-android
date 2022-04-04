package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogCompleteConfirmBinding

class CompleteDialogConfirm(context: Context, val title: String, val guide: String): Dialog(context) {

    val binding: DialogCompleteConfirmBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_complete_confirm, null, false)
    }
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_complete_confirm)
        binding.titleTv.text = title
        binding.guideTv.text = guide
        binding.confirmBtn.setOnClickListener {
            dismiss()
        }
    }
}