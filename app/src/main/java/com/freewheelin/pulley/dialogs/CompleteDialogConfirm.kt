package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_complete_confirm.*

class CompleteDialogConfirm(context: Context, val title: String, val guide: String): Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_complete_confirm)
        titleTv.text = title
        guideTv.text = guide
        confirmBtn.setOnClickListener {
            dismiss()
        }
    }
}