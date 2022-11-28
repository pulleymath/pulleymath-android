package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue


class LCPatternViewPager: LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    var pager: ViewPager2
    var isPagerSwipeBlocked = false
    var scaleFactor = 1f
    var pagerEnableCallback: (Boolean) -> Unit = {}

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val vp = ViewPager2(context)
        vp.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        vp.id = View.generateViewId()
        this.addView(vp)
        pager = vp
        pager.isSaveEnabled = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        handleInterceptTouchEvent(ev)

        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    private var touchSlop = 0
    private var initialX = 0f
    private var initialY = 0f

    private fun handleInterceptTouchEvent(e: MotionEvent) {

        if (e.action == MotionEvent.ACTION_DOWN) {
            initialX = e.x
            initialY = e.y
        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = e.x - initialX
            val dy = e.y - initialY

            val scaledDx = dx.absoluteValue * 1f
            val scaledDy = dy.absoluteValue * .5f

            if (e.pointerCount == 2) {
                pager.isUserInputEnabled = !isPagerSwipeBlocked
            }
            if (scaleFactor != 1f) return
            if (isPageStartIndex && (scaledDx > scaledDy) && dx > 0 && !isPagerSwipeBlocked) {
                pagerEnableCallback(true)
                parent.requestDisallowInterceptTouchEvent(false)
            } else if (isPageEndIndex && (scaledDx > scaledDy) && dx < 0 && !isPagerSwipeBlocked) {
                pagerEnableCallback(true)
                parent.requestDisallowInterceptTouchEvent(false)
            } else {
                pagerEnableCallback(false)
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
    }

    var isPageStartIndex: Boolean = false
    var isPageEndIndex: Boolean = false

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