package com.freewheelin.pulley.legacy.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewDaebakDateRangePickerBinding
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.hide
import com.freewheelin.pulley.legacy.utils.show
import com.freewheelin.pulley.legacy.views.calendarPickerViews.DaebakCalendar
import com.freewheelin.pulley.legacy.views.calendarPickerViews.DaebakCalendarListener
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime


interface DateRangePickerDialogListener {
    fun onCancelClicked(picker: DateRangePickerDialog) {}
    fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

class DateRangePickerDialog(context: Context, from: LocalDate, to: LocalDate, firstDate: LocalDate) : Dialog(context), View.OnClickListener, DaebakCalendarListener {
    enum class Type (val text: String?) {
        RECENT7("최근 7일"),
        RECENT14("최근 14일"),
        RECENT30("최근 30일"),
        CUSTOM(null)
    }
    val binding: ViewDaebakDateRangePickerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_daebak_date_range_picker, null, false)
    }

    override fun onSelectDate(calendar: DaebakCalendar, date: LocalDate) {
        binding.selectRangeBtn.performClick()
        setRangeGuideTextTemp(calendar, date)
    }

    var toastHandler = Handler(Looper.getMainLooper())
    var listener: DateRangePickerDialogListener? = null
    val period: Int
        get() = DateTimeUtils.getPeriod(from, to)

    var firstDate: LocalDate

    var tempFrom: LocalDate? = null
    var tempTo: LocalDate? = null
    var from: LocalDate
        set(value) {
            val ldt = LocalDateTime(value.year, value.monthOfYear, value.dayOfMonth, 0, 0)
            binding.fromCalendar.calendar.date = ldt.toDateTime().millis
        }
        get() = LocalDate(binding.fromCalendar.calendar.date)

    var to: LocalDate
        set(value) {
            val ldt = LocalDateTime(value.year, value.monthOfYear, value.dayOfMonth, 0, 0)
            binding.toCalendar.calendar.date = ldt.toDateTime().millis
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
        binding.fromCalendar.listener = this
        binding.toCalendar.listener = this

        binding.selectRangeBtn.setOnClickListener(this)
        binding.aWeekRangeBtn.setOnClickListener(this)
        binding.twoWeeksRangeBtn.setOnClickListener(this)
        binding.aMonthBtn.setOnClickListener(this)
//        totalRangeBtn.setOnClickListener(this)

        onClick(binding.aWeekRangeBtn)
        binding.updateBtn.isEnabled = false
        binding.updateBtn.setOnClickListener {
            if (binding.updateBtn.isEnabled == false) {
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
        binding.selectRangeBtn.isSelected = false
        binding.aWeekRangeBtn.isSelected = false
        binding.twoWeeksRangeBtn.isSelected = false
        binding.aMonthBtn.isSelected = false
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
        binding.updateBtn.isEnabled = false
    }

    private fun setRangeGuideTextTemp(calendar: DaebakCalendar, date: LocalDate) {
        when (calendar) {
            binding.fromCalendar -> {
                if (date > to) {
                    showDateRangeErrToast()
                    Handler(Looper.getMainLooper()).postDelayed({
                        from = from
                    }, 250)
                } else {
                    from = date
                    updateRangeGuide()
                }

            }
            binding.toCalendar -> {
                if (from > date) {
                    showDateRangeErrToast()
                    Handler(Looper.getMainLooper()).postDelayed({
                        to = to
                    }, 250)
                } else {
                    to = date
                    updateRangeGuide()
                }
            }
        }
    }
    private fun updateRangeGuide() {
        binding.updateBtn.isEnabled = true
        binding.rangeGuideTv.text = "${from.year}.${from.monthOfYear}.${from.dayOfMonth} " +
            "- ${to.year}.${to.monthOfYear}.${to.dayOfMonth} (${period}일간)"
    }

    private fun setRangeGuideText() {
        if (from > to) {
            binding.updateBtn.isEnabled = false
            showDateRangeErrToast()
        } else {
            updateRangeGuide()
        }
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
