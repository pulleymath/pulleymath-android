package com.freewheelin.pulley.views.calendarPickerViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CalendarView
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import org.joda.time.LocalDate

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
        val mon = if (month in 0..11) month + 1 else month
        val date = LocalDate(year, mon, day)
        listener?.onSelectDate(this, date)

    }
}