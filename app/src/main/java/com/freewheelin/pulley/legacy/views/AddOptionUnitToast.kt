package com.freewheelin.pulley.legacy.views

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
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.ViewAddOptionalUnitToastBinding
import com.freewheelin.pulley.legacy.utils.DisplayUtils

class AddOptionUnitToast: PopupWindow() {
    companion object {
        var startAnimator: ValueAnimator? = null
        var hideAnimator: ValueAnimator? = null
        var alreadyShownWindow: PopupWindow? = null

        var check = false

        var scoredInfo:ScoredStudentGoalInfo? = null

        fun show(context: Context, title: String, contents: String) {
            startAnimator?.cancel()
            hideAnimator?.cancel()
            if(alreadyShownWindow != null)
                alreadyShownWindow?.dismiss()

            val toastView = AddOptionalUnitToastView(context)

            toastView.title = title
            toastView.contents = contents
//            toastView.setOnClickListener {
//                hideToast(context, 500)
//            }
            toastView.measure(0,0)

            toastView.binding.checkNoShow.setOnCheckedChangeListener { buttonView, isChecked -> check = isChecked }

            toastView.binding.btnNo.setOnClickListener { no(context) }
            toastView.binding.btnYes.setOnClickListener { yes(context) }

            val popupWindow = PopupWindow(toastView, toastView.measuredWidth, toastView.measuredHeight)

            popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))

            val offsetY = DisplayUtils.getScreenHeight(context) / 2

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
            // TODO SubjectV3 개편과 동시에 AddOptionUnitToast를 사용하지 않게되어 주석처리하였다.
//            if(scoredInfo?.getAskAddSubjects()?.isNotEmpty() == true) {
//                AddOptionUnitToast.scoredInfo = scoredInfo
//                val title = scoredInfo.getAskAddSubjectCodeText()
//                show(context, "${title}\n과목을 풀고 있군요!","추천설정에 없는 선택과목인데\n추가해 놓을까요?")
//            }
        }

        fun no(context:Context) {
            setNoShowConfigure()
            hideToast(context, 0)
        }

        fun yes(context:Context) {
            setNoShowConfigure()
            // AddOptionUnitToast 을 사용하지않아서 Subject -> SubjectV3 변경작업에서 주석처리되었습니다.
//            scoredInfo?.let {
//                UserManager.addInitOptionalSubject(context!!, user!!, it.getAskAddSubjects(), successCB = {
////                    CompleteDialog(context!!, "추가되었습니다.", "해당 내역은 추천 문항에 반영됩니다.").showFor(1000)
//
//                    SuccessToast.showAdded(context, scoredInfo?.getAskAddSubjectCodeText()?:"")
//                    hideToast(context, 0)
//                })
//            }
        }

        fun setNoShowConfigure() {
//            user?.setNoShowAddOptionalSubject(check)
        }
    }
}

class AddOptionalUnitToastView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var binding: ViewAddOptionalUnitToastBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_add_optional_unit_toast, this, true)

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

    init {
    }
}