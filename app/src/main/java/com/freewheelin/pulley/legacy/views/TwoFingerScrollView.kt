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

//    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
//        if(childCount > 0)
//            when(event?.keyCode) {
//                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
//                    getChildAt(0).dispatchKeyEvent(event)
//                    return true
//                }
//            }
//        return super.dispatchKeyEvent(event)
//    }

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

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(isBlock) {
            if(ev?.pointerCount == 2)
                return true
            else
                return false
        } else {
            return true
        }
    }
}