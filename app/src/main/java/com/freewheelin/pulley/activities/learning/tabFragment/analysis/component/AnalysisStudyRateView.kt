package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager.findFragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.Curation
import com.freewheelin.pulley.core.API.ResponseModel.NormalNoteRatio
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.setOnBasicPOrHigherClickListener
import com.freewheelin.pulley.utils.visibleIf

interface AnalysisStudyRateViewListener {
    fun onWrongStudyBtnClicked(view: AnalysisStudyRateView)
}

class AnalysisStudyRateView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var listener: AnalysisStudyRateViewListener? = null

    var actionBtnWrapperCl: ConstraintLayout
    var actionLockIv: ImageView
    var guideTv: TextView
    var summaryTv: TextView
    var studyRateChart: StudyRateView
    var chartNormalTv: TextView
    var chartNoteTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_study_rate, this)

        actionBtnWrapperCl = findViewById(R.id.actionBtnWrapperCl)
        actionLockIv = findViewById(R.id.actionLockIv)
        guideTv = findViewById(R.id.guideTv)
        summaryTv = findViewById(R.id.summaryTv)
        studyRateChart = findViewById(R.id.studyRateChart)
        chartNormalTv = findViewById(R.id.chartNormalTv)
        chartNoteTv = findViewById(R.id.chartNoteTv)

        initUI()
    }

    private fun initUI() {
        actionBtnWrapperCl.setOnBasicPOrHigherClickListener(cb = { listener?.onWrongStudyBtnClicked(this) },
            deniedCb = {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "추천문제풀기")
                val dialog = PurchaseGuideDialog()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
//                findFragment<PurchaseGuideDialog>(this).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
//                dialog.show()
//                supportFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
//                DialogUtils.confirmDialog(context, "[테스트]구독중이 아닙니다.", "열려라 참깨")
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