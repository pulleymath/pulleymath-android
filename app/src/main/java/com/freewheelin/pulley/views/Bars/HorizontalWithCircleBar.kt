package com.freewheelin.pulley.views.Bars

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.bar_horizontal_with_circle.view.*

class HorizontalWithCircleBar : ConstraintLayout {
    var value: Float = 0f
        set(value) {
            field = value
            clipView.value = value
            setUI()
        }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.bar_horizontal_with_circle, this)
    }

    fun setUI() {
        if(value <= 0f)
            fillView.visibility = View.INVISIBLE
        else {
            fillView.visibility = View.VISIBLE
            fillView.layoutParams.width = (measuredWidth * value).toInt()
            fillView.requestLayout()
        }
    }

}


class HorizontalWithCircleBarClip : View {

    var eraser: Paint? = null
    lateinit var fillPaint: Paint
    lateinit var bm: Bitmap
    var cv: Canvas? = null

    var value: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : super(context) {
        initUI()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initUI()
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initUI()
    }

    private fun initUI() {

        fillPaint = Paint()
        fillPaint!!.isAntiAlias = true
        fillPaint!!.color = ContextCompat.getColor(context, R.color.purple_6e6cff)
        eraser = Paint()
        eraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        eraser!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            cv = Canvas(bm)

        }
        super.onSizeChanged(w, h, oldw, oldh)
    }


    override fun onDraw(canvas: Canvas) {
        eraser?.let { eraser ->
            cv?.drawColor(Color.RED, PorterDuff.Mode.CLEAR)
            eraser?.color = Color.WHITE
            val outerRectangle = RectF(0f, 0f, width.toFloat(), height.toFloat())
            cv?.drawRect(outerRectangle, eraser)

            eraser?.color = Color.TRANSPARENT
            eraser?.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)

            val lineHeight = 3.toPx()
            val top = (height - lineHeight) / 2f
            val bottom = top + lineHeight
            cv?.drawRect(0f, top, width.toFloat(), bottom, eraser)

            val radius = 8f.toPx()
            val spaceAmongCircles = (width - (radius * 2) * 6) / 5f

            // draw circles

            cv?.drawCircle(radius, height.toFloat() / 2, radius, if(value > 0) fillPaint else eraser)
            cv?.drawCircle(radius * 3 + spaceAmongCircles, height.toFloat() / 2, radius, if(value >= 0.195) fillPaint else eraser)
            cv?.drawCircle(radius * 5 + spaceAmongCircles * 2, height.toFloat() / 2, radius, if(value >= 0.395) fillPaint else eraser)
            cv?.drawCircle(radius * 7 + spaceAmongCircles * 3, height.toFloat() / 2, radius, if(value >= 0.595) fillPaint else eraser)
            cv?.drawCircle(radius * 9 + spaceAmongCircles * 4, height.toFloat() / 2, radius, if(value >= 0.795) fillPaint else eraser)
            cv?.drawCircle(radius * 11 + spaceAmongCircles * 5, height.toFloat() / 2, radius, if(value >= 1) fillPaint else eraser)
            canvas.drawBitmap(bm, 0f, 0f, null)
        }
    }
}