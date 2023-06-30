package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class TabViewPager : ViewPager {

    private var mIsEnabled: Boolean = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mIsEnabled = true
    }

    constructor(context: Context) : super(context) {
        mIsEnabled = true
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (mIsEnabled) {
            super.onInterceptTouchEvent(event)
        } else {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mIsEnabled) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.mIsEnabled = enabled
    }
}