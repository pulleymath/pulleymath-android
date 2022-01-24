package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.Curation
import com.freewheelin.pulley.core.API.ResponseModel.NormalNoteRatio
import com.freewheelin.pulley.views.Buttons.PrimaryButton
import kotlinx.android.synthetic.main.view_analysis_study_rate.view.*

interface AnalysisStudyRateViewListener {
    fun onWrongStudyBtnClicked(view: AnalysisStudyRateView)
}

class AnalysisStudyRateView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var listener: AnalysisStudyRateViewListener? = null

    var actionBtn: PrimaryButton
    var guideTv: TextView
    var summaryTv: TextView
    var studyRateChart: StudyRateView
    var chartNormalTv: TextView
    var chartNoteTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_study_rate, this)

        actionBtn = findViewById(R.id.actionBtn)
        guideTv = findViewById(R.id.guideTv)
        summaryTv = findViewById(R.id.summaryTv)
        studyRateChart = findViewById(R.id.studyRateChart)
        chartNormalTv = findViewById(R.id.chartNormalTv)
        chartNoteTv = findViewById(R.id.chartNoteTv)

        initUI()
    }

    private fun initUI() {
        actionBtn.setOnClickListener { listener?.onWrongStudyBtnClicked(this) }
    }

    fun setUpUI(ratio: NormalNoteRatio, curation: Curation) {
        guideTv.text = curation.ratio["title"]
        summaryTv.text = curation.ratio["curation"]
        studyRateChart.values = listOf(ratio.normalRatio, ratio.noteRatio)
        chartNormalTv.text = "${ratio.normalRatio}"
        chartNoteTv.text = "${ratio.noteRatio}"

    }
}