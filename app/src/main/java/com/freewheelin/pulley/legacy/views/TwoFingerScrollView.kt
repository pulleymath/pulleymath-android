package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView


class TwoFingerScrollView: ScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)



    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if(ev.pointerCount == 1 && ev.action == MotionEvent.ACTION_MOVE)
            return false

        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event?.pointerCount == 2)
            return true

        return super.onInterceptTouchEvent(event)
    }
}

class DisableHorizontalScrollView: HorizontalScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return true
    }
}

class DisableVerticalScrollView: ScrollView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var isBlock = false
    var fingerDrawMode = false

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(isBlock) {
            val pp = MotionEvent.PointerProperties()
            ev?.getPointerProperties(0, pp)
            if (!fingerDrawMode && pp.toolType != MotionEvent.TOOL_TYPE_STYLUS) {
                return true
            }
            return ev?.pointerCount == 2
        } else {
            return true
        }
    }
}