package com.freewheelin.pulley.utils

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.View
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_timer.*

interface AnimationListener {
    fun onAnimationEnd() {

    }

    fun onAnimationCancel() {

    }
}
class AnimationUtils {
    companion object {
        fun showTimer(context: Context, count: Int, endText: String, listener: AnimationListener? = null) {
            var dialog = TimerDialog(context, count, endText)
            dialog.listener = listener
            dialog.show()
        }
    }

}

class TimerDialog: Dialog {
    var listener: AnimationListener? = null

    constructor(context: Context, count: Int=3 , endText: String): super(context, R.style.CustomBackgroundDialog) {
        setContentView(R.layout.dialog_timer)

        endTv.text = "준비~"
        endTv.visibility = View.VISIBLE

        timerLottie.repeatCount = 0
        timerTv.visibility = View.INVISIBLE

        timerLottie.progress = 1f


        val handler = Handler()

        handler.postDelayed({
            timerLottie.playAnimation()
        }, 1500)

        timerLottie.addAnimatorListener(object: Animator.AnimatorListener{
            override fun onAnimationCancel(p0: Animator?) {
                listener?.onAnimationCancel()
                timerLottie.removeAllAnimatorListeners()
            }

            override fun onAnimationStart(p0: Animator?) {
                this@TimerDialog.endTv.text = endText
                this@TimerDialog.timerTv.visibility = View.GONE
                this@TimerDialog.endTv.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                this@TimerDialog.dismiss()
                listener?.onAnimationEnd()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        timerLottie.cancelAnimation()
    }
}