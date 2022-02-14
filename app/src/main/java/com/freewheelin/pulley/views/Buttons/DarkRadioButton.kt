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
            setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
            background = ContextCompat.getDrawable(context, R.drawable.bg_grey_f2f2f2_round_18)
            typeface = Theme.extraBold(context)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.grey_f2f2f2))
            background = ContextCompat.getDrawable(context, R.drawable.bg_grey_3d3d3d_round_18)
            typeface = Theme.bold(context)
        }
    }
}