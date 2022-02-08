package com.freewheelin.pulley.views.calendarPickerViews

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
            setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
            background = ContextCompat.getDrawable(context, R.drawable.bg_black_4c4c4c_round_18)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
            background = null
        }
    }
}