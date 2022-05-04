package com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.toPx

class FilterButtonHolder(val filterBtn: Button) : RecyclerView.ViewHolder(filterBtn) {

    val context get() = filterBtn.context
    var isSelected: Boolean = false
        set(value) {
            field = value
            if (value) {
                filterBtn.typeface = Theme.extraBold(filterBtn.context)
                filterBtn.setTextColor(ContextCompat.getColor(filterBtn.context, R.color.purple_6D6DFF))
                filterBtn.background = ContextCompat.getDrawable(filterBtn.context, R.drawable.bg_purple_ecebff_stroke_purple_acacff_round_18)
            } else {
                filterBtn.typeface = Theme.bold(filterBtn.context)
                filterBtn.setTextColor(ContextCompat.getColor(filterBtn.context, R.color.black_4c4c4c))
                filterBtn.background = ContextCompat.getDrawable(filterBtn.context, R.drawable.bg_white_fafafa_stroke_grey_e8e8e8_round_18)
            }
        }

    init {
        filterBtn.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        filterBtn.typeface = Theme.bold(context)
        filterBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, filterBtn.resources.getDimension(R.dimen.sp14))
        filterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_white_fafafa_stroke_grey_e8e8e8_round_18)
        val height = 36.toPx()
        filterBtn.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }

}


