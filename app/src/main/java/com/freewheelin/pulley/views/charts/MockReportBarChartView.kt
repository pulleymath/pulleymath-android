package com.freewheelin.pulley.views.charts

import android.animation.Animator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewMockexamHorizontalBarChartBinding

class MockReportBarChartView(val context: Context, val parent: ViewGroup, val showSub:Boolean=false) {
//    val view by lazy { LayoutInflater.from(context).inflate(R.layout.view_mockexam_horizontal_bar_chart, parent, false)}
    var mainPercent = 0f
    var subPercent = 0f
    var binding: ViewMockexamHorizontalBarChartBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_mockexam_horizontal_bar_chart, parent, false)

    fun setTitles(leftTitle:String, leftSub:String, rightTitle:String, rightSub:String) {
        binding.leftTitle.text = leftTitle
        binding.leftSubTitle.text = leftSub
        binding.rightTitle.text = rightTitle
        binding.rightSubTitle.text = rightSub
    }

    fun setMainColorWithPercent(percent:Int) {
        val colorId = when {
            percent <= 29 -> R.color.red_fe7b67
            percent >= 70 -> R.color.blue_78beff
            else -> R.color.yellow_ffd545
        }
        binding.mainBar.progressColor = ContextCompat.getColor(context, colorId)
        binding.mainBar.completeColor = ContextCompat.getColor(context, colorId)
        mainPercent = percent.toFloat()
    }

    fun setSubPercent(percent:Int) {
        subPercent = percent.toFloat()
    }

    fun show() {
        if(!showSub) {
            binding.subBar.visibility = View.GONE
            binding.rightSubTitle.visibility = View.GONE
        }
        parent.addView(binding.root)


        val mp = mainPercent * 0.01f
        val duration = (mp * 800).toLong()
        binding.mainBar.set(mp, true, listener = animationListener, delay = 30, duration = duration )
        binding.subBar.set(subPercent * 0.01f, false)
        parent.postInvalidate()
    }

    val animationListener = object: Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
    }
}