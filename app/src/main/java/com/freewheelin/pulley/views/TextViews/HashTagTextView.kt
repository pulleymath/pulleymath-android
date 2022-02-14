package com.freewheelin.pulley.views.textViews

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.freewheelin.pulley.R

class HashTagTextView: TextView {
    constructor(context: Context,
                title: String): super(context) {
        val leftRightPadding = resources.getDimension(R.dimen.dp16).toInt()
        val topBottomPadding = resources.getDimension(R.dimen.dp12).toInt()
        val textSize = resources.getDimension(R.dimen.sp16)
        setPadding(leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding)
        background = ContextCompat.getDrawable(context, R.drawable.bg_grey_f2f2f2_round_20)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        typeface = ResourcesCompat.getFont(context, R.font.pretendard_semibold)
        text = title
    }
}