package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.Curation
import com.freewheelin.pulley.core.API.ResponseModel.NormalNoteRatio
import kotlinx.android.synthetic.main.view_analysis_study_rate.view.*

interface AnalysisStudyRateViewListener {
    fun onWrongStudyBtnClicked(view: AnalysisStudyRateView)
}

class AnalysisStudyRateView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var listener: AnalysisStudyRateViewListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_study_rate, this)
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