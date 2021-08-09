package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.BookType
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setPermissionClickListener
import kotlinx.android.synthetic.main.item_study_list.view.*
import kotlinx.android.synthetic.main.view_analysis_today_study_list.view.*

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

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_today_study_list, this)
//        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        recyclerView.adapter = StudyListAdapter()
//        val adapter = StudyListAdapter()
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
            recyclerView.visibility = View.INVISIBLE
        } else {
            emptyGuideTv.visibility = View.INVISIBLE
            studyBtn.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
//            recyclerView.adapter?.notifyDataSetChanged()
            setList()
        }
    }

    fun setUpUIByUserAnalysis() {
        isUserAnalysis = true
    }

    var newOne = false
    fun setList() {
        recyclerView.removeAllViews()
        var holder:StudyListViewHolder? = null

        for((index, piece) in contents.withIndex()) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_study_list, recyclerView, false)
            holder = StudyListViewHolder(view)
            holder.set(piece)
            if (isUserAnalysis) holder.setUserAnalysisUI()

            holder.solveBtn.setPermissionClickListener {
                listener?.onSolveBtnClicked(this@AnalysisTodayStudyListView, piece)
            }
            holder.reportBtn.setOnClickListener {
                listener?.onReportBtnClicked(this@AnalysisTodayStudyListView, piece)
            }
            Log.d("테스트", "setList($index, $newOne)")
            if(index == 0 && newOne) holder.setHighlight()
            recyclerView.addView(view)
        }
        if(newOne) newOne = false
        holder?.borderView?.visibility = View.INVISIBLE
    }
//    inner class StudyListAdapter: RecyclerView.Adapter<StudyListViewHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyListViewHolder {
//            val view = LayoutInflater.from(context).inflate(R.layout.item_study_list, parent, false)
//            return StudyListViewHolder(view)
//        }
//
//        override fun getItemCount(): Int {
//            return contents.size
//        }
//
//        override fun onBindViewHolder(holder: StudyListViewHolder, position: Int) {
//            val content = contents[position]
//            holder.set(content)
//            if(position == contents.size - 1) {
//                holder.borderView.visibility = View.INVISIBLE
//            } else {
//                holder.borderView.visibility = View.VISIBLE
//            }
//            holder.solveBtn.setPermissionClickListener {
//                listener?.onSolveBtnClicked(this@AnalysisTodayStudyListView, content)
//            }
//            holder.reportBtn.setOnClickListener {
//                listener?.onReportBtnClicked(this@AnalysisTodayStudyListView, content)
//            }
//        }
//    }
}

class StudyListViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    val dateTv = view.dateTv
    val categoryTv = view.categoryTv
    val titleTv = view.titleTv
    val problemCntTv = view.problemCntTv
    val problemTotalCntTv = view.problemTotalCntTv
    val scoreTv = view.scoreTv
    val reportBtn = view.reportBtn
    val solveBtn = view.solveBtn
    val borderView = view.borderView

    fun set(piece: Content) {
        dateTv.text = DateTimeUtils.mMDashddFormat.format(piece.updateDateTime)
        titleTv.text = piece.subject
//        problemCntTv.text = if(piece.similarProblemNumber > 0) "${piece.markedNumber}(+${piece.similarProblemNumber})문제" else "${piece.markedNumber}문제"

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

    }

    fun setUserAnalysisUI() {
        solveBtn.visibility = View.INVISIBLE
    }

    fun setHighlight() {
        view.setBackgroundResource(R.color.yellow_fffbef)
    }
}