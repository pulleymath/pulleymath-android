package com.freewheelin.pulley.legacy.views.bars

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx

class HorizontalBar: View {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var value: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var color: Int = ContextCompat.getColor(context, R.color.gray_300)
        set(value) {
            field = value
            invalidate()
        }

    var bgColor: Int = ContextCompat.getColor(context, R.color.gray_200)
        set(value) {
            field = value
            invalidate()
        }

    var progressColor: Int = ContextCompat.getColor(context, R.color.green_300)
        set(value) {
            field = value
            invalidate()
        }

    var borderColor: Int = ContextCompat.getColor(context, R.color.gray_300)
        set(value) {
            field = value
            invalidate()
        }


    private val radius = 5f.toPx()
    private val strokeWidth = 1f.toPx()

    init {
        value = 0f
    }
    override fun onDraw(canvas: Canvas) {
        val clipPath = Path()
        clipPath.addRoundRect(RectF(canvas.clipBounds), radius + 2, radius + 2, Path.Direction.CW)
        canvas.clipPath(clipPath)

        val paint = Paint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = bgColor
        canvas.drawRoundRect(strokeWidth * 0.5f, strokeWidth * 0.5f, width.toFloat() - strokeWidth * 0.5f, height.toFloat() - strokeWidth * 0.5f, radius, radius, paint)


        val fillPaint = Paint()
        fillPaint.isDither = true
        fillPaint.isAntiAlias = true
        fillPaint.color = progressColor
        fillPaint.style = Paint.Style.FILL
        canvas.drawRect(strokeWidth, strokeWidth * 0.5f, (width.toFloat() - strokeWidth) * value, height.toFloat() - strokeWidth * 0.5f, fillPaint)

        val strokePaint = Paint()

        strokePaint.isDither = true
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth
        strokePaint.color = borderColor
        canvas.drawRoundRect(strokeWidth * 0.5f, strokeWidth * 0.5f, width.toFloat() - strokeWidth * 0.5f, height.toFloat() - strokeWidth * 0.5f, radius, radius, strokePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val w = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize

            MeasureSpec.AT_MOST -> {
                24.toPx()
            }
            else -> {
                widthSize
            }
        }

        val h = when(heightMode) {
            MeasureSpec.EXACTLY -> {
               heightSize
            }
            MeasureSpec.AT_MOST -> {
                190.toPx()
            }
            else -> {
                heightSize
            }
        }

        setMeasuredDimension(w, h)
    }
}