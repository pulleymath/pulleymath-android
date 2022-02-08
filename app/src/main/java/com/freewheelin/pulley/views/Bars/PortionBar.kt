package com.freewheelin.pulley.views.bars

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.TextUtils
import com.freewheelin.pulley.utils.toPx

class PortionBar : View {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var firstValue: Float = 20f

    var secondValue: Float = 20f

    var thirdValue: Float = 15f

    var fourthValue: Float = 15f

    var fifthValue: Float = 30f


    private val sum: Float
        get() = firstValue + secondValue + thirdValue + fourthValue + fifthValue

    private val firstRatio: Float
        get() = firstValue / sum

    private val secondRatio: Float
        get() = secondValue / sum

    private val thirdRatio: Float
        get() = thirdValue / sum

    private val fourthRatio: Float
        get() = fourthValue / sum

    private val fifthRatio: Float
        get() = fifthValue / sum

    var font = Theme.regular(context)
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val clipPath = Path()
        clipPath.addRoundRect(RectF(canvas.clipBounds), 5f.toPx(), 5f.toPx(), Path.Direction.CW)
        canvas.clipPath(clipPath)

        val paint = Paint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        val penPaint = Paint()
        penPaint.isDither = true
        penPaint.isAntiAlias = true
        penPaint.textSize = resources.getDimension(R.dimen.sp16)
        penPaint.typeface = font
        penPaint.textAlign = Paint.Align.CENTER

        val firstWidth = width * firstRatio
        paint.color = Color.parseColor("#e1f1fe")
        canvas.drawRect(0f, 0f, firstWidth, height.toFloat(), paint)
        penPaint.color = Color.parseColor("#30a4ff")
        val firstRatioText = TextUtils.percentFormat.format(firstRatio)
        val r = Rect()
        paint.getTextBounds(firstRatioText, 0, firstRatioText.length, r)
        if(firstRatio >= 0.03)
            canvas.drawText(firstRatioText, firstWidth / 2, (height.toFloat() / 2) + r.height()/2, penPaint)

        val secondWidth = width * secondRatio
        paint.color = Color.parseColor("#b9defe")
        canvas.drawRect(firstWidth, 0f, firstWidth + secondWidth, height.toFloat(), paint)
        if(secondRatio >= 0.03)
            canvas.drawText(TextUtils.percentFormat.format(secondRatio), (firstWidth + secondWidth + firstWidth) / 2, height.toFloat() / 2f + r.height()/2, penPaint)

        val thirdWidth = width * thirdRatio
        paint.color = Color.parseColor("#78beff")
        canvas.drawRect(firstWidth + secondWidth, 0f, firstWidth + secondWidth + thirdWidth, height.toFloat(), paint)
        penPaint.color = Color.WHITE
        if(thirdRatio >= 0.03)
            canvas.drawText(TextUtils.percentFormat.format(thirdRatio), (firstWidth + secondWidth + thirdWidth + secondWidth + firstWidth) / 2, height.toFloat() / 2 + r.height()/2, penPaint)

        val fourthWidth = width * fourthRatio
        paint.color = Color.parseColor("#30a4ff")
        canvas.drawRect(firstWidth + secondWidth + thirdWidth, 0f, firstWidth + secondWidth + thirdWidth + fourthWidth, height.toFloat(), paint)
        if(fourthRatio >= 0.03)
            canvas.drawText(TextUtils.percentFormat.format(fourthRatio), (firstWidth + secondWidth + thirdWidth + fourthWidth + thirdWidth +secondWidth + firstWidth) / 2, height.toFloat() / 2 + r.height()/2, penPaint)

        val fifthWidth = width * fifthRatio
        paint.color = Color.parseColor("#2287ef")
        canvas.drawRect(firstWidth + secondWidth + thirdWidth + fourthWidth, 0f, firstWidth + secondWidth + thirdWidth + fourthWidth + fifthWidth, height.toFloat(), paint)
        if(fifthRatio >= 0.03)
            canvas.drawText(TextUtils.percentFormat.format(fifthRatio), (firstWidth + secondWidth + thirdWidth + fourthWidth + fifthWidth + fourthWidth + thirdWidth + secondWidth + firstWidth) / 2, height.toFloat() / 2 + r.height()/2, penPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(widthSize, heightSize)
    }

    fun setValues(first: Int, second: Int, third: Int, fourth: Int, fifth: Int, withAnim: Boolean = false) {
        if(!withAnim) {
            firstValue = first.toFloat()
            secondValue = second.toFloat()
            thirdValue = third.toFloat()
            fourthValue = fourth.toFloat()
            fifthValue = fifth.toFloat()
            invalidate()
        } else {
            val firstOrigin = firstValue
            val secondOrigin = secondValue
            val thirdOrigin = thirdValue
            val fourthOrigin = fourthValue
            val fifthOrigin = fifthValue

            val firstDiff = first - firstValue
            val secondDiff = second - secondValue
            val thirdDiff = third - thirdValue
            val fourthDiff = fourth - fourthValue
            val fifthDiff = fifth - fifthValue

            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.duration = 500
            anim.addUpdateListener {
                val animVal = it.animatedValue as Float
                firstValue = firstOrigin + (firstDiff * animVal)
                secondValue = secondOrigin + (secondDiff * animVal)
                thirdValue = thirdOrigin + (thirdDiff * animVal)
                fourthValue = fourthOrigin + (fourthDiff * animVal)
                fifthValue = fifthOrigin + (fifthDiff * animVal)
                invalidate()
            }
            anim.start()
        }
    }
}