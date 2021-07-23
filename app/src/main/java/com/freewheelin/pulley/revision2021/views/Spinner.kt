package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

class Spinner : androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        this.setPopupBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_black_4c4c4c_round_10))
    }
}