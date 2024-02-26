package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.dpToPx

class SchoolSpinner : androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        this.setPopupBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_400_round_10))
        this.dropDownVerticalOffset = 48.dpToPx()
    }
}