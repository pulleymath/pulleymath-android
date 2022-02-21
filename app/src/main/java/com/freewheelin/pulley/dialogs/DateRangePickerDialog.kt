package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.calendarPickerViews.*
import org.joda.time.LocalDate
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.freewheelin.pulley.databinding.ViewDaebakDateRangePickerBinding
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime


interface DateRangePickerDialogListener {
    fun onCancelClicked(picker: DateRangePickerDialog) {}
    fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

class DateRangePickerDialog(context: Context, from: LocalDate, to: LocalDate, firstDate: LocalDate) : Dialog(context), View.OnClickListener, DaebakCalendarListener {
    enum class Type {
        RECENT7, RECENT14, RECENT30, CUSTOM
    }
    private val binding: ViewDaebakDateRangePickerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_daebak_date_range_picker, null, false)
    }

    override fun onSelectDate(calendar: DaebakCalendar, date: LocalDate) {
        binding.selectRangeBtn.performClick()
        setRange()
        setRangeGuideText()
        binding.updateBtn.toEnableUI()
    }

    var toastHandler = Handler()
    var listener: DateRangePickerDialogListener? = null
    val period: Int
        get() = DateTimeUtils.getPeriod(from, to)

    var firstDate: LocalDate

    var from: LocalDate
        set(value) {
            binding.updateBtn.toEnableUI()
//            binding.fromCalendar.setSelectedDate(value, true)
            binding.fromCalendar.calendar.date = value.toLocalDateTime(LocalTime(DateTimeZone.UTC)).millisOfSecond.toLong()
            setRange()
        }
        get() = LocalDate(binding.fromCalendar.calendar.date)

    var to: LocalDate
        set(value) {
            binding.updateBtn.toEnableUI()
            binding.toCalendar.calendar.date = value.toLocalDateTime(LocalTime(DateTimeZone.UTC)).millisOfSecond.toLong()
//            binding.toCalendar.setSelectedDate(value, true)
            setRange()
        }
        get() = LocalDate(binding.toCalendar.calendar.date)

    init {
        setContentView(binding.root)
        initUI()
        this.from = from
        this.to = to
        this.firstDate = firstDate
    }

    fun initUI() {
//        binding.fromCalendar.calendar.setMonthView(FromCalendarView::class.java)
//        binding.toCalendar.calendar.setMonthView(ToCalendarView::class.java)
        binding.fromCalendar.listener = this
        binding.toCalendar.listener = this
//        binding.fromCalendar.setUnitlNowMonthUISetter()
//        binding.toCalendar.setUnitlNowMonthUISetter()

        binding.selectRangeBtn.setOnClickListener(this)
        binding.aWeekRangeBtn.setOnClickListener(this)
        binding.twoWeeksRangeBtn.setOnClickListener(this)
        binding.aMonthBtn.setOnClickListener(this)
//        totalRangeBtn.setOnClickListener(this)

        onClick(binding.aWeekRangeBtn)
        binding.updateBtn.toDisableUI()
        binding.updateBtn.setOnClickListener {
            if (binding.updateBtn.isEnableUI() == false) {
                showDateRangeErrToast("날짜 선택을 완료해주세요.")
            } else if (from > to) {
                showDateRangeErrToast()
            } else {
                dismiss()
                listener?.onUpdateClicked(this, from, to, getSelected())
            }
        }
        binding.cancelBtn.setOnClickListener {
            dismiss()
            listener?.onCancelClicked(this)
        }
    }

    fun getSelected() : Type {
        return when {
            binding.aWeekRangeBtn.isSelected -> Type.RECENT7
            binding.twoWeeksRangeBtn.isSelected -> Type.RECENT14
            binding.aMonthBtn.isSelected -> Type.RECENT30
            else -> Type.CUSTOM
        }
    }

    override fun onClick(view: View?) {
//        binding.selectRangeBtn.isSelected = false
//        binding.aWeekRangeBtn.isSelected = false
//        binding.twoWeeksRangeBtn.isSelected = false
//        binding.aMonthBtn.isSelected = false
//        totalRangeBtn.isSelected = false
        view?.isSelected = true

        when (view) {
            binding.selectRangeBtn -> {
            }
            binding.aWeekRangeBtn -> {
                from = LocalDate.now().minusDays(6)
                to = LocalDate.now()
            }
            binding.twoWeeksRangeBtn -> {
                from = LocalDate.now().minusDays(13)
                to = LocalDate.now()
            }
            binding.aMonthBtn -> {
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
        binding.updateBtn.toDisableUI()
    }

    private fun setRange() {
//        binding.fromCalendar.calendar.setOnCalendarRangeSelectListener(null)
//        binding.toCalendar.calendar.setOnCalendarRangeSelectListener(null)
//
//        binding.fromCalendar.calendar.setSelectCalendarRange(
//                from.year,
//                from.monthOfYear,
//                from.dayOfMonth,
//                to.year,
//                to.monthOfYear,
//                to.dayOfMonth
//        )
//        binding.toCalendar.calendar.setSelectCalendarRange(
//                from.year,
//                from.monthOfYear,
//                from.dayOfMonth,
//                to.year,
//                to.monthOfYear,
//                to.dayOfMonth
//
//        )
//        binding.toCalendar.calendar.scrollToCalendar(to.year, to.monthOfYear, to.dayOfMonth)
//        binding.fromCalendar.calendar.scrollToCalendar(from.year, from.monthOfYear, from.dayOfMonth)
//
//        binding.toCalendar.calendar.setOnCalendarRangeSelectListener(binding.toCalendar)
//        binding.fromCalendar.calendar.setOnCalendarRangeSelectListener(binding.fromCalendar)
    }

    private fun setRangeGuideText() {
        if (from > to)
            showDateRangeErrToast()

        binding.rangeGuideTv.text = "${from.year}.${from.monthOfYear}.${from.dayOfMonth} " +
                "- ${to.year}.${to.monthOfYear}.${to.dayOfMonth} (${period}일간)"

    }

    val hideRunnable = Runnable {
        binding.toastView.hide(350)
    }

    fun showDateRangeErrToast(text: String = "마지막 날짜가 시작 날짜보다 앞설 수 없습니다.") {
        binding.toastView.text = text
        toastHandler.removeCallbacks(hideRunnable)

        binding.toastView.show(350) {
            toastHandler.postDelayed(hideRunnable, 800)
        }
    }

    override fun dismiss() {
        super.dismiss()
        binding.toastView.visibility = View.GONE
        toastHandler.removeCallbacks(hideRunnable)
    }
}
