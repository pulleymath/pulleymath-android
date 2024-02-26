package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.BigUnitV3
import com.freewheelin.pulley.legacy.core.Theme

class SubjectSelectionButton: androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {

    }
    var bigUnits: MutableList<BigUnitV3> = mutableListOf()
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if(isSelected) {
            typeface = Theme.extraBold(context)
            setTextColor(ContextCompat.getColor(context, R.color.purple_300))
            background = ContextCompat.getDrawable(context, R.drawable.bg_purple_100_stroke_purple_200_round_24)
        } else {
            typeface = Theme.bold(context)
            setTextColor(ContextCompat.getColor(context, R.color.gray_800))
            background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_300_round_24)
        }
    }
}