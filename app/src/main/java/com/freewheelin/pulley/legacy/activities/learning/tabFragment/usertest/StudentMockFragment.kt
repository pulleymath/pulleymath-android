package com.freewheelin.pulley.legacy.activities.learning.tabFragment.usertest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentMyMockBinding
import com.freewheelin.pulley.databinding.ItemMyMockHeaderBinding
import com.freewheelin.pulley.databinding.ItemMyMockListBinding
import com.freewheelin.pulley.legacy.bases.is10InchUI
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.MockExamManager
import com.freewheelin.pulley.legacy.lib.ObservableHashSet
import com.freewheelin.pulley.legacy.lib.ObservableHashSetListener
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.textViews.SortableListener
import com.freewheelin.pulley.legacy.views.textViews.SortableTextView
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity.Companion.REQUEST_MOCK_TEST
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity.Companion.RESULT_MOCK_FINISH
import com.freewheelin.pulley.revision2023.ui.activity.MockTabListener

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

//    lateinit var headerView:View
    lateinit var headerBinding: ItemMyMockHeaderBinding
    lateinit var binding: FragmentMyMockBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_mock, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setInitOrder()
    }

    fun setInitOrder() {
        headerBinding.dateSl.isSelected = true
    }

    override fun onItemChanged(set: ObservableHashSet<MockExam>) {
        with(binding) {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MOCK_TEST
                && resultCode == RESULT_MOCK_FINISH) {
            listener?.onMockTestFinished()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        setHeader()

        with(binding) {
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

                this@StudentMockFragment.studentID = studentID

                MockExamManager.getStudentMockExamList(requireContext(), studentID) {

                    it?.map { it.selectOptionalSubjectSummary = it.personalData?.optionalSubjectList?.toList() ?: listOf() }
                    Log.d(javaClass.simpleName, "it==========>$it")

                    this@StudentMockFragment.exams = it
                    this@StudentMockFragment.sortedExams = exams
                    myExamRv.adapter?.notifyDataSetChanged()
                    configureUI()
                }
            }
        }

    }

    private fun setHeader() {
        headerBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.item_my_mock_header, binding.headerContainer, false)

//        headerView = headerBinding.root
        val holder = StudentMockHeadHolder(headerBinding)
        binding.headerContainer.addView(headerBinding.root)

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
        with(binding) {
            newExamBtn.visibility = View.GONE
            if (getExamList().isEmpty()) {
                emptyContainerCl.visibility = View.VISIBLE
                myExamRv.visibility = View.GONE
            } else {
                emptyContainerCl.visibility = View.GONE
                myExamRv.visibility = View.VISIBLE
            }
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

        binding.myExamRv.adapter?.notifyDataSetChanged()
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
            return StudentMockHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_my_mock_list, parent, false))
        }
    }
}



class StudentMockHeadHolder(val headerBinding: ItemMyMockHeaderBinding) {
    val typeSl = headerBinding.typeSl
    val gradeSl = headerBinding.gradeSl
    val examTitleSl = headerBinding.examTitleSl
    val percentSl = headerBinding.percentSl
    val scoreSl = headerBinding.scoreSl
    val dateSl = headerBinding.dateSl
    val ratingSl = headerBinding.ratingSl
    val percentageSl = headerBinding.percentageSl
    val review = headerBinding.reviewContainer

    init {
        review.visibility = View.GONE
    }

    val sortableTextViews: List<SortableTextView>
        get() = listOf(
            headerBinding.typeSl, headerBinding.gradeSl, headerBinding.examTitleSl, headerBinding.percentSl, headerBinding.scoreSl, headerBinding.dateSl, headerBinding.ratingSl, headerBinding.percentageSl
        )
}

class StudentMockHolder(val itemBinding: ItemMyMockListBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    var typeTv = itemBinding.typeTv
    var gradeTv = itemBinding.gradeTv
    var titleTv = itemBinding.titleTv
    var scoreTv = itemBinding.scoreTv
    var percentageTv = itemBinding.percentageTv
    var ratingTv = itemBinding.ratingTv
    var remainBtn = itemBinding.remainBtn
    var remainCountText = itemBinding.remainCountText
    var dateTv = itemBinding.dateTv
    var ratingIv = itemBinding.ratingIv
    val reportBtn = itemBinding.reportBtn
    val horizontalBorder = itemBinding.horizontalBorder
    val reviewBtn = itemBinding.reviewBtn
    val containerCl = itemBinding.containerCl
    val outContainerCl = itemBinding.outContainerCl

    val correctPercentTv = itemBinding.correctPercentTv
    val correctCountTv = itemBinding.correctCountTv

    val optionContainer = itemBinding.optionContainer
    val optionContainer8inch = itemBinding.optionContainer8inch

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
            if (!itemBinding.root.context.is10InchUI) {
                if(it.optionalSubjectList.isEmpty()) {
                    optionContainer8inch.visibility = View.GONE
                } else {
                    optionContainer8inch.visibility = View.VISIBLE
                }
            }
            // add
            for(subject in it.optionalSubjectList) {
                if (itemBinding.root.context.is10InchUI) {
                    val label = LayoutInflater.from(itemBinding.root.context).inflate(R.layout.item_mockexam_mymock_option_label,optionContainer, false) as TextView
                    label.text = subject.title
                    optionContainer.addView(label)
                } else {
                    val label = LayoutInflater.from(itemBinding.root.context).inflate(R.layout.item_mockexam_mymock_option_label, optionContainer8inch, false) as TextView
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
        textView.setTextColor( ContextCompat.getColor(textView.context, R.color.gray_500))
        textView.text = "-"
    }

    fun setString(textView:TextView, string:String) {
        textView.setTextColor( ContextCompat.getColor(textView.context, R.color.gray_800))
        textView.text = string
    }

    fun setLastHolderUI() {
        horizontalBorder.visibility = View.GONE
        outContainerCl.layoutParams.apply {
            height = 112.toPx()
        }
        outContainerCl.background = ContextCompat.getDrawable(itemBinding.root.context, R.drawable.bg_shadow_bottom)
    }

    fun setMiddleHolderUI() {
        horizontalBorder.visibility = View.VISIBLE
        outContainerCl.layoutParams.apply {
            height = itemBinding.root.context.resources.getDimension(R.dimen.dp64).toInt()
        }
        outContainerCl.background = ContextCompat.getDrawable(itemBinding.root.context, R.drawable.bg_shadow_middle)
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
