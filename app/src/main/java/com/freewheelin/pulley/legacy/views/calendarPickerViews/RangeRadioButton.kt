package com.freewheelin.pulley.legacy.views.calendarPickerViews

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

class RangeRadioButton: Button {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        this.isSelected = false
    }


    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected) {
            setTextColor(ContextCompat.getColor(context, R.color.white))
            background = ContextCompat.getDrawable(context, R.drawable.bg_gray_800_round_18)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.gray_800))
            background = null
        }
    }
}