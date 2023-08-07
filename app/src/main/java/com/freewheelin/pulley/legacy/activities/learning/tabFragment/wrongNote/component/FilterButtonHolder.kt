package com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.utils.toPx

class FilterButtonHolder(val filterBtn: Button) : RecyclerView.ViewHolder(filterBtn) {

    val context get() = filterBtn.context
    var isSelected: Boolean = false
        set(value) {
            field = value
            if (value) {
                filterBtn.typeface = Theme.extraBold(filterBtn.context)
                filterBtn.setTextColor(ContextCompat.getColor(filterBtn.context, R.color.purple_300))
                filterBtn.background = ContextCompat.getDrawable(filterBtn.context, R.drawable.bg_purple_100_stroke_purple_200_round_18)
            } else {
                filterBtn.typeface = Theme.bold(filterBtn.context)
                filterBtn.setTextColor(ContextCompat.getColor(filterBtn.context, R.color.gray_800))
                filterBtn.background = ContextCompat.getDrawable(filterBtn.context, R.drawable.bg_gray_100_stroke_gray_300_round_18_ripple)
            }
        }

    init {
        filterBtn.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
        filterBtn.typeface = Theme.bold(context)
        filterBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, filterBtn.resources.getDimension(R.dimen.sp14))
        filterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_gray_100_stroke_gray_300_round_18_ripple)
        val height = 36.toPx()
        filterBtn.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }

}


