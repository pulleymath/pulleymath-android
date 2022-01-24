package com.freewheelin.pulley.activities.learning.tabFragment.mockExam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.MockReportActivity
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment.Companion.REQUEST_MOCK_TEST
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment.Companion.RESULT_MOCK_FINISH
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.StudentMockReportActivity
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.TextViews.SortableListener
import com.freewheelin.pulley.views.TextViews.SortableTextView
import kotlinx.android.synthetic.main.dialog_wrong_management.*
import kotlinx.android.synthetic.main.fragment_my_mock.*
import kotlinx.android.synthetic.main.fragment_new_mock.*
import kotlinx.android.synthetic.main.item_my_mock_header.view.*
import kotlinx.android.synthetic.main.item_my_mock_list.view.*
import java.util.*

class StudentMockFragment : Fragment(), ObservableHashSetListener<MockExam> {

    enum class SortType {
        category,
        grade,
        title,
        correctPercent,
        score,
        solvedDate,
        rating,
        percentage
    }

    var exams: List<MockExam>? = null
    var sortedExams: List<MockExam>? = null

    var listener: MockTabListener? = null

    var order: SortableTextView.Order = SortableTextView.Order.descend
    var selectedSort: SortType? = null

    var studentID: String? = ""

    lateinit var headerView:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_my_mock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setInitOrder()
    }

    fun setInitOrder() {
        headerView.dateSl.isSelected = true
    }

    override fun onItemChanged(set: ObservableHashSet<MockExam>) {
        if (set.isEmpty()) {
            wrongManageView.inactive()
            wrongManageView.hide(true)
        } else {
            if (set.size == 1)
                wrongManageView.active("'${set.first().title}'이 선택되었습니다.")
            else
                wrongManageView.active("'${set.first().title}' 외 ${set.size - 1}건이 선택되었습니다.")
            wrongManageView.show(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MOCK_TEST
                && resultCode == RESULT_MOCK_FINISH) {
            listener?.onMockTestFinished()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {

        myExamRv.layoutManager = LinearLayoutManager(context)
        myExamRv.adapter = StudentExamAdapter()

        wrongManageView.hideReviewBtn()
        wrongManageView.hide(false)

        newExamBtn.setOnClickListener {
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "새로풀기", "나의모의고사")
            listener?.onNewExamBtnClicked()
        }

        Log.d(javaClass.simpleName, "studentID==========>${arguments?.getString("studentID")}")

        arguments?.getString("studentID")?.let { studentID ->

            this.studentID = studentID

            MockExamManager.getStudentMockExamList(requireContext(), studentID) {

                it?.map { it.selectOptional = it.personalData?.optionalSubjectList?.map { CommercialSubject.valueOf(it.subjectCodeType) }?.toMutableList() ?: mutableListOf() }

                Log.d(javaClass.simpleName, "it==========>$it")

                this@StudentMockFragment.exams = it
                this@StudentMockFragment.sortedExams = exams
                myExamRv.adapter?.notifyDataSetChanged()
                configureUI()
            }
        }

        setHeader()
    }

    private fun setHeader() {
        headerView = LayoutInflater.from(context).inflate(R.layout.item_my_mock_header, headerContainer, false)
        val holder = StudentMockHeadHolder(headerView)
        headerContainer.addView(headerView)

        holder.sortableTextViews.forEach {
            it.listener = (object : SortableListener {
                override fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order) {
                    holder.sortableTextViews.forEach { it.isSelected = false }
                    view.isSelected = true
                    this@StudentMockFragment.onOrderChanged(holder, view, order)
                }
            })
        }
    }

    private fun configureUI() {
        newExamBtn.visibility = View.GONE
        if (getExamList().isEmpty()) {
            emptyContainerCl.visibility = View.VISIBLE
            myExamRv.visibility = View.GONE
        } else {
            emptyContainerCl.visibility = View.GONE
            myExamRv.visibility = View.VISIBLE
        }
    }

    private fun getExamList(): List<MockExam> {
        return this.exams ?: listOf()
    }

    private fun onReportBtnClicked(exam: MockExam) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "선생님>학생목록", "학생별 모의고사", "보고서")
        val intent = StudentMockReportActivity.getIntent(requireContext(), exam, studentID?:"")
        startActivity(intent)
    }

    fun onOrderChanged(holder: StudentMockHeadHolder, view: SortableTextView, order: SortableTextView.Order) = holder.apply {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "선생님>학생목록", "학생별 모의고사>정렬", view.label)
        when (view) {
            typeSl -> selectedSort = SortType.category
            gradeSl -> selectedSort = SortType.grade
            examTitleSl -> selectedSort = SortType.title
            percentSl -> selectedSort = SortType.correctPercent
            scoreSl -> selectedSort = SortType.score
            dateSl -> selectedSort = SortType.solvedDate
            ratingSl -> selectedSort = SortType.rating
            percentageSl -> selectedSort = SortType.percentage
            else -> {
                LogUtils.assert(false, "Unexpected view type")
            }
        }

        this@StudentMockFragment.order = order
        sortedExams = sortExamList()

        myExamRv.adapter?.notifyDataSetChanged()
    }

    private fun sortExamList(): List<MockExam>? {
        return when (selectedSort) {

            SortType.category -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.type.sortPriority() }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.type.sortPriority() }
                }
            }
            SortType.grade -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.grade }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.grade }
                }
            }
            SortType.title -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.title }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.title }
                }
            }
            SortType.correctPercent -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.personalData?.correctRate?: 0 }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.personalData?.correctRate?: 0 }
                }
            }
            SortType.score -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.personalData?.score?: 0 }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.personalData?.score?: 0 }
                }
            }
            SortType.solvedDate -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.createDate.time }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.createDate.time }
                }
            }
            SortType.rating -> {
                when (order) {
                    SortableTextView.Order.descend -> exams?.sortedBy { it.personalData?.rating?: 99 }
                    SortableTextView.Order.ascend -> exams?.sortedByDescending { it.personalData?.rating?: 99 }
                }
            }
            SortType.percentage -> {
                when (order) {
                    SortableTextView.Order.ascend -> exams?.sortedBy { it.personalData?.percent?: 0 }
                    SortableTextView.Order.descend -> exams?.sortedByDescending { it.personalData?.percent?: 0 }
                }
            }
            else -> {
                exams
            }
        }
    }

    inner class StudentExamAdapter : RecyclerView.Adapter<StudentMockHolder>() {

        override fun onBindViewHolder(holder: StudentMockHolder, position: Int) {
            val exam = sortedExams!![position]

            if (exam == sortedExams!!.last())
                holder.setLastHolderUI()
            else
                holder.setMiddleHolderUI()

            holder.set(exam)
            holder.reportBtn.setOnClickListener { onReportBtnClicked(exam) }
        }

        override fun getItemCount(): Int {
            return sortedExams?.size?:0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentMockHolder {
            return StudentMockHolder(LayoutInflater.from(context).inflate(R.layout.item_my_mock_list, parent, false))
        }
    }
}



class StudentMockHeadHolder(val view: View) {
    val typeSl = view.typeSl
    val gradeSl = view.gradeSl
    val examTitleSl = view.examTitleSl
    val percentSl = view.percentSl
    val scoreSl = view.scoreSl
    val dateSl = view.dateSl
    val ratingSl = view.ratingSl
    val percentageSl = view.percentageSl
    val review = view.reviewContainer

    init {
        review.visibility = View.GONE
    }

    val sortableTextViews: List<SortableTextView>
        get() = listOf(
                view.typeSl, view.gradeSl, view.examTitleSl, view.percentSl, view.scoreSl, view.dateSl, view.ratingSl, view.percentageSl
        )
}

class StudentMockHolder(val view: View) : RecyclerView.ViewHolder(view) {

    var typeTv = view.findViewById<TextView>(R.id.typeTv)
    var gradeTv = view.findViewById<TextView>(R.id.gradeTv)
    var titleTv = view.findViewById<TextView>(R.id.titleTv)
    var scoreTv = view.findViewById<TextView>(R.id.scoreTv)
    var percentageTv = view.findViewById<TextView>(R.id.percentageTv)
    var ratingTv = view.findViewById<TextView>(R.id.ratingTv)
    var remainBtn = view.remainBtn
    var remainCountText = view.remainCountText
    var dateTv = view.findViewById<TextView>(R.id.dateTv)
    var ratingIv = view.findViewById<ImageView>(R.id.ratingIv)
    val reportBtn = view.reportBtn
    val horizontalBorder = view.horizontalBorder
    val reviewBtn = view.reviewBtn
    val containerCl = view.containerCl
    val outContainerCl = view.outContainerCl

    val correctPercentTv = view.correctPercentTv
    val correctCountTv = view.correctCountTv

    val optionContainer = view.optionContainer
    val optionContainer8inch = view.optionContainer8inch

    init {
        remainBtn.visibility = View.GONE
        reviewBtn.visibility = View.GONE
    }

    fun set(exam: MockExam) {

        dateTv.text = DateTimeUtils.getBeforeDateStr(date = exam.createDate)

        typeTv.text = exam.type.getStr()
        gradeTv.text = "고${exam.grade}"

        titleTv.text = exam.title //+ if(exam.count > 0) " (${exam.count})" else ""

        exam.personalData?.let {
            // 제거 후
            optionContainer.removeAllViews()
            optionContainer8inch.removeAllViews()

            // 코드가 지저분 하긴 한데.. 생각이 안난다...
            // 8인치 미만에서 레이블 컨테이너 없애기 - 세로 정렬 어긋나는거 때문에
            if (!view.context.is10InchUI) {
                if(it.optionalSubjectList.isEmpty()) {
                    optionContainer8inch.visibility = View.GONE
                } else {
                    optionContainer8inch.visibility = View.VISIBLE
                }
            }
            // add
            for(subject in it.optionalSubjectList) {
                if (view.context.is10InchUI) {
                    val label = LayoutInflater.from(view.context).inflate(R.layout.item_mockexam_mymock_option_label,optionContainer, false) as TextView
                    label.text = subject.title
                    optionContainer.addView(label)
                } else {
                    val label = LayoutInflater.from(view.context).inflate(R.layout.item_mockexam_mymock_option_label, optionContainer8inch, false) as TextView
                    label.text = subject.title
                    optionContainer8inch.addView(label)
                }
            }

            if (exam.isPersonalCompleted()) {
                setString(percentageTv, "${it.percent}%")
                setString(ratingTv, "${it.rating}")
                setRating(it.rating)
                setString(scoreTv, "${it.score}점")
                setString(correctPercentTv, "${it.correctRate}%")

                correctCountTv.text = "${it.correctCount}/${it.totalNumber}"

                reportBtn.visibility = View.VISIBLE

                correctPercentTv.visibility = View.VISIBLE
                correctCountTv.visibility = View.VISIBLE

            } else {
                setNone(scoreTv)
                setNone(percentageTv)
                setNone(ratingTv)
                setNone(correctPercentTv)

                it.percent = -1
                it.rating = 99
                it.score = -1
                it.correctRate = -1

                ratingIv.visibility = View.INVISIBLE
                ratingTv.visibility = View.VISIBLE
                reportBtn.visibility = View.INVISIBLE

                remainCountText.text = "${it.totalNumber-it.markedNumber}문항"

                correctPercentTv.visibility = View.VISIBLE
                correctCountTv.visibility = View.GONE
            }

            // 백분위가 null 이면 점수와 백분위 "-"
            if(it.percent == null || it.percent?:0 < 0) {
                setNone(percentageTv)
                setNone(scoreTv)
                setNone(ratingTv)

                it.percent = -1
                it.rating = 99
                it.score = -1
            }
        }
    }

    fun setNone(textView:TextView) {
        textView.setTextColor( ContextCompat.getColor(textView.context, R.color.grey_c0c0c0))
        textView.text = "-"
    }

    fun setString(textView:TextView, string:String) {
        textView.setTextColor( ContextCompat.getColor(textView.context, R.color.black_4c4c4c))
        textView.text = string
    }

    fun setLastHolderUI() {
        horizontalBorder.visibility = View.GONE
        outContainerCl.layoutParams.apply {
            height = 112.toPx()
        }
        outContainerCl.background = ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_bottom)
    }

    fun setMiddleHolderUI() {
        horizontalBorder.visibility = View.VISIBLE
        outContainerCl.layoutParams.apply {
            height = view.context.resources.getDimension(R.dimen.dp64).toInt()
        }
        outContainerCl.background = ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_middle)
    }

    private fun setRating(rating: Int?) {
        when (rating) {
            1 -> {
                ratingTv.visibility = View.INVISIBLE
                ratingIv.visibility = View.VISIBLE
                ratingIv.setImageResource(R.drawable.ic_rating_1)
            }
            2 -> {
                ratingTv.visibility = View.INVISIBLE
                ratingIv.visibility = View.VISIBLE
                ratingIv.setImageResource(R.drawable.ic_rating_2)
            }
            in 3..9 -> {
                ratingIv.visibility = View.INVISIBLE
                ratingTv.visibility = View.VISIBLE
                ratingTv.text = "$rating"
            }
            else -> {
                ratingIv.visibility = View.INVISIBLE
                ratingTv.visibility = View.VISIBLE
                ratingTv.text = "-"
            }
        }
    }
}
