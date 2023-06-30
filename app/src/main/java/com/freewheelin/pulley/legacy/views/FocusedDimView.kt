package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.getTargetAbsolutePosition
import com.freewheelin.pulley.legacy.utils.toPx

interface FocusedDimViewListener {
    fun onTargetTouch()
    fun onOutsideTouch() {}
}
class FocusedDimView: View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    lateinit var bitmap: Bitmap
    var cv: Canvas? = null
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var listener: FocusedDimViewListener? = null

    var defaultRadius = 5f.toPx()
    var targetRadius: List<Float> = emptyList()
    var targetViews: List<View> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    init {
        elevation = 10f.toPx()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if(w != oldw || h != oldh) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            cv = Canvas(bitmap)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        cv?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val outerRectangle = RectF(0f, 0f, width.toFloat(), height.toFloat())

        paint.color = ContextCompat.getColor(context, R.color.black50_000000)
        cv?.drawRect(outerRectangle, paint)

        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)


        for (i in targetViews.indices) {
            val view = targetViews[i]
            val radius = targetRadius.getOrNull(i) ?: defaultRadius
            val (left, top) = view.getTargetAbsolutePosition(false)
            val right = left + view.measuredWidth.toFloat()
            val bottom = top + view.measuredHeight.toFloat()
            cv?.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }
        canvas.drawBitmap(bitmap,0f,0f,null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_DOWN) {
            var isInTarget = isTargetTouch(event.x, event.y)
            if(isInTarget) {
                listener?.onTargetTouch()
                return false
            } else {
                listener?.onOutsideTouch()
                return true
            }
        } else {
            return true
        }
    }


    fun bright() {
        cv?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }


    fun isTargetTouch(x: Float, y: Float): Boolean {
        for (view in targetViews) {
            val (left, top) = view.getTargetAbsolutePosition(false)
            val right = left + view.measuredWidth.toFloat()
            val bottom = top + view.measuredHeight.toFloat()

            if(x in left..right && y >= top && y <= bottom) {
                return true
            }
        }
        return false
    }
}