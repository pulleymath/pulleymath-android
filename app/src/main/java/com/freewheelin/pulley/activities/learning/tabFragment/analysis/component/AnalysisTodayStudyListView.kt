package com.freewheelin.pulley.activities.learning.tabFragment.analysis.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.ItemStudyListBinding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.BookType
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.SecondaryButton

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
    var recyclerView: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_today_study_list, this)

        viewAllListBtn = findViewById(R.id.viewAllListBtn)
        studyBtn = findViewById(R.id.studyBtn)
        emptyGuideTv = findViewById(R.id.emptyGuideTv)
        recyclerView = findViewById(R.id.recyclerView)

        setList()
        viewAllListBtn.setOnClickListener {
            println("asoaso viewAllListBtn click!!! listener null? : ${listener == null}")
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
            holder = StudyListViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_study_list, null, false))

            holder.set(piece)
            if (isUserAnalysis) holder.setUserAnalysisUI()

            holder.listBinding.solveBtnWrapperCl.apply {
                // TODO start challenge reward는 islocked 이 풀려있나?
                setOnClickListener {
                    if (piece.isLocked) {
//                        DialogUtils.confirmDialog(context, "[테스트]구독중이 아닙니다.", "돈내놔!")
                        val dialog = PurchaseGuideDialog()
                        FragmentManager.findFragment<PurchaseGuideDialog>(this@AnalysisTodayStudyListView).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
                    }
                    else {
                        println("asoaso solveBtnWrapperCl click!!! listener null? : ${listener == null}")
                        println("asoaso solveBtnWrapperCl click!!! bookSeries: ${Book(piece).bookCategoryList?.bookSeries}")
                        println("asoaso solveBtnWrapperCl click!!! subca?: ${piece.pieceSubCategory}")
                        listener?.onSolveBtnClicked(this@AnalysisTodayStudyListView, piece)
                    }
                }
            }
            holder.listBinding.reportBtn.apply {
                setOnClickListener {
                    if (piece.isLocked) {
                        val dialog = PurchaseGuideDialog()
                        FragmentManager.findFragment<PurchaseGuideDialog>(this@AnalysisTodayStudyListView).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
                    }
                    else { listener?.onReportBtnClicked(this@AnalysisTodayStudyListView, piece) }
                }
            }
            Log.d("테스트", "setList($index, $newOne)")
            if(index == 0 && newOne) holder.setHighlight()
            recyclerView.addView(holder.listBinding.root)
        }
        if(newOne) newOne = false
        holder?.listBinding?.borderView?.visibility = View.INVISIBLE
    }
}

class StudyListViewHolder(val listBinding: ItemStudyListBinding): RecyclerView.ViewHolder(listBinding.root) {
    fun set(piece: Content) {
        listBinding.apply {
            dateTv.text = DateTimeUtils.mMDashddFormat.format(piece.updateDateTime)
            titleTv.text = piece.subject

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

            solveLockIv.visibleIf(piece.isLocked)
        }

    }

    fun setUserAnalysisUI() {
        listBinding.solveBtnWrapperCl.visibility = View.INVISIBLE
    }

    fun setHighlight() {
        listBinding.root.setBackgroundResource(R.color.yellow_fffbef)
    }
}