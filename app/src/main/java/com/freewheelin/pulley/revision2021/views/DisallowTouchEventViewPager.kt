package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2

class DisallowTouchEventViewPager : LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    var pager: ViewPager2
    init {
        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val vp = ViewPager2(context)
        vp.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        vp.id = View.generateViewId()
        this.addView(vp)
        pager = vp
        pager.isSaveEnabled = false
    }

    var initialXValue: Float = 0f // as we have to detect swipe to right
    var prevXValue: Float = 0f

    var rightMoveCount = arrayListOf<Unit>()
    var leftMoveCount = arrayListOf<Unit>()

    var isPagerSwipeBlocked = false
    var scaleFactor = 1f // nested scroll 을 적용할때 scaleFactor가 1일때만 최상단 viewpager가 스와이프 되도록 하였다.

    var pagerEnableCallback: () -> Unit = {}
//    var pagerDisabledDebounce: (u: Unit) -> Unit = {}

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        val disallowFlag = if (isPageEndIndex || isPageStartIndex) {

            var returnValue = true

            event?.let {
                if (it.action == MotionEvent.ACTION_DOWN) {
                    initialXValue = event.x
                }
                if (it.action == MotionEvent.ACTION_UP) {
                    initialXValue = 0f
                    rightMoveCount.clear()
                    leftMoveCount.clear()
                }
                if (it.action == MotionEvent.ACTION_MOVE) {
                    val diffX = event.x - initialXValue
                    if (diffX < 0 && prevXValue > event.x) {
                        // right
                        rightMoveCount.add(Unit)
                        if (rightMoveCount.size > 3) {
                            // 오른쪽 무빙
                            if (isPageEndIndex) {
                                if (scaleFactor == 1f) {
                                    println("xjcl, lc패턴 오른쪽무빙! ")
                                    pagerEnableCallback()
                                    returnValue = false
                                }
                            }
                        }
                    } else if (diffX < 0) {
                        rightMoveCount.clear()
                    }
                    if (diffX > 0 && prevXValue < event.x) {
                        // left
                        leftMoveCount.add(Unit)
                        if (leftMoveCount.size > 3) {
                            // 왼쪽 무빙
                            if (isPageStartIndex) {
                                if (scaleFactor == 1f) {
                                    println("xjcl, lc패턴 왼쪽무빙! ")
                                    pagerEnableCallback()
                                    returnValue = false
                                }
                            }
                        }
                    } else if (diffX > 0) {
                        leftMoveCount.clear()
                    }
                    prevXValue = event.x
                }
            }

            returnValue = when (event?.pointerCount) {
                1 -> { isPagerSwipeBlocked || returnValue } // returnvalue true 일때
                2 -> {
                    pager.isUserInputEnabled = !isPagerSwipeBlocked
                    true
                }
                3 -> { returnValue}
                else -> { false }
            }

            returnValue
        } else {
            true
        }

        println("xjcl, returnValue : ${disallowFlag}, ${event?.pointerCount}")
        parent.requestDisallowInterceptTouchEvent(disallowFlag) // false면 최상단 뷰페이저 스와이프가 가능, true 면 현재 뷰페이저 스와이프 가능
        return super.dispatchTouchEvent(event)
//        return disallowFlag
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