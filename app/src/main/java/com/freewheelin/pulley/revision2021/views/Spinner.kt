package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

class Spinner : androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        this.setPopupBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round_10))
    }
}