package com.freewheelin.pulley.views.CalendarPickerViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.*
import com.haibin.calendarview.*
import com.haibin.calendarview.Calendar
import kotlinx.android.synthetic.main.view_daebak_calendar.view.*
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

class UntilNowUISetter(val context: Context,
                       override var nextBtn: ImageButton,
                       override var prevBtn: ImageButton) : MonthChangeUISetter {
    override fun setUpUI(year: Int, month: Int) {
        val today = Date()

        if(year == today.year() && month == today.month()) {
            nextBtn.setColorFilter(ContextCompat.getColor(context, R.color.grey_e0e0e0))
            prevBtn.clearColorFilter()
        } else if (year == 2019 && month == 1) {
            prevBtn.setColorFilter(ContextCompat.getColor(context, R.color.grey_e0e0e0))
            nextBtn.clearColorFilter()
        } else {
            nextBtn.clearColorFilter()
            prevBtn.clearColorFilter()
        }
    }
}
class DaebakCalendar : ConstraintLayout,
        View.OnClickListener,
        CalendarView.OnMonthChangeListener,
        CalendarView.OnCalendarRangeSelectListener {

    override fun onCalendarSelectOutOfRange(calendar: Calendar?) { }
    override fun onSelectOutOfRange(calendar: Calendar?, isOutOfMinRange: Boolean) {}
    override fun onCalendarRangeSelect(calendar: Calendar, isEnd: Boolean) {
        val date = LocalDate(calendar.year, calendar.month, calendar.day)
        selectedDate = date
        listener?.onSelectDate(this, date)
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var selectedDate: LocalDate

    var listener: DaebakCalendarListener? = null

    var monthChangeBehavior: MonthChangeUISetter? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_daebak_calendar, this)
        val today = Date()
        calendar.setRange(2019,1,1, today.year(),today.month(),today.day())
        calendar.setOnMonthChangeListener(this)
        calendar.setOnCalendarRangeSelectListener(this)
        calendar.setSelectRangeMode()

        selectedDate = LocalDate.now()
        yearMonthTv.text = "${selectedDate.year}년 ${selectedDate.monthOfYear}월"

        nextBtn.setOnClickListener(this)
        prevBtn.setOnClickListener(this)
    }

    override fun onMonthChange(year: Int, month: Int) {

        yearMonthTv.text = "${year}년 ${month}월"
        monthChangeBehavior?.setUpUI(year, month)
    }


    override fun onClick(view: View?) {
        when (view) {
            prevBtn -> calendar.scrollToPre(true)
            nextBtn -> calendar.scrollToNext(true)
        }
    }

    fun setSelectedDate(date: LocalDate, withScroll: Boolean = false) {
        selectedDate = date
        if (withScroll)
            calendar.scrollToCalendar(date.year, date.monthOfYear, date.dayOfMonth, true)

    }

    fun setUnitlNowMonthUISetter() {
        this.monthChangeBehavior = UntilNowUISetter(context, nextBtn, prevBtn)
    }
}

class SelectCalendarView(context: Context): MonthView(context) {
    private var mRadius: Int = 0
    private var selectCirclePaint = Paint()

    override fun onPreviewHook() {
        super.onPreviewHook()
        mSelectTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
        mSelectTextPaint.typeface = Theme.extraBold(context)
        mSelectTextPaint.textSize = 16.spToPx()

        mCurDayTextPaint.typeface = Theme.light(context)
        mCurDayTextPaint.textSize = 16.spToPx()
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.grey_f2f2f2)

        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.STROKE

        selectCirclePaint.style = Paint.Style.FILL
        selectCirclePaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        selectCirclePaint.isAntiAlias = true
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int, y: Int) {}

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        if (!isSelected) {
            when {
                calendar.isCurrentDay -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
                else -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.black_4c4c4c)
            }
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        }
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2

        val baselineY = mTextBaseLine + y

        canvas.drawCircle(cx.toFloat(), cy.toFloat(), 16.toPx().toFloat(), selectCirclePaint)
        canvas.drawText(
                calendar.day.toString(),
                cx.toFloat(),
                baselineY,
                mSelectTextPaint
        )
        return false
    }
}

class FromCalendarView(context: Context) : RangeMonthView(context) {
    private var mRadius: Int = 0
    private var selectCirclePaint = Paint()

    override fun onPreviewHook() {
        super.onPreviewHook()
        mSelectTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
        mSelectTextPaint.typeface = Theme.extraBold(context)


        mCurDayTextPaint.typeface = Theme.light(context)
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.grey_f2f2f2)

        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.STROKE

        selectCirclePaint.style = Paint.Style.FILL
        selectCirclePaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        selectCirclePaint.isAntiAlias = true
    }


    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        if (!isSelected) {
            when {
                calendar.isCurrentDay -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
                calendar.timeInMillis > Date().time -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.grey_e0e0e0)
                else -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.black_4c4c4c)
            }
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        }
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelectedPre: Boolean, isSelectedNext: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2

        if (isSelectedPre) {
            if (isSelectedNext) {
                canvas.drawRect(x.toFloat(), (cy - mRadius).toFloat(), (x + mItemWidth).toFloat(), (cy + mRadius).toFloat(), mSelectedPaint)
            } else {
                canvas.drawRect(x.toFloat(), (cy - mRadius).toFloat(), cx.toFloat() + (mItemWidth.toFloat() / 2 - mRadius), (cy + mRadius).toFloat(), mSelectedPaint)
                canvas.drawCircle(cx + (mItemWidth.toFloat() / 2 - mRadius), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
            }
        } else {
            if (isSelectedNext) {
                canvas.drawRect(cx.toFloat(), (cy - mRadius).toFloat(), (x + mItemWidth).toFloat(), (cy + mRadius).toFloat(), mSelectedPaint)
            }
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        }

        val baselineY = mTextBaseLine + y

        if (isSelectedPre == false) {
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), 16.toPx().toFloat(), selectCirclePaint)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mSelectTextPaint)
        } else if (calendar.isCurrentDay) {
            mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        } else {
            mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.black_4c4c4c)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        }

        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int, isSelected: Boolean) {}
}

class ToCalendarView(context: Context) : RangeMonthView(context) {
    private var mRadius: Int = 0
    private var selectCirclePaint = Paint()

    override fun onPreviewHook() {
        super.onPreviewHook()
        mSelectTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
        mSelectTextPaint.typeface = Theme.extraBold(context)


        mCurDayTextPaint.typeface = Theme.light(context)
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        mSelectedPaint.color = ContextCompat.getColor(context, R.color.grey_f2f2f2)

        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.STROKE

        selectCirclePaint.style = Paint.Style.FILL
        selectCirclePaint.color = ContextCompat.getColor(context, R.color.purple_ECEBFF)
        selectCirclePaint.isAntiAlias = true
    }


    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        if (!isSelected) {
            when {
                calendar.isCurrentDay -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
                calendar.timeInMillis > Date().time -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.grey_e0e0e0)
                else -> mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.black_4c4c4c)
            }
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        }
    }


    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelectedPre: Boolean, isSelectedNext: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2

        if (isSelectedPre) {
            if (isSelectedNext) {
                canvas.drawRect(x.toFloat(), (cy - mRadius).toFloat(), (x + mItemWidth).toFloat(), (cy + mRadius).toFloat(), mSelectedPaint)
            } else {
                canvas.drawRect(x.toFloat(), (cy - mRadius).toFloat(), cx.toFloat(), (cy + mRadius).toFloat(), mSelectedPaint)
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
            }
        } else {
            if (isSelectedNext) {
                canvas.drawRect(cx.toFloat() - (mItemWidth.toFloat() / 2 - mRadius), (cy - mRadius).toFloat(), (x + mItemWidth).toFloat(), (cy + mRadius).toFloat(), mSelectedPaint)
            }
            canvas.drawCircle(cx.toFloat() - (mItemWidth.toFloat() / 2 - mRadius), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        }

        val baselineY = mTextBaseLine + y

        if (isSelectedNext == false) {
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), 16.toPx().toFloat(), selectCirclePaint)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mSelectTextPaint)
        } else if (calendar.isCurrentDay) {
            mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.purple_6D6DFF)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        } else {
            mCurDayTextPaint.color = ContextCompat.getColor(context, R.color.black_4c4c4c)
            canvas.drawText(calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mCurDayTextPaint)
        }


        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int, isSelected: Boolean) {}
}


class DaebakWeekBar(context: Context) : WeekBar(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_daebakweekbar, this, true)
    }

}