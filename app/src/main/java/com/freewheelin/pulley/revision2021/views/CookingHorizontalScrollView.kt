package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class CookingHorizontalScrollView: HorizontalScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var isPageStartIndex: Boolean = false
    var isPageEndIndex: Boolean = false

    // 스크롤이 안돼야한다.
    val SCROLLABLE = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
               SCROLLABLE && super.onTouchEvent(ev)
            }
            else -> super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return SCROLLABLE && super.onInterceptTouchEvent(ev)
    }

    fun setTabIndexStatus(lastIndex: Int, index: Int) {
        when (index) {
            0 -> setStartIndex()
            lastIndex -> setEndIndex()
            else -> setMiddleIndex()
        }
    }

    var scrollOldX = 0
    var scrollNewX = 0
    override fun onScrollChanged(x: Int, t: Int, oldX: Int, oldt: Int) {
        scrollNewX = x
        scrollOldX = oldX
        super.onScrollChanged(x, t, oldX, oldt)
    }

    fun setStartIndex() {
        isPageStartIndex = true
        isPageEndIndex = false
    }
    fun setEndIndex() {
        isPageStartIndex = false
        isPageEndIndex = true
    }
    fun setMiddleIndex() {
        isPageStartIndex = false
        isPageEndIndex = false
    }
}