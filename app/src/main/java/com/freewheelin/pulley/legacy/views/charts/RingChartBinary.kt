package com.freewheelin.pulley.legacy.views.charts

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.freewheelin.pulley.legacy.utils.toPx
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.ArrayList

class RingChartBinary: PieChart {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs : AttributeSet): super(context, attrs)

    val correctColor = Color.parseColor("#78beff")
    var wrongColor = Color.parseColor("#fe7b67")

    init {
//        setUsePercentValues(true)
        description.isEnabled = false
        isRotationEnabled = false
        legend.isEnabled = false

        setDrawSlicesUnderHole(false)
        setDrawEntryLabels(false)

        holeRadius = 75f // 이거 퍼센트네

        // chart.spin(2000, 0, 360);

        val l = getLegend().also {
            it.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            it.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            it.orientation = Legend.LegendOrientation.VERTICAL
            it.run {
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 0f
            }
        }
    }

    fun setData(left:Float, right:Float, leftColor:Int=wrongColor, rightColor:Int=correctColor) {

        animateY(670, Easing.EasingOption.EaseInOutQuad)
        dragDecelerationFrictionCoef = 0.95f

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(right))
        entries.add(PieEntry(left))

        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 4f

        val colors = listOf(rightColor, leftColor)
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setDrawValues(false)
        setData(data)
        highlightValue(null)
        postInvalidate()
    }
}