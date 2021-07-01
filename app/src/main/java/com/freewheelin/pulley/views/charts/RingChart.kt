package com.freewheelin.pulley.views.charts

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.ArrayList

class RingChart: PieChart {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs : AttributeSet): super(context, attrs)

    val legendColors = listOf(
            Color.parseColor("#6d6dff"),
            Color.parseColor("#acacff"),
            Color.parseColor("#ecebff"),
            Color.parseColor("#ffd545"),
            Color.parseColor("#ffb300"),
            Color.parseColor("#fe7b67")
    )


    init {
//        setUsePercentValues(true)
        description.isEnabled = false
        isRotationEnabled = false
        legend.isEnabled = false


        setDrawSlicesUnderHole(false)
        setDrawEntryLabels(false)
        holeRadius = 90f



        animateY(670, Easing.EasingOption.EaseInOutQuad)
        dragDecelerationFrictionCoef = 0.95f
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

    fun setData(count: Int, range: Float) {
        val entries = ArrayList<PieEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until count) {
            entries.add(PieEntry((Math.random() * range + range / 5).toFloat()))
        }

        val dataSet = PieDataSet(entries, "Election Results")

        dataSet.sliceSpace = 8f


        dataSet.colors = legendColors
        //dataSet.setSelectionShift(0f);

        val data = PieData(dataSet)
        data.setDrawValues(false)
        setData(data)
        highlightValues(null)
        invalidate()
    }

    fun setData(values: List<Int>) {
        val entries = ArrayList<PieEntry>()
        values.forEach {
            entries.add(PieEntry(it.toFloat()))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 8f
        dataSet.colors = legendColors

        val data = PieData(dataSet)
        data.setDrawValues(false)
        setData(data)
        highlightValue(null)
        invalidate()

    }
}