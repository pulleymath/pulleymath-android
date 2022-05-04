package com.freewheelin.pulley.views.charts

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewMockexamBarChartBinding
import kotlinx.coroutines.*

class MockReportBarChartView: LinearLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { setTypedArray(attrs) }
    fun setTypedArray(attrs: AttributeSet) {}

    var mainPercent = 0f
    var subPercent = 0f
    var binding: ViewMockexamBarChartBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_mockexam_bar_chart, this, true)

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

    fun show(showSub: Boolean) {
        if(!showSub) {
            binding.subBar.visibility = View.GONE
            binding.rightSubTitle.visibility = View.GONE
        }

        val mp = mainPercent * 0.01f
        val duration = (mp * 800).toLong()
        binding.mainBar.set(mp, true, listener = animationListener, delay = 30, duration = duration )

        println("tpehf subBar subPercent : ${subPercent}")
        binding.subBar.set(subPercent * 0.01f, false)
    }


    val animationListener = object: Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
    }
}