package com.freewheelin.pulley.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import java.lang.ref.WeakReference
import java.text.DecimalFormat

/**
 * Created by kht on 2019. 1. 5..
 */
class TwoLineXAxisRenderer: XAxisRenderer {
    var topMargin = 0f
    var topMaringOnSencond = 0f
    var textSizeOnSecond: Float? = null
    var textColorOnSecond: Int? = null
    val chart: LineChart
    constructor(chart: LineChart): super(chart.viewPortHandler, chart.xAxis, chart.getTransformer(YAxis.AxisDependency.LEFT)) {
        this.chart = chart
    }

    override fun drawLabel(c: Canvas?, formattedLabel: String?, x: Float, y: Float, anchor: MPPointF?, angleDegrees: Float) {
        val line = formattedLabel!!.split("\n")
        if(line.size != 2)
            return

        Utils.drawXAxisValue(c, line[0], x, y + topMargin, mAxisLabelPaint, anchor, angleDegrees)

        val paint = mAxisLabelPaint
        val originTextSize = mAxisLabelPaint.textSize
        val originTextColor = mAxisLabelPaint.color
        paint.color = if(textColorOnSecond != null) textColorOnSecond!! else paint.color
        paint.textSize = if (textSizeOnSecond != null) textSizeOnSecond!!.toPx() else paint.textSize
        paint.typeface = Theme.regular(chart.context)
        Utils.drawXAxisValue(c, line[1], x, y + originTextSize + topMargin + topMaringOnSencond, paint, anchor, angleDegrees)
        mAxisLabelPaint.textSize = originTextSize
        mAxisLabelPaint.color = originTextColor
    }
}


class MarkerView
/**
 * Constructor. Sets up the MarkerView with a custom layout resource.
 *
 * @param context
 * @param layoutResource the layout resource to use for the MarkerView
 */
(context: Context, layoutResource: Int) : RelativeLayout(context), IMarker {

    private var mOffset: MPPointF? = MPPointF()
    private val mOffset2 = MPPointF()
    private var mWeakChart: WeakReference<Chart<*>>? = null

    var chartView: Chart<*>?
        get() = if (mWeakChart == null) null else mWeakChart!!.get()
        set(chart) {
            mWeakChart = WeakReference(chart!!)
        }

    init {
        setupLayoutResource(layoutResource)
    }

    /**
     * Sets the layout resource for a custom MarkerView.
     *
     * @param layoutResource
     */
    private fun setupLayoutResource(layoutResource: Int) {

        val inflated = LayoutInflater.from(context).inflate(layoutResource, this)

        inflated.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        inflated.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        // measure(getWidth(), getHeight());
        inflated.layout(0, 0, inflated.measuredWidth, inflated.measuredHeight)
    }

    fun setOffset(offset: MPPointF) {
        mOffset = offset

        if (mOffset == null) {
            mOffset = MPPointF()
        }
    }

    fun setOffset(offsetX: Float, offsetY: Float) {
        mOffset!!.x = offsetX
        mOffset!!.y = offsetY
    }

    override fun getOffset(): MPPointF? {
        return mOffset
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {

        val offset = offset
        mOffset2.x = offset!!.x
        mOffset2.y = offset.y

        val chart = chartView

        val width = width.toFloat()
        val height = height.toFloat()

        if (posX + mOffset2.x < 0) {
            mOffset2.x = -posX
        } else if (chart != null && posX + width + mOffset2.x > chart.width) {
            mOffset2.x = chart.width.toFloat() - posX - width
        }

        if (posY + mOffset2.y < 0) {
            mOffset2.y = -posY
        } else if (chart != null && posY + height + mOffset2.y > chart.height) {
            mOffset2.y = chart.height.toFloat() - posY - height
        }

        return mOffset2
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {

        measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        layout(0, 0, measuredWidth, measuredHeight)

    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {

        val offset = getOffsetForDrawingAtPoint(posX, posY)

        val saveId = canvas.save()
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, posY + offset.y)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }
}


class RadarMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView
    private val format = DecimalFormat("##0")

    init {

        tvContent = findViewById(R.id.contentTv)
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        tvContent.text = String.format("%s %%", format.format(e!!.y.toDouble()))

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height - 10).toFloat())
    }
}


data class EntryPref(
        val index: Int,
        val position: BalloonMarkerView.BalloonPosition,
        val data: Any
) {
    var titleText: String? = null
}

class BalloonMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource) {
    enum class BalloonPosition {
        top, bottom
    }

    private var tvContent: TextView = findViewById(R.id.contentTv)
    private var balloonIv: ImageView = findViewById(R.id.balloonIv)

    private var position: BalloonPosition? = null

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        var dataPref = e?.data as EntryPref

        position = dataPref.position

        if (position == BalloonPosition.top) {
            if (dataPref.index == 0)
                balloonIv.setImageResource(R.drawable.balloon_top)
            else
                balloonIv.setImageResource(R.drawable.balloon_top_yellow)

            var param = tvContent.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0,0,0,8.toPx())
        } else {
            if (dataPref.index == 0)
                balloonIv.setImageResource(R.drawable.balloon_bottom)
            else
                balloonIv.setImageResource(R.drawable.balloon_bottom_yellow)

            var param = tvContent.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0,8.toPx(),0,0)
        }

        if (dataPref.titleText != null)
            tvContent.text = dataPref.titleText
        else {
            tvContent.text = e.y.toString()
        }



        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        var margin = 12
        var yOffset: Float = if(position == BalloonPosition.top) (-getHeight()).toFloat() - margin else margin.toFloat()
        return MPPointF((-(getWidth() / 2)).toFloat(), yOffset)
    }
}


class RoundChartRenderer(chart: BarDataProvider,
                         animator: ChartAnimator,
                         viewPortHandler: ViewPortHandler,
                         private val mRadius: Float): BarChartRenderer(chart, animator, viewPortHandler) {

    private val mBarShadowRectBuffer = RectF()

    override fun drawDataSet(canvas: Canvas?, dataSet: IBarDataSet?, index: Int) {

        if (canvas == null || dataSet == null) return

        val trans = mChart.getTransformer(dataSet.axisDependency)

        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled) {
            mShadowPaint.color = dataSet.barShadowColor

            val barData = mChart.barData

            val barWidth = barData.barWidth
            val barWidthHalf = barWidth / 2.0f
            var x: Float

            var i = 0
            val count = Math.min(Math.ceil((dataSet.entryCount.toFloat() * phaseX).toDouble()).toInt(), dataSet.entryCount)
            while (i < count) {

                val e = dataSet.getEntryForIndex(i)

                x = e.x

                mBarShadowRectBuffer.left = x - barWidthHalf
                mBarShadowRectBuffer.right = x + barWidthHalf

                trans.rectValueToPixel(mBarShadowRectBuffer)

                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
                    i++
                    continue
                }

                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                    break

                mBarShadowRectBuffer.top = mViewPortHandler.contentTop()
                mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom()

                canvas.drawRoundRect(mBarRect, mRadius, mRadius, mShadowPaint)
                i++
            }
        }

        // initialize the buffer
        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)

        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        val isSingleColor = dataSet.colors.size == 1

        if (isSingleColor) {
            mRenderPaint.color = dataSet.color
        }

        var j = 0
        while (j < buffer.size()) {

            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                j += 4
                continue
            }

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                break

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.color = dataSet.getColor(j / 4)
            }

            canvas.drawRoundRect(RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                    buffer.buffer[j + 3]), mRadius, mRadius, mRenderPaint)

            if (drawBorder) {
                canvas.drawRoundRect(RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3]), mRadius, mRadius, mBarBorderPaint)
            }
            j += 4
        }
    }
}


