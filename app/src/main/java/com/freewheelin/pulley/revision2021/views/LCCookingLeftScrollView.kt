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
import com.freewheelin.pulley.legacy.views.memoView.MemoView
import java.lang.Float.max
import java.lang.Float.min


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
    var fingerDrawMode = false


    // pattern에서도 이 뷰를 쓰고있으므로 아래의 뷰는 id로 받기보다는 fragment로부터 받아와야 할것같다.
    val leftContentCl by lazy { this.findViewById<ConstraintLayout>(R.id.leftContentWrapperCl) }
    val memoView by lazy { this.findViewById<MemoView>(R.id.memoView) }

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

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if(isBlock) {
            val pp = MotionEvent.PointerProperties()
            ev?.getPointerProperties(0, pp)
            if (!fingerDrawMode && pp.toolType != MotionEvent.TOOL_TYPE_STYLUS) {
                return true
            }
            ev?.pointerCount == 2
        } else {
            true
        }
    }

    override fun onScale(p0: ScaleGestureDetector): Boolean {
        p0.let { detector ->
            scaleFactor *= detector.scaleFactor
            scaleFactor = min(max(scaleFactor, minScale), maxScale)
            setViewScale()
        }
        return true
    }

    fun setViewScale() {
        leftContentCl.scaleX = scaleFactor
        leftContentCl.scaleY = scaleFactor
        memoView.scaleX = scaleFactor
        memoView.scaleY = scaleFactor

    }

    override fun onScroll(event1: MotionEvent?, event2: MotionEvent, xDiff: Float, yDiff: Float): Boolean {
        val minX = getMinX()
        val minY = getMinY()

        if(!touchStart) return true

        val memoMaxX = (scaleFactor - 1) * memoView.measuredWidth * 0.5f
        val memoMaxY = (scaleFactor - 1) * memoView.measuredHeight * 0.5f

        var newX = memoView.x - xDiff
        var newY = memoView.y - yDiff

        newX = min(max(newX, minX), memoMaxX)
        newY = min(max(newY, minY), memoMaxY)

        memoView.x = newX
        memoView.y = newY


        setViewPosition()
        return false
    }


    open fun getIvX(): Float {
        return memoView.x + 0.5f * (1 - scaleFactor) * (memoView.width - leftContentCl.width)
    }

    open fun getIvY(): Float {
        return memoView.y + 0.5f * (1 - scaleFactor) * (memoView.height - leftContentCl.height)
    }

    private fun getMinX(): Float {
        return (1 - scaleFactor)*(memoView.width) * 0.5f
    }

    private fun getMinY(): Float {
        return ((memoView.parent as View).height - memoView.height * scaleFactor) + 0.5f * (scaleFactor - 1) * memoView!!.height
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
    override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        fling(-p3.toInt())
        return true
    }

}