package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.core.API.ResponseModel.DailySummary
import com.freewheelin.pulley.core.API.ResponseModel.DaySummary
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.spToPx
import kotlinx.android.synthetic.main.view_daily_summary_contents.view.*
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryContentsView: ConstraintLayout {
    var guide: String = ""
        set(value) {
            field = value
            if(value.isEmpty()) {
                guideTv.visibility = View.GONE
            } else {
                guideTv.visibility = View.VISIBLE
            }
            guideTv.text = value
            changeUI()
        }
    var isPercentVisible: Boolean = true
        set(value) {
            field = value
            if(value) {
                percentageTv.visibility = View.VISIBLE
                percentageLabel.visibility = View.VISIBLE
            } else {
                percentageTv.visibility = View.GONE
                percentageLabel.visibility = View.GONE
            }
            changeUI()
        }
    var isDateVisible: Boolean = true
        set(value) {
            field = value
            if(value) {
                dateTv.visibility = View.VISIBLE
            } else {
                dateTv.visibility = View.GONE
            }
            changeUI()
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_daily_summary_contents, this)
    }

    fun changeUI() {
        setTextSize()
    }

    private fun setTextSize() {
        val mainTopLabelSize = if(guide.isEmpty() && !isDateVisible && !isPercentVisible)
            12.spToPx() else 10.spToPx()

        val mainTextSize = if(guide.isEmpty() && !isDateVisible && !isPercentVisible)
            36.spToPx() else 24.spToPx()

        val dateTextSize = if(guide.isEmpty()) 24.spToPx() else 14.spToPx()

        studyTimeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)
        percentageLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)
        problemCntLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)

        studyTimeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)
        percentageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)
        problemCntTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)

        dateTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize)
    }

    fun setUpUI(dailySummary: DailyStudy) {
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(Date())
        problemCntTv.text = "${dailySummary.todayProblemCount}"
        percentageTv.text = "${dailySummary.todayPercentage}%"
        studyTimeTv.text = DateTimeUtils.getHourMinSpentTimeStr(dailySummary.totalStudyTime)
    }
}