package com.freewheelin.pulley.legacy.dialogs

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.show

interface LottieDialogListener {
    fun onDismissDialog()
}
class LottieDialog(context: Context, val text: String, val lottieFile: String): Dialog(context) {
    var listener: LottieDialogListener? = null

    var lottie: LottieAnimationView
    var guideTv: TextView
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_lottie)
        setCancelable(false)
        lottie = findViewById(R.id.lottie)
        guideTv = findViewById(R.id.guideTv)

        lottie.setAnimation(lottieFile)
        lottie.playAnimation()
        guideTv.visibility = View.INVISIBLE
        lottie.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                dismiss()
                listener?.onDismissDialog()
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationStart(p0: Animator) {}
        })
        guideTv.text = text
        guideTv.show(300)
    }
}