package com.freewheelin.pulley.legacy.views

import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.legacy.views.charts.BarVerticalChartView
import org.junit.Test
import org.junit.Assert.assertEquals

class BarVerticalChartViewTest: ContextTest() {

    val chart = BarVerticalChartView(context)

    @Test
    fun `should return proper bar count`() {
        val data = ArrayList(listOf(
                Triple(1,2,3)
        ))

        chart.setValues(data)
        assertEquals(1, chart.bars?.size)

        data.add(Triple(1,2,3))
        chart.setValues(data)
        assertEquals(2, chart.bars?.size)

        data.add(Triple(1,2,3))
        chart.setValues(data)
        assertEquals(3, chart.bars?.size)

        data.clear()
        chart.setValues(data)
        assertEquals(0, chart.bars?.size)
    }

}