package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.databinding.ViewSuccessToastBinding
import com.freewheelin.pulley.utils.DisplayUtils

class SuccessToast: PopupWindow() {
    companion object {
        var startAnimator: ValueAnimator? = null
        var hideAnimator: ValueAnimator? = null
        var alreadyShownWindow: PopupWindow? = null

        fun show(context: Context, title: String, contents: String) {
            startAnimator?.cancel()
            hideAnimator?.cancel()
            if(alreadyShownWindow != null)
                alreadyShownWindow?.dismiss()

            val toastView = SuccessToastView(context)

            toastView.title = title
            toastView.contents = contents
            toastView.setOnClickListener {
                hideToast(context, 500)
            }
            toastView.measure(0,0)
//            toastView.xBtn.setOnClickListener {
//                hideToast(context, 0)
//            }
            val popupWindow = PopupWindow(toastView, toastView.measuredWidth, toastView.measuredHeight)

            popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))

            val offsetY = DisplayUtils.getScrenHeight(context) / 4

            val screenWidth = DisplayUtils.getScreenWidth(context)

            popupWindow.showAtLocation(toastView, Gravity.NO_GRAVITY, screenWidth - toastView.measuredWidth, offsetY)

            alreadyShownWindow = popupWindow

            val originX = (toastView.measuredWidth).toFloat()
            val targetX = 0f
            startAnimator = ValueAnimator.ofFloat(originX, targetX)
            startAnimator!!.addUpdateListener {
                val value = it.animatedValue as Float
                toastView.translationX = value
            }

            startAnimator!!.duration = 300
            startAnimator!!.interpolator = AccelerateInterpolator(1.5f)
            startAnimator!!.start()
            alreadyShownWindow = popupWindow

            hideToast(context, 2000)
        }

        private fun hideToast(context: Context, delay: Long = 0) {
            val alreadyShownWindow  = alreadyShownWindow ?: return

            val originX = alreadyShownWindow.contentView.measuredWidth.toFloat()
            val targetX = 0f

            hideAnimator = ValueAnimator.ofFloat(targetX, originX)
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

        fun showCompleteDialogIfNeed(context: Context, scoredInfo: ScoredStudentGoalInfo?) {
            if(scoredInfo?.isNeedToShowCompletedToast() == true) {
                show(context, "${scoredInfo.continuousGoalCount }일 연속\n목표달성에 성공했어요!","하루 ${scoredInfo.goalProblemCount}문제 풀기 완료 :)")
            }
        }

        fun showAdded(context: Context, subjectsNames:String) {
            show(context, "${subjectsNames}\n추가되었습니다.","메인화면에서 이름을 눌러\n언제든지 수정할 수 있어요.")
        }
    }
}

class SuccessToastView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var title: String
        set(value) {
            binding.titleTv.text = value
        }
        get() {
            return binding.titleTv.text.toString()
        }

    var contents: String
        set(value) {
            binding.contentTv.text = value
        }
        get() {
            return binding.contentTv.text.toString()
        }
    var binding: ViewSuccessToastBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_success_toast, this, true)

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_success_toast, this)
    }
}