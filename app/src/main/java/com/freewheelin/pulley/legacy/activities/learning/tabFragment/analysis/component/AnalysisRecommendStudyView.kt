package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.Curation
import com.freewheelin.pulley.legacy.core.API.ResponseModel.WeakChapterResult
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.bars.VerticalBar
import com.freewheelin.pulley.revision2023.ui.view.SecondaryButton

interface AnalysisRecommendStudyViewListener {
    fun onRecommendBtnClicked(view: AnalysisRecommendStudyView)
//    fun onDeniedCallback()

}
class AnalysisRecommendStudyView: ConstraintLayout {
    var listener: AnalysisRecommendStudyViewListener? = null

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var myCorrectRateBar: VerticalBar
    var averageCorrectRateBar: VerticalBar
    var averageBarLabel: TextView
    var actionBtn: SecondaryButton

    var guideTv: TextView
    var unitTv: TextView
    var recommendTv: TextView
    var myBarLabel: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_recommend_study, this)

        myCorrectRateBar = findViewById(R.id.myCorrectRateBar)
        averageCorrectRateBar = findViewById(R.id.averageCorrectRateBar)
        averageBarLabel = findViewById(R.id.averageBarLabel)
        actionBtn = findViewById(R.id.actionBtn)

        guideTv = findViewById(R.id.guideTv)
        unitTv = findViewById(R.id.intentionTv)
        recommendTv = findViewById(R.id.recommendTv)
        myBarLabel = findViewById(R.id.myBarLabel)
//        actionLockIv = findViewById(R.id.actionLockIv)

        myCorrectRateBar.value = 0.3f
        myCorrectRateBar.barHeight = resources.getDimension(R.dimen.dp120)
        myCorrectRateBar.barWidth = resources.getDimension(R.dimen.dp56)
        averageCorrectRateBar.value = 0.6f
        averageCorrectRateBar.barHeight = resources.getDimension(R.dimen.dp120)
        averageCorrectRateBar.barWidth = resources.getDimension(R.dimen.dp56)
        averageCorrectRateBar.color = ContextCompat.getColor(context, R.color.gray_400)
        averageBarLabel.text = "등급\n평균"
        actionBtn.setOnBasicPOrHigherClickListener(cb = { listener?.onRecommendBtnClicked(this) },
            deniedCb = {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "추천유형학습풀기")

                val dialog = PurchaseGuideDialog()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
            })
    }

    fun setUpUI(result: WeakChapterResult, curation: Curation) {
        val myPercent = result.studentPercent * 0.01f
        val sameGradePercent = result.sameGradePercent * 0.01f
        myCorrectRateBar.value = myPercent
        myCorrectRateBar.lowLabel = "나의\n정답률"
        averageCorrectRateBar.value = sameGradePercent

        averageBarLabel.text = "${result.studentRating}등급\n평균"
        averageCorrectRateBar.lowLabel = "${result.studentRating}등급\n평균"

        if(myPercent < sameGradePercent) {
            myCorrectRateBar.color = ContextCompat.getColor(context!!, R.color.red_300)
        } else {
            myCorrectRateBar.color = ContextCompat.getColor(context!!, R.color.blue_400)
        }

        guideTv.text = "${result.bigChapterName} ${curation.guide["title"]}"
        unitTv.text = "${result.bigChapterName} 정답률 비교"

        if(myPercent <= 0.4)
            myBarLabel.visibility = View.INVISIBLE
        else
            myBarLabel.visibility = View.VISIBLE

        if(sameGradePercent <= 0.4)
            averageBarLabel.visibility = View.INVISIBLE
        else
            averageBarLabel.visibility = View.VISIBLE

        val text1 = curation.guide["curation"]?:""
        val text2 = result.smallChapterName

        recommendTv.text = "$text1 $text2"
                .partialFontAndColored(
                        Theme.extraBold(context),
                        ContextCompat.getColor(context!!, R.color.purple_300),
                        text2
                )
    }

}