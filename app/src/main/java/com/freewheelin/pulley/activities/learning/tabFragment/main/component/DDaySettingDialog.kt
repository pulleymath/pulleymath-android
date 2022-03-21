package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.DDay
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.ArduousSpinner
import com.freewheelin.pulley.views.ArduousSpinnerListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.text.InputFilter
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.freewheelin.pulley.views.calendarPickerViews.DaebakCalendar
import com.freewheelin.pulley.views.calendarPickerViews.DaebakCalendarListener
import com.freewheelin.pulley.views.editText.DaebakInputField
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.lang.Math.abs


interface DDaySettingDialogListener {
    fun onOnDDaySettingCompleted()
}

class DDaySettingDialog(context: Context, setOnSpyMode: () -> Unit): Dialog(context), ArduousSpinnerListener,
    DaebakCalendarListener {

    val rootView get() = findViewById<ConstraintLayout>(R.id.rootView)
    val dDayLabelPrefix get() = findViewById<TextView>(R.id.dDayLabelPrefix)
    val dDayLabelSuffix get() = findViewById<TextView>(R.id.dDayLabelSuffix)
    val dDayTv get() = findViewById<TextView>(R.id.dDayTv)
    val actionBtn get() = findViewById<PrimaryButton>(R.id.actionBtn)
    val targetSpinner get() = findViewById<ArduousSpinner>(R.id.targetSpinner)
    val customField get() = findViewById<DaebakInputField>(R.id.customField)
    val errorContainer get() = findViewById<LinearLayout>(R.id.errorContainer)
    val scrollView get() = findViewById<ScrollView>(R.id.scrollView)
    val selectCalendar get() = findViewById<DaebakCalendar>(R.id.selectCalendar)

    var dDays: List<DDay> = emptyList()
    var selectedDate: Date? = null
    set(value) {
        field = value
        if(value == null)
            hideDDayLabel()
        else {
            showDDayLabel()
            errorContainer.visibility = View.GONE

            val dday = DateTimeUtils.getDayDifferences(Date(), value)

            if(dday >= 0) {
                dDayLabelPrefix.text = "${value.year()}년 ${value.month()}월 ${value.day()}일까지"
                dDayTv.text = "${DateTimeUtils.getDayDifferences(Date(), value)}일"
                dDayLabelSuffix.text = "남았습니다"
            } else if(dday < 0) {
                dDayLabelPrefix.text = "${value.year()}년 ${value.month()}월 ${value.day()}일로부터"
                dDayTv.text = "${abs(DateTimeUtils.getDayDifferences(Date(), value))}일"
                dDayLabelSuffix.text = "지났습니다"
            }
        }
    }
    var listener: DDaySettingDialogListener? = null

    val customFieldText = "직접입력"
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_dday_setting)

        actionBtn.setOnClickListener {
            onModifyBtnClicked()
        }
//        selectInfoTv.visibility = View.INVISIBLE
        targetSpinner.listener = this
        customField.goneLabel()

        val maxLength = 10
        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(maxLength)
        customField.editText.filters = fArray

        syncDayList()
        errorContainer.visibility = View.VISIBLE
        scrollView.setOnTouchListener { view, motionEvent ->
            context.hideKeyboard(view)
            false
        }

        errorContainer.setOnClickListener {
            // spy mode 켜기
            setOnSpyMode()
        }
    }

    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        val dDays = dDays.getOrNull(position)
        if(dDays == null) {
            customField.visibility = View.VISIBLE
            selectedDate = null
        } else {
            configureUIByDDay(dDays)
        }
    }

    override fun dismiss() {
        try {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
        }
        super.dismiss()
    }


    fun syncDayList() {
        API_V2.getDDays().enqueue(object: Callback<List<DDay>>{
            override fun onFailure(call: Call<List<DDay>>, t: Throwable) {

            }

            override fun onResponse(call: Call<List<DDay>>, response: Response<List<DDay>>) {
                val dDayList = response.body() ?: return
                dDays = dDayList
                configureUI()
            }

        })
    }

    private fun configureUI() {
        val listTitles = dDays.map { it.description }.toMutableList()
        listTitles.add(customFieldText)
        targetSpinner.items = listTitles
        selectCalendar.listener = this

        val existTarget = getTargetTitleAndDate()

        Log.d("테스트", "existTarget=$existTarget")

        if(existTarget == null) {
            targetSpinner.position = 0
            configureUIByDDay(dDays.first())
            targetSpinner.defaultStr = dDays.first().description
        } else {
            selectedDate = existTarget.third
            val ldt = LocalDateTime(existTarget.third.year(), existTarget.third.month(), existTarget.third.day(), 0, 0)
            selectCalendar.calendar.date = ldt.toDateTime().millis
            if(existTarget.first == -1) {
                targetSpinner.position = dDays.size
                customField.visibility = View.VISIBLE
                customField.text = existTarget.second
                targetSpinner.defaultStr = customFieldText
            } else {
                targetSpinner.position = 0
                customField.visibility = View.GONE
                targetSpinner.defaultStr = existTarget.second
            }
        }
    }

    private fun configureUIByDDay(dDays: DDay) {
        customField.visibility = View.GONE
        selectedDate = dDays.startDate
        val year = dDays.startDate.year()
        val month = dDays.startDate.month()
        val day = dDays.startDate.day()
        val ldt = LocalDateTime(year, month, day, 0, 0)
        selectCalendar.calendar.date = ldt.toDateTime().millis
    }

    fun onModifyBtnClicked() {

        val dDays = if(targetSpinner.position == null) null else  dDays.getOrNull(targetSpinner.position!!)

        Log.d("테스트", "dDays=$dDays")

        if(dDays == null) {
            if(customField.text.isEmpty()) {
                customField.showErrorMsg("디데이의 이름을 입력해주세요.")
            }

            if(selectedDate == null) {
                errorContainer.visibility = View.VISIBLE
            }

            val title = customField.text
            val date = selectedDate

            Log.d("테스트", "title=$title, date=$date")

            if(date != null && title.isNotEmpty()) {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "메인", "설정 완료하기 버튼", customFieldText)
                setTargetTitleAndDate(title, date)
                listener?.onOnDDaySettingCompleted()
                dismiss()
            }
        } else {
            LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "메인", "설정 완료하기 버튼", dDays.description)
            setTargetDDay(dDays)
            listener?.onOnDDaySettingCompleted()
            dismiss()
        }
    }

    private fun showDDayLabel() {
        dDayTv.visibility = View.VISIBLE
        dDayLabelPrefix.visibility = View.VISIBLE
        dDayLabelSuffix.visibility = View.VISIBLE

//        selectInfoTv.visibility = View.INVISIBLE
    }

    private fun hideDDayLabel() {
        dDayTv.visibility = View.INVISIBLE
        dDayLabelPrefix.visibility = View.INVISIBLE
        dDayLabelSuffix.visibility = View.INVISIBLE

//        selectInfoTv.visibility = View.VISIBLE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        context.hideKeyboard(rootView)
        return super.onTouchEvent(event)
    }

    fun getTargetTitleAndDate(): Triple<Int, String, Date>? {
        val targetTitle = Preferences.targetDateTitle.get()
        val targetDate = Preferences.targetDate.get()
        val targetID = Preferences.targetID.get()
        if(targetTitle.isEmpty() || targetDate == 0L) {
            return null
        } else {
            return Triple(targetID, targetTitle, Date(targetDate))
        }
    }

    fun setTargetTitleAndDate(title: String, date: Date) {
        Preferences.targetDateTitle.set(title)
        Preferences.targetDate.set(date.time)
        Preferences.targetID.set(-1)
    }

    fun setTargetDDay(dday: DDay) {
        Preferences.targetDate.set(dday.startDate.time)
        Preferences.targetDateTitle.set(dday.description)
        Preferences.targetID.set(dday.id)
    }

    override fun onSelectDate(calendar: DaebakCalendar, date: LocalDate) {
        selectedDate = date.toDate()
        customField.visibility = View.VISIBLE
        targetSpinner.defaultStr = customFieldText
        targetSpinner.position = dDays.size
    }
}