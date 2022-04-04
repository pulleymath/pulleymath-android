package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.databinding.ViewDailySummaryContentsBinding
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.spToPx
import java.util.*

class DailySummaryContentsView: ConstraintLayout {
    var guide: String = ""
        set(value) {
            field = value
            if(value.isEmpty()) {
                binding.guideTv.visibility = View.GONE
            } else {
                binding.guideTv.visibility = View.VISIBLE
            }
            binding.guideTv.text = value
            changeUI()
        }
    var isPercentVisible: Boolean = true
        set(value) {
            field = value
            if(value) {
                binding.percentageTv.visibility = View.VISIBLE
                binding.percentageLabel.visibility = View.VISIBLE
            } else {
                binding.percentageTv.visibility = View.GONE
                binding.percentageLabel.visibility = View.GONE
            }
            changeUI()
        }
    var isDateVisible: Boolean = true
        set(value) {
            field = value
            if(value) {
                binding.dateTv.visibility = View.VISIBLE
            } else {
                binding.dateTv.visibility = View.GONE
            }
            changeUI()
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    var binding: ViewDailySummaryContentsBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_daily_summary_contents, this, true)

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_daily_summary_contents, this)
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

        with(binding) {
            studyTimeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)
            percentageLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)
            problemCntLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTopLabelSize)

            studyTimeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)
            percentageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)
            problemCntTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize)

            dateTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize)
        }
    }

    fun setUpUI(dailySummary: DailyStudy) {
        with(binding) {
            dateTv.text = DateTimeUtils.yyyyMMddFormat.format(Date())
            problemCntTv.text = "${dailySummary.todayProblemCount}"
            percentageTv.text = "${dailySummary.todayPercentage}%"
            studyTimeTv.text = DateTimeUtils.getHourMinSpentTimeStr(dailySummary.totalStudyTime)
        }
    }
}