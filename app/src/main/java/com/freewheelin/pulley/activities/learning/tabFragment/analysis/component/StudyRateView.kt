package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import java.lang.Math.round
import kotlin.math.roundToInt

class StudyRateView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var ratePaint = Paint()
    private var penPaint = Paint()
    val outerRect: RectF
    val innerRect: RectF

    var values: List<Int> = emptyList()
    set(value) {
        field = value
        invalidate()
    }
    val sum: Int
    get() = values.fold(0) { acc, i -> acc + i }


    init {
        outerRect = RectF()
        innerRect = RectF()

        penPaint.isDither = true
        penPaint.isAntiAlias = true
        penPaint.textSize = resources.getDimension(R.dimen.sp24)
        penPaint.color = ContextCompat.getColor(context, R.color.gray_500)
        penPaint.typeface = Theme.extraBold(context)
        penPaint.textAlign = Paint.Align.CENTER
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ratePaint.isAntiAlias = true
        outerRect.set(0f,0f, width.toFloat(), height.toFloat())
        innerRect.set(width * 0.25f, height * 0.25f, width * 0.75f, height * 0.75f)

        if(values.size == 0 || sum == 0) {
            ratePaint.color = ContextCompat.getColor(context, R.color.gray_400)
            canvas.drawArc(outerRect, 180f, 180f, true, ratePaint)
        } else {
            ratePaint.color = ContextCompat.getColor(context, R.color.gray_400)
            canvas.drawArc(outerRect, 180f, 180f * values.first() / sum, true, ratePaint)
            ratePaint.color = ContextCompat.getColor(context, R.color.yellow_300)
            canvas.drawArc(outerRect, 180 + (180f * values.first() / sum), 180f - (180f * values.first()) / sum, true, ratePaint)
        }

        ratePaint.color = ContextCompat.getColor(context, R.color.white)
        canvas.drawArc(innerRect, 180f, 180f, true, ratePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when(widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            else -> {
                resources.getDimension(R.dimen.dp200).toInt()
            }
        }

        val height = when(heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            else -> {
                resources.getDimension(R.dimen.dp200).toInt()
            }
        }
        setMeasuredDimension(width, height)
    }




}