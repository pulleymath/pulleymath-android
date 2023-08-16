package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.ItemStudyListBinding
import com.freewheelin.pulley.legacy.model.contents.BookType
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.view.SecondaryButton

interface AnalysisTodayStudyListViewListener {
    fun onStudyHistoryBtnClicked(view: AnalysisTodayStudyListView)
    fun onSolveBtnClicked(view: AnalysisTodayStudyListView, piece: Content)
    fun onReportBtnClicked(view: AnalysisTodayStudyListView, piece: Content)
    fun onStudyBtnClicked(view: AnalysisTodayStudyListView)

}
class AnalysisTodayStudyListView: ConstraintLayout {
    var listener: AnalysisTodayStudyListViewListener? = null
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var contents: List<Content> = emptyList()
    var isUserAnalysis: Boolean = false

    var viewAllListBtn: TextView
    var studyBtn: SecondaryButton

    var emptyGuideTv: TextView
    var studyListLl: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_today_study_list, this)

        viewAllListBtn = findViewById(R.id.viewAllListBtn)
        studyBtn = findViewById(R.id.studyBtn)
        emptyGuideTv = findViewById(R.id.emptyGuideTv)
        studyListLl = findViewById(R.id.studyListLl)

        setList()
        viewAllListBtn.setOnClickListener {
            listener?.onStudyHistoryBtnClicked(this)
        }
        studyBtn.setOnClickListener {
            listener?.onStudyBtnClicked(this)
        }
    }

    fun setUpUI(contents: List<Content>) {
        this.contents = contents
        if(this.contents.isEmpty()) {
            emptyGuideTv.visibility = View.VISIBLE
            studyBtn.visibility = View.VISIBLE
            studyListLl.visibility = View.INVISIBLE
        } else {
            emptyGuideTv.visibility = View.INVISIBLE
            studyBtn.visibility = View.INVISIBLE
            studyListLl.visibility = View.VISIBLE
//            studyListLl.adapter?.notifyDataSetChanged()
            setList()
        }
    }

    fun setUpUIByUserAnalysis() {
        isUserAnalysis = true
    }

    var newOne = false
    fun setList() {
        studyListLl.removeAllViews()
        var holder:StudyListViewHolder? = null

        for((index, piece) in contents.withIndex()) {
            holder = StudyListViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_study_list, null, false))

            holder.set(piece)
            if (isUserAnalysis) holder.setUserAnalysisUI()

            holder.listBinding.solveBtn.apply {
                // TODO start challenge reward는 islocked 이 풀려있나?
                setOnClickListener {
                    if (piece.isLocked) {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "풀기/리뷰")
                        val dialog = PurchaseGuideDialog.newInstance()
                        val fm = (context as AppCompatActivity).supportFragmentManager
                        fm.let { dialog.show(it, "purchaseGuideDialog")}
                    }
                    else {
                        listener?.onSolveBtnClicked(this@AnalysisTodayStudyListView, piece)
                    }
                }
            }
            holder.listBinding.reportBtn.apply {
                setOnClickListener {
                    if (piece.isLocked) {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "리포트")
                        val dialog = PurchaseGuideDialog.newInstance()
                        val fm = (context as AppCompatActivity).supportFragmentManager
                        fm.let { dialog.show(it, "purchaseGuideDialog")}
                    }
                    else { listener?.onReportBtnClicked(this@AnalysisTodayStudyListView, piece) }
                }
            }

            if(index == 0 && newOne) holder.setHighlight()
            studyListLl.addView(holder.listBinding.root)
        }
        if(newOne) newOne = false
        holder?.listBinding?.borderView?.visibility = View.INVISIBLE
    }
}

class StudyListViewHolder(val listBinding: ItemStudyListBinding): RecyclerView.ViewHolder(listBinding.root) {
    fun set(piece: Content) {
        listBinding.apply {
            dateTv.text = DateTimeUtils.mMDashddFormat.format(piece.updateDateTime)
            titleTv.text = if (piece.title.isNullOrEmpty()) piece.subject else piece.title

            val solvedCnt = piece.markedNumber + piece.similarProblemNumber
            val totalCnt = piece.totalNumber + piece.similarProblemNumber

            problemCntTv.text = "$solvedCnt"

            problemTotalCntTv.text = "/$totalCnt"

            scoreTv.text = if(piece.isCompleted()) "${piece.score}%" else "-"
            categoryTv.text = piece.pieceCategoryTag.getTagTitle

            if(piece.isCompleted() && piece.pieceCategoryTag == BookType.MO || piece.pieceCategoryTag == BookType.TEST) {
                reportBtn.visibility = View.VISIBLE
            } else {
                reportBtn.visibility = View.INVISIBLE
            }

            solveBtn.showStartIcon(piece.isLocked)
        }

    }

    fun setUserAnalysisUI() {
        listBinding.solveBtn.visibility = View.INVISIBLE
    }

    fun setHighlight() {
        listBinding.root.setBackgroundResource(R.color.yellow_100)
    }
}