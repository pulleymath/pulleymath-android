package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_complete.*

class CompleteDialog(context: Context, val title: String, val guide: String): Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_complete)
        titleTv.text = title
        guideTv.text = guide
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