package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.legacy.utils.toPx

class LCNaviTextView: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}


    init {
        val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 8.toPx(), 0, 0)
        setPadding(12.toPx(), 8.toPx(), 0, 8.toPx())
        layoutParams = lp
        id = View.generateViewId()
        typeface = ResourcesCompat.getFont(context, R.font.pretendard_semibold)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16));
        setTextColor(ContextCompat.getColor(context, R.color.gray_800))
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            isFocusable = true
        } else {
            focusable = View.FOCUSABLE
        }

    }
    var course: SingleCourseDesc? = null

    fun applyBackground(drawable: Drawable) {
        background = drawable
    }

    fun applyText(msg: String) {
        text = msg
    }

}