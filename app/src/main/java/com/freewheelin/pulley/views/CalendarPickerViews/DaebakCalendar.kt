package com.freewheelin.pulley.views.calendarPickerViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.*
import org.joda.time.LocalDate
import java.util.*

interface DaebakCalendarListener {
    fun onSelectDate(calendar: DaebakCalendar, date: LocalDate)
}

interface MonthChangeUISetter {
    var nextBtn: ImageButton
    var prevBtn: ImageButton
    fun setUpUI(year: Int, month: Int)
}

class DaebakCalendar : ConstraintLayout, CalendarView.OnDateChangeListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var listener: DaebakCalendarListener? = null
    var calendar: CalendarView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_daebak_calendar, this)

        calendar = findViewById(R.id.calendar)
        calendar.setOnDateChangeListener(this)
    }

    override fun onSelectedDayChange(p0: CalendarView, year: Int, month: Int, day: Int) {
        println("tpehf, year: ${year}, month : $month, day : $day")
        val date = LocalDate(year, month, day)
        listener?.onSelectDate(this, date)

    }
}