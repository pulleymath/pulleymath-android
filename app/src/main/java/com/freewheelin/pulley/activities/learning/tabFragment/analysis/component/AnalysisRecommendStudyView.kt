package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.Curation
import com.freewheelin.pulley.core.API.ResponseModel.WeakChapterResult
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.bars.VerticalBar

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
//    var actionBtn: PrimaryButton
    var actionBtnWrapperCl: ConstraintLayout

    var guideTv: TextView
    var unitTv: TextView
    var recommendTv: TextView
    var myBarLabel: TextView
    var actionLockIv: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_recommend_study, this)

        myCorrectRateBar = findViewById(R.id.myCorrectRateBar)
        averageCorrectRateBar = findViewById(R.id.averageCorrectRateBar)
        averageBarLabel = findViewById(R.id.averageBarLabel)
        actionBtnWrapperCl = findViewById(R.id.actionBtnWrapperCl)

        guideTv = findViewById(R.id.guideTv)
        unitTv = findViewById(R.id.intentionTv)
        recommendTv = findViewById(R.id.recommendTv)
        myBarLabel = findViewById(R.id.myBarLabel)
        actionLockIv = findViewById(R.id.actionLockIv)

        myCorrectRateBar.value = 0.3f
        myCorrectRateBar.barHeight = resources.getDimension(R.dimen.dp120)
        myCorrectRateBar.barWidth = resources.getDimension(R.dimen.dp56)
        averageCorrectRateBar.value = 0.6f
        averageCorrectRateBar.barHeight = resources.getDimension(R.dimen.dp120)
        averageCorrectRateBar.barWidth = resources.getDimension(R.dimen.dp56)
        averageCorrectRateBar.color = ContextCompat.getColor(context, R.color.grey_e0e0e0)
        averageBarLabel.text = "등급\n평균"
        actionBtnWrapperCl.setOnBasicPOrHigherClickListener(cb = { listener?.onRecommendBtnClicked(this) },
            deniedCb = {
//                listener?.onDeniedCallback()
                val dialog = PurchaseGuideDialog()
                FragmentManager.findFragment<PurchaseGuideDialog>(this@AnalysisRecommendStudyView).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
            })
    }

    fun setUpUI(result: WeakChapterResult, curation: Curation) {
        val myPercent = result.studentPercent * 0.01f
        val sameGradePercent = result.sameGradePercent * 0.01f
        myCorrectRateBar.value = myPercent
        myCorrectRateBar.lowLabel = "나의\n정답률"
        averageCorrectRateBar.value = sameGradePercent

        averageBarLabel.text = if (result.studentRating == 0) {
            "중등\n평균"
        } else {
            "${result.studentRating}등급\n평균"
        }

        var userRatingText = "${user!!.rating}등급\n평균"
        if(user!!.rating < 1) {
            userRatingText = "4등급\n평균"
        }
        averageCorrectRateBar.lowLabel = userRatingText

        if(myPercent < sameGradePercent) {
            myCorrectRateBar.color = ContextCompat.getColor(context!!, R.color.red_fe7b67)
        } else {
            myCorrectRateBar.color = ContextCompat.getColor(context!!, R.color.blue_30a4ff)
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
                        ContextCompat.getColor(context!!, R.color.purple_6D6DFF),
                        text2
                )

        val showLockIv = user?.serviceType?.isTypeEqualOrHigher(PaidServiceType.BASIC_P) == false
        actionLockIv.visibleIf(showLockIv)
    }
}