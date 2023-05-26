package com.freewheelin.pulley.views.buttons

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.Button
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme

class DarkRadioButton: Button {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        configureUI()
    }

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
        configureUI()
    }

    private fun configureUI() {
        if(isSelected) {
            setTextColor(ContextCompat.getColor(context, R.color.gray_800))
            background = ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_18)
            typeface = Theme.extraBold(context)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.gray_200))
            background = ContextCompat.getDrawable(context, R.drawable.bg_black_100_round_18)
            typeface = Theme.bold(context)
        }
    }
}