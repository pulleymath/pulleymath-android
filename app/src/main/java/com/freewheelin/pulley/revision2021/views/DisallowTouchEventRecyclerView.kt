package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DisallowTouchEventRecyclerView : RecyclerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    var isBlocked = false
    var verticalScrollFlag = true

    init {
        layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return verticalScrollFlag && super.canScrollVertically()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        // isBlocked가 false이면 무조건 canScroll, pointcount 2 이면 canScroll
        verticalScrollFlag = ev?.pointerCount == 2 || !isBlocked

        if (isBlocked) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }
}