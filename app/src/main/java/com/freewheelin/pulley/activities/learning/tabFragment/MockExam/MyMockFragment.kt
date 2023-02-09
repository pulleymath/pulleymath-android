package com.freewheelin.pulley.activities.learning.tabFragment.mockExam


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment.Companion.REQUEST_MOCK_TEST
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment.Companion.RESULT_MOCK_FINISH
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.FragmentMyMockBinding
import com.freewheelin.pulley.databinding.ItemMyMockHeaderBinding
import com.freewheelin.pulley.databinding.ItemMyMockListBinding
import com.freewheelin.pulley.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.model.contents.MarkingState
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.textViews.SortableListener
import com.freewheelin.pulley.views.textViews.SortableTextView
import com.github.mikephil.charting.data.Entry
import java.util.*
import kotlin.collections.ArrayList

class MyMockFragment : Fragment(), ObservableHashSetListener<MockExam>, MockExamGuideDialogListener {

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

    var selectedScoreEntries = ArrayList<Entry>()
    var selectedPercentageEntries = ArrayList<Entry>()
    var listener: MockTabListener? = null

    lateinit var binding: FragmentMyMockBinding
    lateinit var headerViewBinding: ItemMyMockHeaderBinding
    lateinit var receiver: BroadcastReceiver
    lateinit var clearReceiver: BroadcastReceiver

    var order: SortableTextView.Order = SortableTextView.Order.descend
    var selectedSort: SortType? = null

    companion object {
        @JvmStatic
        fun newInstance(): MyMockFragment {
            val fragment = MyMockFragment()
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                MockExamManager.getMyMockExamList(context, user!!) {
//                    val exam = getSerializable(requireActivity(), MockExamManager.ARG_MOCK_EXAM, MockExam::class.java)
                    val exam = intent.getSerializableExtra(MockExamManager.ARG_MOCK_EXAM) as MockExam
                    val resultExam = it?.filter { it.assignID == exam.assignID }?.firstOrNull()
                    if (resultExam?.getMakringState() == MarkingState.COMPLETED) {
                        selectedPercentageEntries.clear()
                        selectedScoreEntries.clear()
                    }
                    it?.map { it.selectOptional = it.personalData?.optionalSubjectList?.map { CommercialSubject.valueOf(it.subjectCodeType) }?.toMutableList() ?: mutableListOf() }
                    this@MyMockFragment.exams = it
                    this@MyMockFragment.sortedExams = sortExamList()
                    binding.myExamRv.adapter?.notifyDataSetChanged()
                    configureUI()
                }
            }
        }

        clearReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                this@MyMockFragment.initUI()
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(clearReceiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_CLEAR))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_SCORING))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(clearReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        super.onDestroy()
    }

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
        headerViewBinding.dateSl.isSelected = true
    }

    override fun onItemChanged(set: ObservableHashSet<MockExam>) {
        binding.apply {
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

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, REQUEST_MOCK_TEST)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, REQUEST_MOCK_TEST)
    }

    private fun initUI() {
        binding.apply {
            myExamRv.layoutManager = LinearLayoutManager(context)
            myExamRv.adapter = MyExamAdapter()

            wrongManageView.hideReviewBtn()
            wrongManageView.hide(false)

            newExamBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "새로풀기", "나의모의고사")
                listener?.onNewExamBtnClicked()
            }

            MockExamManager.getMyMockExamList(requireContext(), user!!) {
                it?.map { it.selectOptional = it.personalData?.optionalSubjectList?.map { CommercialSubject.valueOf(it.subjectCodeType) }?.toMutableList() ?: mutableListOf() }
                this@MyMockFragment.exams = it
                this@MyMockFragment.sortedExams = exams
                myExamRv.adapter?.notifyDataSetChanged()
                configureUI()
            }

            setHeader()
        }
    }

    private fun setHeader() {
        val headerContainer = binding.headerContainer
        headerViewBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.item_my_mock_header, null, false)


        val holder = MyMockHeadHolder(headerViewBinding)
        headerContainer.addView(headerViewBinding.root)

        holder.sortableTextViews.forEach {
            it.listener = (object : SortableListener {
                override fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order) {
                    holder.sortableTextViews.forEach { it.isSelected = false }
                    view.isSelected = true
                    this@MyMockFragment.onOrderChanged(holder, view, order)
                }
            })
            it.order = SortableTextView.Order.descend
        }
    }

    private fun configureUI() {
        binding.apply {
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

    private fun onSolveBtnClicked(exam: MockExam) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "이어풀기", "나의모의고사")
        MockExamGuideDialog(requireContext(), exam, true, this@MyMockFragment).show()
    }

    private fun onReportBtnClicked(exam: MockExam) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "보고서", "나의모의고사")
        getMockWithOptionalSubjects(exam) { mock ->
            val intent = MockReportActivity.getIntent(requireContext(), mock)
            startActivity(intent)
        }
    }

    private fun onReviewBtnClicked(exam: MockExam) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "리뷰하기", "나의모의고사")
        val intent = SolveActivity.getReviewIntent(requireContext(), exam)
        startActivity(intent)
    }

    fun onOrderChanged(holder: MyMockHeadHolder, view: SortableTextView, order: SortableTextView.Order) = holder.apply {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "새로풀기-정렬", view.label)
        holder.headerBinding.apply {
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
        }

        this@MyMockFragment.order = order
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

    inner class MyExamAdapter : RecyclerView.Adapter<MyMockHolder>() {

        override fun onBindViewHolder(holder: MyMockHolder, position: Int) {
            holder.binding.apply {
                val exam = sortedExams!![position]

                if (exam == sortedExams!!.last())
                    holder.setLastHolderUI()
                else
                    holder.setMiddleHolderUI()

                holder.set(exam)
                remainBtn.setOnClickListener { onSolveBtnClicked(exam) }
                reportBtn.setOnClickListener { onReportBtnClicked(exam) }
                reviewBtn.setOnClickListener { onReviewBtnClicked(exam) }
            }
        }

        override fun getItemCount(): Int {
            return sortedExams?.size?:0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMockHolder {
            return MyMockHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_my_mock_list, parent, false))
        }
    }

    private fun getMockWithOptionalSubjects(content: Content, cb: (summary: MockExam) -> Unit) {
        val mock = MockExam(content)
        MockExamManager.getMockSummary(requireContext(), content.mockID, user!!) { mockExamSummery ->
            val optionResult = mutableListOf<CommercialSubject>()
            mockExamSummery?.let {
                val optionalSubjects = mockExamSummery.optionalSubjectSummary

                for(subject in optionalSubjects?: arrayOf()) {
                    if (subject.isSelected) {
                        optionResult.add(CommercialSubject.valueOf(subject.subjectCodeType))
                    }
                }
                mock.selectOptional = optionResult
                mock.examType = mockExamSummery.examType.let {
                    MockExam.ExamType.valueOnString(it)
                }
                mock.grade = mockExamSummery.grade
            }


            cb(mock)
        }
    }
}


class MyMockHeadHolder(val headerBinding: ItemMyMockHeaderBinding) {
    val sortableTextViews: List<SortableTextView>
        get() {
            headerBinding.apply {
                return listOf(
                    typeSl, gradeSl, examTitleSl, percentSl, scoreSl, dateSl, ratingSl, percentageSl
                )
            }
        }
}

class MyMockHolder(val binding: ItemMyMockListBinding): RecyclerView.ViewHolder(binding.root) {

    fun set(exam: MockExam) {
        val view = binding.root
        binding.apply {
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
                    if (it.optionalSubjectList.isEmpty()) {
                        optionContainer8inch.visibility = View.GONE
                    } else {
                        optionContainer8inch.visibility = View.VISIBLE
                    }
                }
                // add
                for (subject in it.optionalSubjectList) {
                    if (view.context.is10InchUI) {
                        val label = LayoutInflater.from(view.context).inflate(
                            R.layout.item_mockexam_mymock_option_label,
                            optionContainer,
                            false
                        ) as TextView
                        label.text = subject.title
                        optionContainer.addView(label)
                    } else {
                        val label = LayoutInflater.from(view.context).inflate(
                            R.layout.item_mockexam_mymock_option_label,
                            optionContainer8inch,
                            false
                        ) as TextView
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
                    remainBtn.visibility = View.INVISIBLE
                    reviewBtn.visibility = View.VISIBLE

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

                    if (view.context.isTablet) {
                        ratingIv.visibility = View.INVISIBLE
                        ratingTv.visibility = View.VISIBLE
                        reportBtn.visibility = View.INVISIBLE
                        remainBtn.visibility = View.VISIBLE
                        reviewBtn.visibility = View.INVISIBLE

                        remainCountText.text = "${it.totalNumber - it.markedNumber}문항"

                        correctPercentTv.visibility = View.VISIBLE
                        correctCountTv.visibility = View.GONE
                    }
                }

                // 백분위가 null 이면 점수와 백분위 "-"
                if (it.percent == null || it.percent ?: 0 < 0) {
                    setNone(percentageTv)
                    setNone(scoreTv)
                    setNone(ratingTv)

                    it.percent = -1
                    it.rating = 99
                    it.score = -1
                }
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
        val view = binding.root
        binding.apply {
            horizontalBorder.visibility = View.GONE
            outContainerCl.layoutParams.apply {
                height = 112.toPx()
            }
            outContainerCl.background =
                ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_bottom)
        }
    }

    fun setMiddleHolderUI() {
        val view = binding.root
        binding.apply {
            horizontalBorder.visibility = View.VISIBLE
            outContainerCl.layoutParams.apply {
                height = view.context.resources.getDimension(R.dimen.dp64).toInt()
            }
            outContainerCl.background =
                ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_middle)
        }
    }

    private fun setRating(rating: Int?) {
        val view = binding.root
        binding.apply {
            if (view.context.isTablet) {
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
    }
}