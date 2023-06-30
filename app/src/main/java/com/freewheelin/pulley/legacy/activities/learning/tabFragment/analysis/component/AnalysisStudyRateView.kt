package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.Curation
import com.freewheelin.pulley.legacy.core.API.ResponseModel.NormalNoteRatio
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.setOnBasicPOrHigherClickListener
import com.freewheelin.pulley.revision2023.ui.view.SecondaryButton

interface AnalysisStudyRateViewListener {
    fun onWrongStudyBtnClicked(view: AnalysisStudyRateView)
}

class AnalysisStudyRateView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var listener: AnalysisStudyRateViewListener? = null

    var actionBtn: SecondaryButton
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
        actionBtn.setOnBasicPOrHigherClickListener(cb = { listener?.onWrongStudyBtnClicked(this) },
            deniedCb = {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "추천문제풀기")
                val dialog = PurchaseGuideDialog()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
            })
    }

    fun setUpUI(ratio: NormalNoteRatio, curation: Curation) {
        guideTv.text = curation.ratio["title"]
        summaryTv.text = curation.ratio["curation"]
        studyRateChart.values = listOf(ratio.normalRatio, ratio.noteRatio)
        chartNormalTv.text = "${ratio.normalRatio}"
        chartNoteTv.text = "${ratio.noteRatio}"

    }
}