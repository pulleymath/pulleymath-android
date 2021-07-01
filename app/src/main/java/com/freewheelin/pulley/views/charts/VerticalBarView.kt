package com.freewheelin.pulley.views.charts

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.hide
import com.freewheelin.pulley.utils.show
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.Bars.VerticalBar
import kotlinx.android.synthetic.main.view_bar_vertical.view.*
import java.io.Serializable

interface VerticalBarListener {
    fun onDetailBtnClicked(item: VerticalBarView)
}

class VerticalBarView : ConstraintLayout {
    class BarData(
            var title: String,
            val value: Serializable
    )

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var firstBar: VerticalBar? = null
    var secondBar: VerticalBar? = null
    var thirdBar: VerticalBar? = null

    var firstValue: Float? = null
    var secondValue: Float? = null
    var thirdValue: Float? = null

    var firstBarLabelTv: TextView? = null
    var secondBarLabelTv: TextView? = null
    var thirdBarLabelTv: TextView? = null

    var firstLabel: String?= null
    var secondLabel: String?= null
    var thirdLabel: String?= null

    var listener: VerticalBarListener? = null
    private var barMaxHeight = 160.toPx()

    private val visibleDuration: Long = 250
    var isSelectedDetailBtn: Boolean
        set(value) {
            field = value
            if(value) {
                detailBtn.setBackgroundResource(R.drawable.bg_black_4c4c4c_round_18)
                detailBtn.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                detailBtn.typeface = Theme.extraBold(context)
                showLabel(true)
            } else {
                detailBtn.setBackgroundResource(R.drawable.bg_white_ffffff_stroke_black_4c4c4c_round_18)
                detailBtn.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                detailBtn.typeface = Theme.bold(context)
                hideLabel(true)
            }
        }
    var title: String? = null
        get() {
            return titleTv.text.toString()
        }
        set(value) {
            field = value
            titleTv.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_bar_vertical, this)
        isSelectedDetailBtn = false
        this.setOnClickListener {
            isSelectedDetailBtn = !isSelectedDetailBtn
            listener?.onDetailBtnClicked(this)
        }
        detailBtn.setOnClickListener {
            isSelectedDetailBtn = !isSelectedDetailBtn
            listener?.onDetailBtnClicked(this)
        }
    }

    fun showLabel(withAnim: Boolean) {
        if(firstBarLabelTv == null) {
            val set = ConstraintSet()
            set.clone(this)

            firstBarLabelTv = makeBarLabel()
            secondBarLabelTv = makeBarLabel()
            secondBarLabelTv?.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
            thirdBarLabelTv = makeBarLabel()

            if (firstBar != null)
                setBarLabelPosition(firstBar!!, firstBarLabelTv!!, set)

            if (secondBar != null)
                setBarLabelPosition(secondBar!!, secondBarLabelTv!!, set)

            if (thirdBar != null)
                setBarLabelPosition(thirdBar!!, thirdBarLabelTv!!, set)

            set.applyTo(this)
        }

        firstBarLabelTv?.text = firstLabel
        secondBarLabelTv?.text = secondLabel
        thirdBarLabelTv?.text = thirdLabel

        firstBarLabelTv?.visibility = View.VISIBLE
        secondBarLabelTv?.visibility = View.VISIBLE
        thirdBarLabelTv?.visibility = View.VISIBLE

        if(withAnim) {
            firstBarLabelTv?.show(visibleDuration)
            secondBarLabelTv?.show(visibleDuration)
            thirdBarLabelTv?.show(visibleDuration)
        }
    }
    fun hideLabel(withAnim: Boolean) {
        if(withAnim) {
            firstBarLabelTv?.hide(visibleDuration)
            secondBarLabelTv?.hide(visibleDuration)
            thirdBarLabelTv?.hide(visibleDuration)
        } else {
            firstBarLabelTv?.visibility = View.GONE
            secondBarLabelTv?.visibility = View.GONE
            thirdBarLabelTv?.visibility = View.GONE
        }
    }



    fun configChartUI(data: BarData) {
        title = data.title

        val set = ConstraintSet()
        set.clone(this)

        when {
            data.value is Triple<*, *, *> -> {

                configureTripleUI(set)

                val firstValue = data.value.first as Float
                val secondValue = data.value.second as Float

                if(firstValue <= secondValue) {

                    secondBar?.color = ContextCompat.getColor(context, R.color.blue_30a4ff)
                } else {
                    secondBar?.color = ContextCompat.getColor(context, R.color.red_fe7b67)
                }
            }
            data.value is Pair<*, *> -> {
                configurePairUI(set)

                val firstValue = data.value.first as Float
                val secondValue = data.value.second as Float

                if(firstValue <= secondValue) {
                    secondBar?.color = ContextCompat.getColor(context, R.color.blue_30a4ff)
                } else {
                    secondBar?.color = ContextCompat.getColor(context, R.color.red_fe7b67)
                }
            }
            data.value is Float -> configureOnlyOneUI(set)
        }

        set.applyTo(this)
        requestLayout()

    }

    fun setValue(value: Serializable) {
        when (value) {
            is Triple<*, *, *> -> setTripleValue(value as Triple<Float, Float, Float>)
            is Pair<*, *> -> setPairValue(value as Pair<Float, Float>)
            is Float -> setOnlyOneValue(value)
        }
    }

    private fun setTripleValue(value: Triple<Float,Float,Float>) {
        firstBar?.value = value.first
        secondBar?.value = value.second
        thirdBar?.value = value.third
    }

    private fun setPairValue(value: Pair<Float, Float>) {
        firstBar?.value = value.first
        secondBar?.value = value.second
    }

    private fun setOnlyOneValue(value: Float) {
        firstBar?.value = value
    }

    private fun makeBar(color: Int = ContextCompat.getColor(context, R.color.grey_e0e0e0)): VerticalBar {
        val view = VerticalBar(context)
        view.id = View.generateViewId()
        view.color = color
        return view
    }

    private fun makeBarLabel(): TextView {
        val textView = TextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp12))
        textView.typeface = Theme.bold(context)
        textView.id = View.generateViewId()
        textView.gravity = Gravity.CENTER
        textView.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        return textView
    }


    private fun setDataLabelPosition(bar: View, textView: TextView, set: ConstraintSet) {
        this.addView(textView)
        set.connect(textView.id, START, bar.id, START)
        set.connect(textView.id, END, bar.id, END)
        set.connect(textView.id, BOTTOM, bar.id, TOP, 8.toPx())
        set.constrainDefaultHeight(textView.id, MATCH_CONSTRAINT_WRAP)
        set.constrainDefaultWidth(textView.id, MATCH_CONSTRAINT_WRAP)
    }

    private fun setBarLabelPosition(bar: View, textView: TextView, set: ConstraintSet) {
        this.addView(textView)
        set.connect(textView.id, START, bar.id, START)
        set.connect(textView.id, END, bar.id, END)
        set.connect(textView.id, BOTTOM, bar.id, BOTTOM, 8.toPx())
        set.constrainDefaultHeight(textView.id, MATCH_CONSTRAINT_WRAP)
        set.constrainDefaultWidth(textView.id, MATCH_CONSTRAINT_WRAP)

    }

    private fun configureTripleUI(set: ConstraintSet) {
        val firstBar = makeBar()
        val secondBar = makeBar()
        val thirdBar = makeBar(ContextCompat.getColor(context, R.color.blue_b9defe))

        val barWidth = resources.getDimension(R.dimen.triple_vertical_bar_width).toInt()
        val barSpace = resources.getDimension(R.dimen.triple_vertical_bar_space).toInt()

        addView(firstBar)
        addView(secondBar)
        addView(thirdBar)

        set.connect(firstBar.id, BOTTOM, bottomContainerCl.id, TOP)
        set.connect(secondBar.id, BOTTOM, bottomContainerCl.id, TOP)
        set.connect(thirdBar.id, BOTTOM, bottomContainerCl.id, TOP)

        set.connect(firstBar.id, START, this.id, START)
        set.connect(firstBar.id, END, secondBar.id, START)
        set.constrainWidth(firstBar.id, barWidth)

        set.connect(secondBar.id, START, firstBar.id, END, barSpace)
        set.connect(secondBar.id, END, thirdBar.id, START, barSpace)
        set.constrainWidth(secondBar.id, barWidth)

        set.connect(thirdBar.id, START, secondBar.id, END)
        set.connect(thirdBar.id, END, this.id, END)
        set.constrainWidth(thirdBar.id, barWidth)
        this.firstBar = firstBar
        this.secondBar = secondBar
        this.thirdBar = thirdBar
    }

    private fun configurePairUI(set: ConstraintSet) {
        val firstBar = makeBar()
        val secondBar = makeBar()

        val barWidth = resources.getDimension(R.dimen.triple_vertical_bar_width).toInt()
        val barSpace = resources.getDimension(R.dimen.triple_vertical_bar_space).toInt()

        addView(firstBar)
        addView(secondBar)

        set.connect(firstBar.id, BOTTOM, bottomContainerCl.id, TOP)
        set.connect(secondBar.id, BOTTOM, bottomContainerCl.id, TOP)

        set.connect(firstBar.id, START, this.id, START)
        set.connect(firstBar.id, END, secondBar.id, START)
        set.constrainWidth(firstBar.id, barWidth)

        set.connect(secondBar.id, START, firstBar.id, END, barSpace)
        set.connect(secondBar.id, END, this.id, END)
        set.constrainWidth(secondBar.id, barWidth)

        this.firstBar = firstBar
        this.secondBar = secondBar
    }

    private fun configureOnlyOneUI(set: ConstraintSet) {
        val firstBar = makeBar()
        addView(firstBar)

        val barWidth = resources.getDimension(R.dimen.triple_vertical_bar_width).toInt()

        set.connect(firstBar.id, BOTTOM, bottomContainerCl.id, TOP)
        set.connect(firstBar.id, START, this.id, START)
        set.connect(firstBar.id, END, this.id, END)
        set.constrainWidth(firstBar.id, barWidth)

        this.firstBar = firstBar
    }

}
