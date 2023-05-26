package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewToastBinding
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx

class DaebakToastView: ConstraintLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var binding: ViewToastBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_toast, this, true)

    init {

    }

    fun setBackground(res: Int) {
        binding.toastTv.setBackgroundResource(res)
    }

    var text: String
        set(value) {
            binding.toastTv.text = value
        }
        get() {
            return binding.toastTv.text.toString()
        }
}

class DaebakToast: PopupWindow() {

    companion object {
        var startAnimator: ValueAnimator? = null
        var hideAnimator: ValueAnimator? = null

        var alreadyShownWindow: PopupWindow? = null

        fun showFailedMakePiece(context: Context) {
            show(context, "해당 유형의 유사문제는 준비 중입니다.ㅠㅠ")
        }

        fun show(context: Context, text: String,
                 leftOffset:Int = 24.toPx(),
                 bottomOffset: Int = 24.toPx(),
                 bg: Int = R.drawable.bg_gray_e6818181_round,
                 overDialog: Boolean = false) {
            startAnimator?.cancel()
            hideAnimator?.cancel()
            if(alreadyShownWindow != null)
                alreadyShownWindow?.dismiss()

            val toastView = DaebakToastView(context)
            toastView.text = text
            toastView.setBackground(bg)

            toastView.measure(0,0)
            val popupWindow = makeWindow(context, toastView, leftOffset, bottomOffset, overDialog)

            alreadyShownWindow = popupWindow

            var originX = (toastView.measuredWidth  * -1).toFloat()
            startAnimator = ValueAnimator.ofFloat(originX, leftOffset.toFloat())
            startAnimator!!.addUpdateListener {
                val value = it.animatedValue as Float
                toastView.translationX = value
            }

            startAnimator!!.duration = 300
            startAnimator!!.interpolator = AccelerateInterpolator(1.5f)
            startAnimator!!.start()
            alreadyShownWindow = popupWindow
            startAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    hideToast(3000, leftOffset)
                }
            })
        }

        private fun makeWindow(context: Context, toastView: DaebakToastView, leftOffset: Int, bottomOffset: Int, overDialog: Boolean): PopupWindow {
            val popupWindow = PopupWindow(toastView, toastView.measuredWidth + leftOffset, toastView.measuredHeight)
            if(overDialog) {
                popupWindow.windowLayoutType = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
            }

            popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))
            val screenHeight = DisplayUtils.getScreenHeight(context)

            try {
                popupWindow.showAtLocation(toastView, Gravity.NO_GRAVITY, 0, screenHeight - toastView.measuredHeight - bottomOffset)
            } catch (error: Exception) {
                Log.e(javaClass.simpleName, "error=$error")
            }

            return popupWindow
        }
        private fun hideToast(delay: Long = 0, leftOffSet: Int) {
            val alreadyShownWindow  = alreadyShownWindow ?: return

            hideAnimator = ValueAnimator.ofFloat(leftOffSet.toFloat(), - alreadyShownWindow.width.toFloat())
            hideAnimator!!.addUpdateListener {
                val value = it.animatedValue as Float
                alreadyShownWindow.contentView.translationX = value
            }

            hideAnimator!!.duration = 300
            hideAnimator!!.interpolator = AccelerateInterpolator(1.5f)
            hideAnimator!!.startDelay = delay
            hideAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    alreadyShownWindow.dismiss()
                    this@Companion.alreadyShownWindow = null
                }
            })
            hideAnimator!!.start()
        }
    }
}