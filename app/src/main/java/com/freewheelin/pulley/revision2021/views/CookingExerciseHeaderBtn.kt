package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx

class CookingExerciseHeaderBtn: androidx.appcompat.widget.AppCompatButton {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    init {
        setTextAppearance(R.style.mo_h4)
//        updateLayoutParams<ViewGroup.MarginLayoutParams> {
//            setMargins(4.toPx(),4.toPx(),4.toPx(), 4.toPx())
//        }
    }

    var index = -1
    fun initUI (index: Int, size: Int) {
        val totalWeight = 60
        val calculatedWeight: Float = (totalWeight / size).toFloat()
        layoutParams = LinearLayout.LayoutParams(0, 36.toPx(), calculatedWeight).apply {
            setMargins(4.toPx(),4.toPx(),4.toPx(), 4.toPx())

        }
        this.index = index
        text = "예제 0${index + 1}"
        if (index ==0) {
            setStateSelected()
        } else {
            setStateCommon()
        }
    }
    fun setStateSelected() {
        setTextColor(ContextCompat.getColor(context, R.color.purple_300))
        setBackgroundResource(R.drawable.bg_white_round_5)
    }

    fun setStateCommon() {
        setTextColor(ContextCompat.getColor(context, R.color.purple_200))
        setBackgroundResource(R.drawable.bg_purple_100_round_5)
    }
}