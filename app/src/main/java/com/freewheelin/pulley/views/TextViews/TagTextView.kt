package com.freewheelin.pulley.views.textViews

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx

class TagTextView: TextView {
    constructor(context: Context,
                title: String,
                textSize: Float): super(context) {
        setPadding(8.toPx(), 4.toPx(), 8.toPx(), 4.toPx())
        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        setTextColor(ContextCompat.getColor(context, R.color.grey_9f9f9f))
        text = title
    }
}