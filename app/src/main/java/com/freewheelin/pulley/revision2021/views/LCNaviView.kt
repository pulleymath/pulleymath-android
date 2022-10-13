package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewLcNaviTextBinding
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc

class LCNaviView: ConstraintLayout {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}
    val binding: ViewLcNaviTextBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_lc_navi_text, this, true)

    var course: SingleCourseDesc? = null

    init {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            isFocusable = true
        } else {
            focusable = View.FOCUSABLE
        }
    }
}