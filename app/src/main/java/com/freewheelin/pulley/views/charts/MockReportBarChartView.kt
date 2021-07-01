package com.freewheelin.pulley.views.charts

import android.animation.Animator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.show
import kotlinx.android.synthetic.main.activity_mock_report.*
import kotlinx.android.synthetic.main.view_mockexam_horizontal_bar_chart.view.*

class MockReportBarChartView(val context: Context, val parent: ViewGroup, val showSub:Boolean=false) {
    val view by lazy {LayoutInflater.from(context).inflate(R.layout.view_mockexam_horizontal_bar_chart, parent, false)}
    var mainPercent = 0f
    var subPercent = 0f

    fun setTitles(leftTitle:String, leftSub:String, rightTitle:String, rightSub:String) {
        view.leftTitle.text = leftTitle
        view.leftSubTitle.text = leftSub
        view.rightTitle.text = rightTitle
        view.rightSubTitle.text = rightSub
    }

    fun setMainColorWithPercent(percent:Int) {
        val colorId = when {
            percent <= 29 -> R.color.red_fe7b67
            percent >= 70 -> R.color.blue_78beff
            else -> R.color.yellow_ffd545
        }
        view.mainBar.progressColor = ContextCompat.getColor(context, colorId)
        view.mainBar.completeColor = ContextCompat.getColor(context, colorId)
        mainPercent = percent.toFloat()
    }

    fun setSubPercent(percent:Int) {
        subPercent = percent.toFloat()
    }

    fun show() {
        if(!showSub) {
            view.subBar.visibility = View.GONE
            view.rightSubTitle.visibility = View.GONE
        }
        parent.addView(view)


        val mp = mainPercent * 0.01f
        val duration = (mp * 800).toLong()
        view.mainBar.set(mp, true, listener = animationListener, delay = 30, duration = duration )
        view.subBar.set(subPercent * 0.01f, false)
        parent.postInvalidate()
    }

    val animationListener = object: Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
    }
}