package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.view_add_optional_unit_toast.view.*
import kotlinx.android.synthetic.main.view_success_toast.view.*
import kotlinx.android.synthetic.main.view_success_toast.view.contentTv
import kotlinx.android.synthetic.main.view_success_toast.view.titleTv

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

            toastView.checkNoShow.setOnCheckedChangeListener { buttonView, isChecked -> check = isChecked }

            toastView.btnNo.setOnClickListener { no(context) }
            toastView.btnYes.setOnClickListener { yes(context) }

            val popupWindow = PopupWindow(toastView, toastView.measuredWidth, toastView.measuredHeight)

            popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))

            val offsetY = DisplayUtils.getScrenHeight(context) / 2

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
            if(scoredInfo?.getAskAddSubjects()?.isNotEmpty() == true) {
                AddOptionUnitToast.scoredInfo = scoredInfo
                val title = scoredInfo.getAskAddSubjectCodeText()
                show(context, "${title}\n과목을 풀고 있군요!","추천설정에 없는 선택과목인데\n추가해 놓을까요?")
            }
        }

        fun no(context:Context) {
            setNoShowConfigure()
            hideToast(context, 0)
        }

        fun yes(context:Context) {
            setNoShowConfigure()
            scoredInfo?.let {
                UserManager.addInitOptionalSubject(context!!, user!!, it.getAskAddSubjects(), successCB = {
//                    CompleteDialog(context!!, "추가되었습니다.", "해당 내역은 추천 문항에 반영됩니다.").showFor(1000)

                    SuccessToast.showAdded(context, scoredInfo?.getAskAddSubjectCodeText()?:"")
                    hideToast(context, 0)
                })
            }
        }

        fun setNoShowConfigure() {
            user?.setNoShowAddOptionalSubject(check)
        }
    }
}

class AddOptionalUnitToastView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var title: String
        set(value) {
            titleTv.text = value
        }
        get() {
            return titleTv.text.toString()
        }

    var contents: String
        set(value) {
            contentTv.text = value
        }
        get() {
            return contentTv.text.toString()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_add_optional_unit_toast, this)
//        setBackgroundResource(R.drawable.bg_purple_6d6dff_radius_20_left_only)
//        setPadding(32.toPx() ,32.toPx() ,32.toPx(), 32.toPx())
    }
}