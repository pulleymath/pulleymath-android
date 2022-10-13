package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import java.io.IOException


class LCCookingLeftScrollView: ScrollView,
    GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var isBlock = false
    var scaleFactor = 1f
    private val minScale = 1f
    private val maxScale = 3f
    private val gestureScale: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private val gesture: GestureDetector = GestureDetector(context, this)
    private var touchStart = false


    // pattern에서도 이 뷰를 쓰고있으므로 아래의 뷰는 id로 받기보다는 fragment로부터 받아와야 할것같다.
    val leftContentCl by lazy { this.findViewById<ConstraintLayout>(R.id.leftContentWrapperCl) }
    val memoView by lazy { this.findViewById<CookingMemoView>(R.id.cookingMemoView) }

    // 문제이미지가 화면사이즈보다 클때 y 스크롤의 양이 그렇지 않을때보다 큼. (손가락으로 이동한만큼보다 더 큰 량이 이미지 스크롤이됨
    //
    // super.onTouchEvent를 제거하고 onfling 내부에서 fling함수를 통해 해보려고 했으나
    // on scroll에서는 상하 스크롤시 좌우 민감도가 떨어지지않아서 맘에들지 않는다.

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        try {
            if (ev != null) { gesture.onTouchEvent(ev) }
        } catch (e: NullPointerException) {
            println("Error: ${e}")
        }

        touchStart = ev?.action != MotionEvent.ACTION_UP

        if (scaleFactor > 1f) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        if (ev?.pointerCount == 2) {
            gestureScale.onTouchEvent(ev)
        }
        when (ev?.action) {
            MotionEvent.ACTION_MOVE -> {
                if (ev.pointerCount == 2) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    return false
                }
            }
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        super.onTouchEvent(ev)
        return true
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }


    // 현재 필기중일때 -> isBlock true -> 두손가락일때만 좌우 스와이프 가능
    // 아닐때 return true,

    // false 이어야 메모뷰로 넘어감
    // true 일때 scale처리를 해야함
    //
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if(isBlock) {
            ev?.pointerCount == 2
        } else {
            true
        }
    }

    override fun onScale(p0: ScaleGestureDetector): Boolean {
        p0?.let { detector ->
            val beforeScaleFactor = scaleFactor
            scaleFactor *= detector.scaleFactor

            scaleFactor = if(scaleFactor < minScale)
                minScale
            else if(scaleFactor > maxScale)
                maxScale
            else scaleFactor

            setViewScale()

            val changedMemoWidth =  memoView.measuredWidth * scaleFactor - memoView.measuredWidth * beforeScaleFactor
            val changedMemoHeight = memoView.measuredHeight * scaleFactor - memoView.measuredHeight * beforeScaleFactor

            if(scaleFactor > beforeScaleFactor) {
                memoView.x += changedMemoWidth * 0.5f
                memoView.y += changedMemoHeight * 0.5f
            } else {
                val minX = getMinX()
                val minY = getMinY()

                var newMemoX = memoView.x + changedMemoWidth * 0.5f
                var newMemoY = memoView.y + changedMemoHeight * 0.5f

                if(newMemoX < minX)
                    newMemoX = minX
                if(newMemoY < minY)
                    newMemoY = minY

                memoView.y = newMemoY
                memoView.x = newMemoX
            }

            setViewPosition()
        }
        return true
    }

    fun setViewScale() {
        leftContentCl.scaleX = scaleFactor
        leftContentCl.scaleY = scaleFactor
        memoView.scaleX = scaleFactor
        memoView.scaleY = scaleFactor
    }
    override fun onScroll(event1: MotionEvent, event2: MotionEvent, x: Float, y: Float): Boolean {
        val minX = getMinX()
        val minY = getMinY()

        if(!touchStart) return true

        val beforeX = memoView.x
        var newMemoX = memoView.x
        val memoMaxX = (memoView.measuredWidth * scaleFactor - memoView.measuredWidth) * 0.5f
        newMemoX -= x
        var newX = beforeX - x

        if(newX > memoMaxX) {
            newX = memoMaxX
        } else if(newX < minX) {
            newX = minX
        }
        memoView.x = newX


        var newMemoY = memoView.y
        val memoMaxY = (memoView.measuredHeight * scaleFactor - memoView.measuredHeight) * 0.5f
        newMemoY -= y

        if (newMemoY > memoMaxY) {
            newMemoY = memoMaxY
        } else if (newMemoY < minY) {
            newMemoY = minY
        }

        memoView.y = newMemoY


        setViewPosition()
        return false
    }


    open fun getIvX(): Float {
        return memoView.x + 0.5f * (1 - scaleFactor) * (memoView.measuredWidth - leftContentCl.measuredWidth)
    }

    open fun getIvY(): Float {
        return memoView.y + 0.5f * (1 - scaleFactor) * (memoView.measuredHeight - leftContentCl.measuredHeight)
    }

    private fun getMinX(): Float {
        return (1 - scaleFactor)*(memoView.measuredWidth) * 0.5f
    }

    private fun getMinY(): Float {
        return ((memoView.parent.parent as View).height - memoView.measuredHeight * scaleFactor) + 0.5f * (scaleFactor - 1) * memoView!!.measuredHeight
    }

    // 쓸일없으면 지우자
    fun isParentHeightSameAsMemoHeight(): Boolean {
        return (memoView.parent.parent as View).height == memoView.measuredHeight
    }

    open fun setViewPosition() {
        leftContentCl.x = getIvX()
        leftContentCl.y = getIvY()
    }
    override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector) { }
    override fun onDown(p0: MotionEvent): Boolean { return true }
    override fun onShowPress(p0: MotionEvent) {}
    override fun onSingleTapUp(p0: MotionEvent): Boolean { return true }
    override fun onLongPress(p0: MotionEvent) {}
    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        fling(-p3.toInt())
        return true
    }

}