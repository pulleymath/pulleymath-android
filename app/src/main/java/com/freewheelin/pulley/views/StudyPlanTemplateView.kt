package com.freewheelin.pulley.views

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.*
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.databinding.ViewStudyplanLearnedBinding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx


interface StudyPlanTemplateInteface {
    fun onHidden(view: StudyPlanTemplateView, position: Int)
    fun onSolveBtnClicked(book: Book)
    fun onMailBtnClicked(book: Book)
    fun onReviewBtnClicked(book: Book)
}

class StudyPlanTemplateView : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    lateinit var book: Book
    var listener: StudyPlanTemplateInteface? = null
    var position = 0

    val binding: ViewStudyplanLearnedBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_studyplan_learned, null, false)
    }
    init {
        radius = 5f.toPx()

        with(binding) {
            mailBtn.setOnClickListener {
                listener?.onMailBtnClicked(book)
            }

            solveBtn.setOnClickListener {
                listener?.onSolveBtnClicked(book)
            }
            reviewBtn.setOnClickListener {
                listener?.onReviewBtnClicked(book)
            }
            useCompatPadding = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val width = if(context.is10InchUI)
            (DisplayUtils.getScreenWidth(context) - (16.toPx() * 10)) / 4
        else
            (DisplayUtils.getScreenWidth(context) - 180.toPx()) / 3
        layoutParams.width = width
    }
    
    fun set(book: Book) {
        this.book = book
        with(binding) {
            bookNameTv.text = book.bookName
            subjectTv.text = book.subject
            chapterTv.text = book.chapter
            descTv.text = book.description
            problemCntTv.text = "${book.markedNumber}/${book.totalNumber}"
            progressBar.set((book.markedNumber.toFloat() / book.totalNumber.toFloat()))

            if(book.isCompleted()) {
                blackCl.setBackgroundColor(ContextCompat.getColor(context, R.color.white_fafafa))
                bookNameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_e0e0e0))
                subjectTv.setTextColor(ContextCompat.getColor(context, R.color.grey_e0e0e0))
                chapterTv.setTextColor(ContextCompat.getColor(context, R.color.grey_e0e0e0))
                dateTv.setTextColor(ContextCompat.getColor(context, R.color.grey_e0e0e0))

                progressTitleTv.text = "학습 완료!"
                progressTitleTv.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                problemCntTv.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                mailBtn.setBackgroundResource(R.drawable.bg_grey_f2f2f2_round)
                completeIv.visibility = View.VISIBLE
                reviewBtn.visibility = View.VISIBLE
                solveBtn.visibility = View.INVISIBLE
            } else {
                blackCl.setBackgroundColor(ContextCompat.getColor(context, R.color.black_333333))
                bookNameTv.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                subjectTv.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                chapterTv.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                dateTv.setTextColor(ContextCompat.getColor(context, R.color.grey_9f9f9f))

                progressTitleTv.text = "학습량"
                progressTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                problemCntTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                mailBtn.setBackgroundResource(R.drawable.bg_grey_3d3d3d_round)
                completeIv.visibility = View.INVISIBLE
                reviewBtn.visibility = View.INVISIBLE
                solveBtn.visibility = View.VISIBLE
            }

            if(book.addNewAssignPlan) {
                newTag.visibility = View.VISIBLE
            } else {
                newTag.visibility = View.GONE
            }
            if(book.isCompleted()) {
                newTag.setTextColor(ContextCompat.getColor(context, R.color.red_ffe8e8))
            } else {
                newTag.setTextColor(ContextCompat.getColor(context, R.color.red_fe7b67))
            }

            if(book.updateDateTime == null) {
                dateTv.visibility = View.GONE
            } else {
                dateTv.visibility = View.VISIBLE
                dateTv.text = "최근 학습일 ${DateTimeUtils.mMddFormat.format(book.updateDateTime!!)}"
            }
        }
    }
}
