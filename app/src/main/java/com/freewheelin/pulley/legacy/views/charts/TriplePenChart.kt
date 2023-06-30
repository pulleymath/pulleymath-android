package com.freewheelin.pulley.legacy.views.charts

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.views.bars.PenBar_R

class TriplePenChart : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
    }

    var labelTvs: List<TextView>

    var penbars: List<PenBar_R>

    var isGoneLast: Boolean = false
        set(value) {
            field = value

            if(value) {
                thirdLabelTv.visibility = View.GONE
                thirdPen.visibility = View.GONE
            } else {
                thirdLabelTv.visibility = View.VISIBLE
                thirdPen.visibility = View.VISIBLE
            }
        }

    var firstLabelTv: TextView
    var secondLabelTv: TextView
    var thirdLabelTv: TextView

    var firstPen: PenBar_R
    var secondPen: PenBar_R
    var thirdPen: PenBar_R

    init {
        LayoutInflater.from(context).inflate(R.layout.view_chart_triple_pen, this, true)

        firstLabelTv = findViewById(R.id.firstLabelTv)
        secondLabelTv = findViewById(R.id.secondLabelTv)
        thirdLabelTv = findViewById(R.id.thirdLabelTv)

        firstPen = findViewById(R.id.firstPen)
        secondPen = findViewById(R.id.secondPen)
        thirdPen = findViewById(R.id.thirdPen)

        labelTvs = listOf(
                firstLabelTv,
                secondLabelTv,
                thirdLabelTv
        )
        penbars = listOf(
                firstPen,
                secondPen,
                thirdPen
        )
    }

    var focusedIndex: Int = 1
        set(value) {
            val focusTextColor = ContextCompat.getColor(context, R.color.gray_800)
            val unfocusTextColor = ContextCompat.getColor(context, R.color.gray_500)
            val focusProgressColor = penbars[field].progressColor
            val unfocusProgressColor = ContextCompat.getColor(context, R.color.gray_400)

            field = value
            labelTvs.forEach { it.setTextColor(unfocusTextColor) }
            penbars.forEach {
                it.progressColor = unfocusProgressColor
                it.textColor = unfocusTextColor
            }

            labelTvs[value].setTextColor(focusTextColor)
            penbars[value].progressColor = focusProgressColor
            penbars[value].textColor = focusTextColor
        }


    fun hideFirst() {
        firstPen.visibility = View.GONE
        firstLabelTv.visibility = View.GONE
    }

    fun hideThird() {
        thirdPen.visibility = View.GONE
        thirdLabelTv.visibility = View.GONE
    }

    fun setLabels(firstLabel: String, secondLabel: String, thirdLabel: String?= "") {
        labelTvs[0].text = firstLabel
        labelTvs[1].text = secondLabel
        labelTvs[2].text = thirdLabel
    }

    fun setValues(first: Float, second: Float, third: Float=0f, withAnim: Boolean = false, withRangeColor: Boolean = false) {
        if (withAnim) {
            val firstOrigin = penbars[0].value
            val secondOrigin = penbars[1].value
            val thirdOrigin = penbars[2].value

            val firstDiff = first - penbars[0].value
            val secondDiff = second - penbars[1].value
            val thirdDiff = third - penbars[2].value

            if(withRangeColor) {
                penbars[focusedIndex].progressColor = getRangeColor(first)
            }

            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.duration = 500
            anim.addUpdateListener {
                val animVal = it.animatedValue as Float
                penbars[0].value = firstOrigin + (firstDiff * animVal)
                penbars[1].value = secondOrigin + (secondDiff * animVal)
                penbars[2].value = thirdOrigin + (thirdDiff * animVal)

            }
            anim.start()
        } else {
            penbars[0].value = first
            penbars[1].value = second
            penbars[2].value = third
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.TriplePenChart)
        isGoneLast = array.getBoolean(R.styleable.TriplePenChart_isGoneLast, false)
        focusedIndex = array.getInteger(R.styleable.TriplePenChart_focusIndex, 1)
        array.recycle()
    }

    private fun getRangeColor(value: Float): Int {
        return if(value <= 0.5)
            ContextCompat.getColor(context, R.color.red_300)
        else if(value >0.5 && value <=0.8)
            ContextCompat.getColor(context, R.color.yellow_200)
        else
            ContextCompat.getColor(context, R.color.green_300)
    }

    fun setLabelTextSize(unit: Int, size: Float) {
        this.labelTvs.forEach {
            it.setTextSize(unit,size)
        }
    }

    fun setLabelWidth(size: Int) {
        this.labelTvs.forEach {
            it.layoutParams.width = size
        }
    }

    fun setValueTextSize(size: Float) {
        this.penbars.forEach {
            it.textSize = size
        }
    }
}