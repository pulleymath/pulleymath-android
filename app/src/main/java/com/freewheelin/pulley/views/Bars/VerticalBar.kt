package com.freewheelin.pulley.views.Bars

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.utils.spToPx
import com.freewheelin.pulley.utils.toPx
import java.text.DecimalFormat

class VerticalBar : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var paint: Paint
    var value: Float
        set(value) {
            field = value
            invalidate()
        }
    var color: Int = Color.BLUE
        set(value) {
            field = value
            invalidate()
        }

    var barHeight = 160f.toPx()
        set(value) {
            field = value
            invalidate()
        }
    var barWidth: Float = resources.getDimension(R.dimen.dp40)
        set(value) {
            field = value
            invalidate()
        }


    var radius = 2f.toPx()
        set(value) {
            field = value
            invalidate()
        }

    var label: String? = null
        set(value) {
            field = value
            invalidate()
        }
    var lowLabel: String? = null
        set(value) {
            field = value
            invalidate()
        }

    private val spaceLabelAndBar = 8f.toPx()
    private val formatter = DecimalFormat("##%")
    private val topExtraSpace = 30f.toPx()
    private val font = ResourcesCompat.getFont(context, R.font.nanum_square_extra_bold)

    init {
        paint = Paint()
        value = 0f
    }

    override fun onDraw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.isAntiAlias = true

        val sideSpace = (getMeasureWidth() - barWidth) * 0.5f
        canvas.drawRoundRect(sideSpace, ((barHeight) * (1f - value)) + topExtraSpace, barWidth + sideSpace, barHeight + topExtraSpace, radius, radius, paint)

        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.typeface = font
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = color


        val existLabel = lowLabel
        if(existLabel != null && value <= 0.4f) {
            textPaint.textSize = resources.getDimension(R.dimen.sp12)
            val strs = existLabel.split("\n").reversed()
            var y = (barHeight * (1f - value) + topExtraSpace - spaceLabelAndBar)
            for (str in strs) {
                canvas.drawText(str, canvas.width / 2f, y, textPaint)
                val r = Rect()
                textPaint.getTextBounds(str, 0, str.length, r)
                y -= r.height()
                y -= 6.spToPx()
            }

            y -= 2.spToPx()
            textPaint.textSize = resources.getDimension(R.dimen.sp16)
            canvas.drawText(label ?: "${formatter.format(value)}", canvas.width / 2f, y, textPaint)
        } else {
            textPaint.textSize = resources.getDimension(R.dimen.sp16)
            canvas.drawText(label ?: "${formatter.format(value)}", canvas.width / 2f, (barHeight * (1f - value) + topExtraSpace - spaceLabelAndBar), textPaint)
        }





    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(getMeasureWidth().toInt(), (barHeight + topExtraSpace).toInt())
    }

    private fun getMeasureWidth(): Float {
        return barWidth + if (context.is10InchUI) 5.toPx() else 7.toPx()
    }

}
