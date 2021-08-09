package com.pulleymath.android.pdf.memo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class MemoScrollContainerView: ScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var isBlock = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if(isBlock) {
            ev?.pointerCount == 2
        } else {
            true
        }
    }
}