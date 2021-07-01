package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.CalendarPickerViews.*
import kotlinx.android.synthetic.main.view_daebak_calendar.view.*
import kotlinx.android.synthetic.main.view_daebak_date_range_picker.*
import org.joda.time.LocalDate


interface DateRangePickerDialogListener {
    fun onCancelClicked(picker: DateRangePickerDialog) {}
    fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

class DateRangePickerDialog(context: Context, from: LocalDate, to: LocalDate, firstDate: LocalDate) : Dialog(context), View.OnClickListener, DaebakCalendarListener {
    enum class Type {
        RECENT7, RECENT14, RECENT30, CUSTOM
    }

    override fun onSelectDate(calendar: DaebakCalendar, date: LocalDate) {
        selectRangeBtn.performClick()
        setRange()
        setRangeGuideText()
        updateBtn.toEnableUI()
    }

    var toastHandler = Handler()
    var listener: DateRangePickerDialogListener? = null
    val period: Int
        get() = DateTimeUtils.getPeriod(from, to)

    var firstDate: LocalDate

    var from: LocalDate
        set(value) {
            updateBtn.toEnableUI()
            fromCalendar.setSelectedDate(value, true)
            setRange()
        }
        get() = fromCalendar.selectedDate

    var to: LocalDate
        set(value) {
            updateBtn.toEnableUI()
            toCalendar.setSelectedDate(value, true)
            setRange()
        }
        get() = toCalendar.selectedDate

    init {
        setContentView(R.layout.view_daebak_date_range_picker)
        initUI()
        this.from = from
        this.to = to
        this.firstDate = firstDate
    }

    fun initUI() {
        fromCalendar.calendar.setMonthView(FromCalendarView::class.java)
        toCalendar.calendar.setMonthView(ToCalendarView::class.java)
        fromCalendar.listener = this
        toCalendar.listener = this
        fromCalendar.setUnitlNowMonthUISetter()
        toCalendar.setUnitlNowMonthUISetter()

        selectRangeBtn.setOnClickListener(this)
        aWeekRangeBtn.setOnClickListener(this)
        twoWeeksRangeBtn.setOnClickListener(this)
        aMonthBtn.setOnClickListener(this)
//        totalRangeBtn.setOnClickListener(this)

        onClick(aWeekRangeBtn)
        updateBtn.toDisableUI()
        updateBtn.setOnClickListener {
            if (updateBtn.isEnableUI() == false) {
                showDateRangeErrToast("날짜 선택을 완료해주세요.")
            } else if (from > to) {
                showDateRangeErrToast()
            } else {
                dismiss()
                listener?.onUpdateClicked(this, from, to, getSelected())
            }
        }
        cancelBtn.setOnClickListener {
            dismiss()
            listener?.onCancelClicked(this)
        }
    }

    fun getSelected() : Type {
        return when {
            aWeekRangeBtn.isSelected -> Type.RECENT7
            twoWeeksRangeBtn.isSelected -> Type.RECENT14
            aMonthBtn.isSelected -> Type.RECENT30
            else -> Type.CUSTOM
        }
    }

    override fun onClick(view: View?) {
        selectRangeBtn.isSelected = false
        aWeekRangeBtn.isSelected = false
        twoWeeksRangeBtn.isSelected = false
        aMonthBtn.isSelected = false
//        totalRangeBtn.isSelected = false
        view?.isSelected = true

        when (view) {
            selectRangeBtn -> {
            }
            aWeekRangeBtn -> {
                from = LocalDate.now().minusDays(6)
                to = LocalDate.now()
            }
            twoWeeksRangeBtn -> {
                from = LocalDate.now().minusDays(13)
                to = LocalDate.now()
            }
            aMonthBtn -> {
                from = LocalDate.now().minusDays(29)
                to = LocalDate.now()
            }
//            totalRangeBtn -> {
//                from = firstDate
//                to = LocalDate.now()
//            }
        }

        setRangeGuideText()
    }

    override fun show() {
        super.show()
        updateBtn.toDisableUI()
    }

    private fun setRange() {
        fromCalendar.calendar.setOnCalendarRangeSelectListener(null)
        toCalendar.calendar.setOnCalendarRangeSelectListener(null)

        fromCalendar.calendar.setSelectCalendarRange(
                from.year,
                from.monthOfYear,
                from.dayOfMonth,
                to.year,
                to.monthOfYear,
                to.dayOfMonth
        )
        toCalendar.calendar.setSelectCalendarRange(
                from.year,
                from.monthOfYear,
                from.dayOfMonth,
                to.year,
                to.monthOfYear,
                to.dayOfMonth

        )
        toCalendar.calendar.scrollToCalendar(to.year, to.monthOfYear, to.dayOfMonth)
        fromCalendar.calendar.scrollToCalendar(from.year, from.monthOfYear, from.dayOfMonth)

        toCalendar.calendar.setOnCalendarRangeSelectListener(toCalendar)
        fromCalendar.calendar.setOnCalendarRangeSelectListener(fromCalendar)
    }

    private fun setRangeGuideText() {
        if (from > to)
            showDateRangeErrToast()

        rangeGuideTv.text = "${from.year}.${from.monthOfYear}.${from.dayOfMonth} " +
                "- ${to.year}.${to.monthOfYear}.${to.dayOfMonth} (${period}일간)"

    }

    val hideRunnable = Runnable {
        toastView.hide(350)
    }

    fun showDateRangeErrToast(text: String = "마지막 날짜가 시작 날짜보다 앞설 수 없습니다.") {
        toastView.text = text
        toastHandler.removeCallbacks(hideRunnable)

        toastView.show(350) {
            toastHandler.postDelayed(hideRunnable, 800)
        }
    }

    override fun dismiss() {
        super.dismiss()
        toastView.visibility = View.GONE
        toastHandler.removeCallbacks(hideRunnable)
    }
}
