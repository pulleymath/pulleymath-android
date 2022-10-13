package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import kotlin.math.abs

open class QuizGestures(context: Context, val scaleControlledView: View, val memoView: CookingMemoView) : View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {
    private val gesture: GestureDetector = GestureDetector(context, this)
    private val gestureScale: ScaleGestureDetector = ScaleGestureDetector(context, this)

    var scaleFactor = 1f
    private var inScale = false
    private var touchStart = false

    private val minScale = 1f
    private val maxScale = 3f

    val minDistance = 45
//    var listener: ProblemGestureListener? = null
    var didSwipe: Boolean = false
    var blockSwipe: Boolean = false
    var isBlock = false
    var isCookingScaleChangeEnabled = false

    lateinit var setUserInputOfCookingQuizEnabled: (enabled: Boolean) -> Unit
    lateinit var scrollEndCallback: (u: Unit) -> Unit


    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        isCookingScaleChangeEnabled = event?.pointerCount == 2

        if(event != null)
            gesture.onTouchEvent(event)


        logTouchEvetn(event)

        if(event?.action == MotionEvent.ACTION_DOWN) {
            didSwipe = false
            blockSwipe = false
        }
        touchStart = event?.action != MotionEvent.ACTION_UP

        if(scaleFactor > 1f) {
            setUserInputOfCookingQuizEnabled(false)
            scrollEndCallback(Unit)
        } else {

        }
        // 0, down    2: move   1: up
        when (event?.action) {
            0 -> {
                xScroll = 0
                yScroll = 0
            }
            else -> {}
        }


        if (event?.pointerCount == 2 && (isBlock || isCookingScaleChangeEnabled)) {
            gestureScale.onTouchEvent(event)
            return false
        }

        return true
    }

    var xScroll: Int = 0
    var yScroll: Int = 0

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, x: Float, y: Float): Boolean {
        return true
    }

    override fun onLongPress(event: MotionEvent) {}

    private fun yScrollControl(x: Float, y: Float) {
        if (scaleFactor == 1f) {
            if (abs(x) < abs(y)) {
                yScroll += 1
            } else {
                xScroll += 1
            }
            if (yScroll > xScroll) {
                setUserInputOfCookingQuizEnabled(false)
                scrollEndCallback(Unit)
            }
        }
    }
    override fun onScroll(event1: MotionEvent, event2: MotionEvent, x: Float, y: Float): Boolean {
        yScrollControl(x, y)

        val minX = getMinX()
        val minY = getMinY()

        if(!touchStart)
            return true

        val beforeX = memoView.x
        var newMemoX = memoView.x
        var newMemoY = memoView.y
        val memoMaxX = (memoView.measuredWidth * scaleFactor - memoView.measuredWidth) * 0.5f
        val memoMaxY = (memoView.measuredHeight * scaleFactor - memoView.measuredHeight) * 0.5f
        newMemoX -= x
        newMemoY -= y


        var newX = beforeX - x

        if(newX > memoMaxX) {
            newX = memoMaxX
        } else if(newX < minX) {
            newX = minX
        }

        if(newMemoY > memoMaxY)
            newMemoY = memoMaxY
        else if(newMemoY < minY)
            newMemoY = minY

        memoView.x = newX
        memoView.y = newMemoY
        setViewPosition()

        if(beforeX != newX)
            blockSwipe = true

        if((beforeX - x) > memoMaxX + minDistance && !didSwipe && !inScale && !blockSwipe) {
//            listener?.onLeftSwipe()
            didSwipe = true
        }

        if(beforeX - x < minX - minDistance && !didSwipe && !inScale && !blockSwipe) {
//            listener?.onRightSwipe()
            didSwipe = true
        }

        return false
    }

    override fun onShowPress(event: MotionEvent) {}

    override fun onSingleTapUp(event: MotionEvent): Boolean { return true }

    override fun onDoubleTap(event: MotionEvent): Boolean { return true }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean { return true }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean { return true }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        Log.d("[EVENT]", "scale")

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

        if(scaleFactor != beforeScaleFactor)
            blockSwipe = true
        setViewPosition()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        Log.d("[EVENT]", "scale BEGIN")
        inScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.d("[EVENT]", "scale END")
        inScale = false
    }

    open fun getIvX(): Float {
        return memoView.x + 0.5f * (1 - scaleFactor) * (memoView.measuredWidth - scaleControlledView.measuredWidth)
    }

    open fun getIvY(): Float {
        return memoView.y + 0.5f * (1 - scaleFactor) * (memoView.measuredHeight - scaleControlledView.measuredHeight)
    }

    private fun getMinX(): Float {
        return (1 - scaleFactor)*(memoView.measuredWidth) * 0.5f
    }

    private fun getMinY(): Float {
        return ((memoView.parent.parent as View).height - memoView.measuredHeight * scaleFactor) + 0.5f * (scaleFactor - 1) * memoView!!.measuredHeight
    }

    fun logTouchEvetn(ev: MotionEvent?) {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("[EVENT]", "ACTION DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("[EVENT]", "ACTION MOVE")
            MotionEvent.ACTION_BUTTON_PRESS -> Log.d("[EVENT]", "ACTION PRESS")
            MotionEvent.ACTION_BUTTON_RELEASE -> Log.d("[EVENT]", "ACTION RELEASE")
            MotionEvent.ACTION_CANCEL -> Log.d("[EVENT]", "ACTION CANCEL")
            MotionEvent.ACTION_UP -> Log.d("[EVENT]", "ACTION UP")

        }
    }

    open fun setViewPosition() {
        scaleControlledView.x = getIvX()
        scaleControlledView.y = getIvY()
    }

    open fun setViewScale() {
        scaleControlledView.scaleX = scaleFactor
        scaleControlledView.scaleY = scaleFactor

        memoView.scaleX = scaleFactor
        memoView.scaleY = scaleFactor
    }

    open fun init() {
        scaleControlledView.scaleX = 1f
        scaleControlledView.scaleY = 1f
        scaleControlledView.x = 0f
        scaleControlledView.y = 0f
        memoView.scaleX = 1f
        memoView.scaleY = 1f
        memoView.x = 0f
        memoView.y = 0f
        scaleFactor = 1f
    }
}
