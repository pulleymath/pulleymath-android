package com.freewheelin.pulley.legacy.dialogs

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.legacy.activities.WrongTestReportActivity
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent

class SubmitCompleteLottieDialog(context: Context, content: Content): Dialog(context) {

    var scoreTv: TextView
    var completeGuideTv: TextView
    var scoreSuffixLabel: TextView
    var submitLottie: LottieAnimationView
    var confirmBtn: Button

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_submit_complete_lottie)

        scoreTv = findViewById(R.id.scoreTv)
        completeGuideTv = findViewById(R.id.completeGuideTv)
        scoreSuffixLabel = findViewById(R.id.scoreSuffixLabel)
        submitLottie = findViewById(R.id.submitLottie)
        confirmBtn = findViewById(R.id.confirmBtn)

        confirmBtn.setOnClickListener { onConfirmBtnClicked(content) }
        scoreTv.text = content.score.toString()

        when(content) {
            is Test -> {
                when(content.getTestType()) {
                    Test.TestType.daily -> {
                        completeGuideTv.text = "데일리 테스트 ${content.scoringTestPieceCount}회차 클리어!\n" +
                                "꼭 확인할 문제는 무엇일까요?"
                    }
                    Test.TestType.weekly -> {
                        completeGuideTv.text = "이번 주 주간테스트 클리어!\n" +
                                "꼭 확인할 문제는 무엇일까요?"
                    }
                    else -> {}
                }
            }
        }

    }

    override fun show() {
        super.show()
        playAnim()
    }
    fun show(cb: () -> Unit) {
        super.show()
        playAnim(cb)
    }


    fun showScoreInfo() {
        scoreTv.visibility = View.VISIBLE
        scoreSuffixLabel.visibility = View.VISIBLE
        completeGuideTv.visibility  = View.VISIBLE
        confirmBtn.visibility = View.VISIBLE
    }

    fun playAnim(cb: (() -> Unit)? = null) {
        submitLottie.playAnimation()
        submitLottie.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                submitLottie.visibility = View.INVISIBLE
                showScoreInfo()
                if(cb != null)
                    cb()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })
    }

    private fun onConfirmBtnClicked(content: Content) {
        dismiss()

        when(content) {
            is Test -> {
                when(content.getTestType()) {
                    Test.TestType.daily -> {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "테스트", "보고서 바로보기", "데일리테스트")
                        val intent = com.freewheelin.pulley.legacy.activities.DailyTestReportActivity.getIntent(context, content, true)
                        context.startActivity(intent)
                    }
                    Test.TestType.weekly -> {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "테스트", "보고서 바로보기","주간테스트")
                        val intent = WeeklyTestReportActivity.getIntent(context, content, true)
                        context.startActivity(intent)
                    }
                    Test.TestType.wrong -> {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "테스트", "보고서 바로보기","오답테스트")
                        val intent = WrongTestReportActivity.getIntent(context, content, true)
                        context.startActivity(intent)
                    }
                    else -> {}
                }
            }
        }
    }
}