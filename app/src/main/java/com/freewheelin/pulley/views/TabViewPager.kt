package com.freewheelin.pulley.views

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

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