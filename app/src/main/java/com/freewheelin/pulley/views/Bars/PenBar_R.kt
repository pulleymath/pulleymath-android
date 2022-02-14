package com.freewheelin.pulley.views.bars

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.TextUtils
import com.freewheelin.pulley.utils.toPx

class PenBar_R: View {


    var progressColor: Int = ContextCompat.getColor(context, R.color.grey_e0e0e0)
        set(value) {
            field = value
            invalidate()
        }


    var bgColor: Int = ContextCompat.getColor(context, R.color.grey_f2f2f2)
        set(value) {
            field = value
            invalidate()
        }


    var value: Float = 1f
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = ContextCompat.getColor(context, R.color.black_4c4c4c)
        set(value) {
            field = value
            invalidate()
        }

    var textSize: Float = resources.getDimension(R.dimen.sp18)
        set(value) {
            field = value
            invalidate()
        }


    private var barPaint = Paint()
    private var textPaint = Paint()
    private var textWidth = 48.toPx()
    private var space = 8.toPx()

    private val barWidth: Int
        get() = width - textWidth - space

    private var coverDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pen_point)

    private val textRect = Rect()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
    }

    init {
        barPaint.isDither = true
        barPaint.isAntiAlias = true
        barPaint.style = Paint.Style.FILL

        textPaint.isDither = true
        textPaint.isAntiAlias = true
        textPaint.typeface = Theme.bold(context)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        barPaint.color = bgColor
        canvas.drawRoundRect(0f, 0f, barWidth.toFloat(), height.toFloat(), 2f.toPx(), 2f.toPx(),  barPaint)

        barPaint.color = progressColor
        canvas.drawRoundRect(0f, 0f, barWidth * value, height.toFloat(), 2f.toPx(), 2f.toPx(),  barPaint)

        coverDrawable?.setBounds(0,0, 10, height)
        coverDrawable?.draw(canvas)

        val valueText = TextUtils.percentFormat.format(value)
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.getTextBounds(valueText, 0, valueText.length, textRect)

        canvas.drawText(valueText, (barWidth + space).toFloat(), height * 0.5f + textRect.height() * 0.5f, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PenBar)
        progressColor = array.getColor(R.styleable.PenBar_progressColor, progressColor)

        textColor = array.getColor(R.styleable.PenBar_valueLabelColor, textColor)

        value = array.getFloat(R.styleable.PenBar_value, 0.15f)
    }
}