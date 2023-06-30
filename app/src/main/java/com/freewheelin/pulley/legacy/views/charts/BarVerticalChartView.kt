package com.freewheelin.pulley.legacy.views.charts

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewBarVerticalChartBinding
import java.io.Serializable

interface BarVerticalChartViewListener {
    fun onBarDetailClicked(chart: VerticalBarView, index: Int)
}
class BarVerticalChartView: ConstraintLayout, VerticalBarListener {


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var bars: List<VerticalBarView>? = null
    var listener: BarVerticalChartViewListener? = null


    var selectedBar: VerticalBarView? = null
    var binding: ViewBarVerticalChartBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_bar_vertical_chart, this, true)

    fun setValues(values: List<Serializable>) {

        val charts = values.map {
            val chart = VerticalBarView(context)
            chart.id = View.generateViewId()
            this.addView(chart)
            chart
        }
        configureUI(charts)
        requestLayout()
        this.bars = charts
    }
    private fun setData(data: List<VerticalBarView.BarData>) {
        bars?.forEach { this.removeView(it) }

        val charts = data.map {
            val chart = VerticalBarView(context)
            chart.id = View.generateViewId()
            chart.configChartUI(it)
            chart.setValue(it.value)
            chart.listener = this
            this.addView(chart)
            chart
        }
        configureUI(charts)
        requestLayout()
        this.bars = charts
    }


    fun setData(data: List<VerticalBarView.BarData>, withAnim: Boolean) {
        bars?.forEach { this.removeView(it) }

        if(data.isEmpty()) {
            showEmptyGuideTv()
            return
        }
        hideEmptyGuideTv()

        if (!withAnim) {
            setData(data)
            return
        }

        val charts = data.map {
            val chart = VerticalBarView(context)
            chart.id = View.generateViewId()
            chart.configChartUI(it)
            chart.listener = this
            this.addView(chart)
            chart
        }

        configureUI(charts)
        requestLayout()
        this.bars = charts

        var animator: ValueAnimator = ValueAnimator.ofFloat(0.01f,1f)
        animator.duration = 700
        animator.addListener(object: Animator.AnimatorListener{

            override fun onAnimationEnd(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationStart(p0: Animator) {}
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float

            for (i in 0 until data.size) {
                val dataVal = data[i].value
                data[i].value is Triple<*, *, *>
                val animValue = when(dataVal) {
                    is Triple<*, *, *> -> {
                        Triple((dataVal.first as Float) * value,
                                (dataVal.second as Float) * value,
                                (dataVal.third as Float) * value
                        )
                    }
                    is Pair<*,*> -> {
                        Pair((dataVal.first as Float) * value, (dataVal.second as Float) * value)
                    }
                    else -> {
                        (dataVal as Float) * value
                    }
                }

                charts[i].setValue(animValue)
            }
        }

        animator.start()
    }

    fun setSelectedBarLabel(firstLabel: String, secondLabel:String?=null, thirdLabel:String?=null) {
        bars?.forEach {
            it.firstLabel = firstLabel
            it.secondLabel = secondLabel
            it.thirdLabel = thirdLabel
        }
    }

    fun setDetailBtnVisibility(visibility: Int) {
        bars?.forEach { it.detailBtn.visibility = visibility}
    }

    fun showAllLabels() {
        bars?.forEach { it.showLabel(false) }
    }


    private fun configureUI(charts: List<View>) {
        val set = ConstraintSet()
        set.clone(this)

        for (chart in charts) {
            val index = charts.indexOf(chart)

            set.connect(chart.id, BOTTOM, this.id, BOTTOM)
            if(index == 0)
                set.connect(chart.id, START, this.id, START)
            else
                set.connect(chart.id, START, charts[index - 1].id, END)

            if(index + 1 >= charts.size)
                set.connect(chart.id, END, this.id, END)
            else
                set.connect(chart.id, END, charts[index + 1].id, START)
        }
        if(charts.size == 6) {
            val viewIds = charts.map { it.id }.toIntArray()
            set.createHorizontalChain(
                    this.id,
                    ConstraintSet.LEFT,
                    this.id,
                    ConstraintSet.RIGHT,
                    viewIds,
                    null,
                    ConstraintSet.CHAIN_SPREAD_INSIDE
            )
        }
        set.applyTo(this)
    }

    fun setEmptyGuideText(text: String) {
        binding.emptyGuideTv.text = text
    }

    fun showEmptyGuideTv() {
        binding.horizontalBorder.visibility = View.GONE
        binding.emptyGuideTv.visibility = View.VISIBLE

    }

    fun hideEmptyGuideTv() {
        binding.horizontalBorder.visibility = View.VISIBLE
        binding.emptyGuideTv.visibility = View.GONE
    }

    override fun onDetailBtnClicked(item: VerticalBarView) {
        selectedBar?.isSelectedDetailBtn = false
        if(selectedBar != item)
            selectedBar?.hideLabel(true)

        item.isSelectedDetailBtn = true
        selectedBar = item

        val bars = bars
        val index = bars?.indexOf(item) ?: return

        listener?.onBarDetailClicked(item, index)
    }
}