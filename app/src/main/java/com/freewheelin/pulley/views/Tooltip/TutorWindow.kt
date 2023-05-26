package com.freewheelin.pulley.views.tooltip

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import com.freewheelin.pulley.views.balloonWindow.BalloonWindowListener
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.utils.visibleIf
import kotlin.math.PI
import kotlin.math.sin

class TutorWindow: BalloonWindow, BalloonWindowListener {
    constructor(context: Context, targetView: View, position: Position, offset: Int = 0) : super(context, targetView, position, offset) {
        balloonColor = ContextCompat.getColor(context, R.color.purple_300)
        paddingBottom = 0
        paddingLeft = 0
        paddingRight = 0
        paddingTop = 0
        elevation = 8f.toPx()
        balloonDrawable = ContextCompat.getDrawable(context, R.drawable.bg_purple_300_round_20)
    }

    fun startFloatAnim() {
        val anim = ValueAnimator.ofFloat(0f, PI.toFloat())
        anim.duration = 1200
        anim.repeatCount = 3
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    dismiss()
                }, 1000)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationStart(animation: Animator) {

            }
        })

        setBalloonListener(object : BalloonWindowListener {
            override fun didAppear(window: BalloonWindow) {
                anim.addUpdateListener {
                    val value = it.animatedValue as Float
                    val sign = sin(value)

                    val x = if (position == Position.right || position == Position.left) x + (20 * sign).toInt() else x
                    val y = if (position == Position.above || position == Position.below) y + (20 * sign).toInt() else y
                    window.update(x, y, window.width, window.height)
                }
                contentView.findViewById<LottieAnimationView>(R.id.lottieV)?.playAnimation()
                anim.start()
            }
        })
    }

    fun show(type: Tutor.TooltipType) {
        val toolTipView =  getContentView(type)

        startFloatAnim()
        show(toolTipView)
    }

    fun getContentView(type: Tutor.TooltipType): View {
        val toolTipView =  when(type) {
            Tutor.TooltipType.takeNoteScroll -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_horizontal, null)
                view.findViewById<LottieAnimationView>(R.id.lottieV).setAnimation("tooltip_take_note_scroll.json")
                view.findViewById<TextView>(R.id.contentsTv).text = "필기모드 시,\n" +
                        "두 손가락으로\n" +
                        "스크롤 하세요:)"
                view
            }
            Tutor.TooltipType.addSimilar -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical_img, null)
                view.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.ic_tooltip_add_similar)
                view.findViewById<TextView>(R.id.contentsTv).text = "같은 유형 문제를 추가해\n" +
                        "취약점을 채워보세요 :)"
                view
            }
            Tutor.TooltipType.addSimilarOfStartChallenge -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical_img, null)
                view.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.ic_tooltip_add_similar)
                view.findViewById<TextView>(R.id.contentsTv).text = "문제를 풀면\n" +
                    "유사문항을 계속\n" +
                    "추가할 수 있어요 :)"
                view
            }
            Tutor.TooltipType.changeSimilar -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical, null)
                view.findViewById<LottieAnimationView>(R.id.lottieV).visibility = View.GONE
                view.findViewById<TextView>(R.id.titleTv).visibility = View.GONE
                view.findViewById<TextView>(R.id.contentsTv).text = "추가된 문제를 풀면\n" +
                        "유사문항을 계속\n" +
                        "추가할 수 있어요 :)"
                view
            }
            Tutor.TooltipType.additionalStudyInWrongNote -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_horizontal, null)
                view.findViewById<LottieAnimationView>(R.id.lottieV).setAnimation("tooltip_additional_study.json")
                view.findViewById<TextView>(R.id.contentsTv).text = "더 공부하고 싶은\n" +
                        "문제를 체크하면\n" +
                        "문제리뷰 & 오답학습을\n" +
                        "할 수 있어요 :)"
                view
            }
            Tutor.TooltipType.additionalStudyInAnalysis -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_horizontal, null)
                view.findViewById<LottieAnimationView>(R.id.lottieV).setAnimation("tooltip_additional_study.json")
                view.findViewById<TextView>(R.id.contentsTv).text = "더 공부하고 싶은\n" +
                        "단원을 체크하면\n" +
                        "문제리뷰 & 추가학습을\n" +
                        "할 수 있어요 :)"
                view
            }
            Tutor.TooltipType.recommendPlan -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_img_horizontal, null)
                view.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.ic_tooltip_recommend)
                view.findViewById<TextView>(R.id.contentsTv).text = "${user!!.fullName}님께\n딱 맞는 플랜을 추천해드려요 :)"
                return view
            }
            Tutor.TooltipType.mailInUnitStudy -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical, null)
                view.findViewById<TextView>(R.id.titleTv).text = "출력하고 싶나요?"
                view.findViewById<LottieAnimationView>(R.id.lottieV).setAnimation("tooltip_mail.json")
                view.findViewById<TextView>(R.id.contentsTv).text = "이메일로 학습지를\n" + "보낼 수 있어요!"
                view
            }
            Tutor.TooltipType.middleIntroduceOpening -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical, null)
                view.findViewById<TextView>(R.id.titleTv).visibleIf(false)
                view.findViewById<LottieAnimationView>(R.id.lottieV).visibleIf(false)
                view.findViewById<TextView>(R.id.contentsTv).text = "중학생은 탭을\n" +
                    "클릭해보세요!"
                view
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.tooltip_vertical, null)
                view.findViewById<TextView>(R.id.titleTv).text = "출력하고 싶나요?"
                view.findViewById<LottieAnimationView>(R.id.lottieV).setAnimation("tooltip_mail.json")
                view.findViewById<TextView>(R.id.contentsTv).text = "이 아이콘을 선택하면\n" +
                        "이메일로 학습지를 보내드려요!"
                view
            }
        }

        return toolTipView
    }
}