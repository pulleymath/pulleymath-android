package com.freewheelin.pulley.views.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.TextUtils
import kotlinx.android.synthetic.main.view_triple_bar_horizontal.view.*

class TripleBarHorizontalView: ConstraintLayout {
    var secondBarColor: Int
        set(value) {
            field = value
            secondBar.progressColor = value
            secondLabelTv.setTextColor(value)
            secondRateTv.setTextColor(value)
        }

    var title: String?
        get() {
            return titleTv.text.toString()
        }
        set(value) {
            field = value
            titleTv.text = value
        }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    init {
        LayoutInflater.from(context).inflate(R.layout.view_triple_bar_horizontal, this)
        secondBarColor = ContextCompat.getColor(context, R.color.blue_30a4ff)
        title = null
    }

    fun setValue(firstVal: Float, secondVal: Float, thirdVal: Float) {
        firstBar.set(firstVal)
        secondBar.set(secondVal)
        thirdBar.set(thirdVal)

        firstRateTv.text = TextUtils.percentFormat.format(firstVal)
        secondRateTv.text = TextUtils.percentFormat.format(secondVal)
        thirdRateTv.text = TextUtils.percentFormat.format(thirdVal)

        if(firstVal >= secondVal)
            secondBarColor = ContextCompat.getColor(context, R.color.red_fe7b67)
        else
            secondBarColor = ContextCompat.getColor(context, R.color.blue_30a4ff)
    }

    fun setLabel(firstVal: String, secondVal: String, thirdVal: String) {
        firstLabelTv.text = firstVal
        secondLabelTv.text = secondVal
        thirdLabelTv.text = thirdVal
    }
}