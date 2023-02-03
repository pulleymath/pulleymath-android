package com.freewheelin.pulley.activities.learning.tabFragment.analysis

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.activities.WrongTestReportActivity
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.component.StudyListViewHolder
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.ActivityStudyHistoryBinding
import com.freewheelin.pulley.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio

class StudyHistoryActivity : AppCompatActivity(), DabakTabRadioListener, MockExamGuideDialogListener {
    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        binding.apply {
            var filteredList = contents

            filteredList = when(categoryTab.selectedIndex) {
                1 -> filteredList.filter { it.pieceCategoryTag == BookType.BOOK || it.pieceCategoryTag == BookType.CUSTOM_BOOK }
                2 -> filteredList.filter { it.pieceCategoryTag == BookType.MO  }
                3 -> filteredList.filter { it.pieceCategoryTag == BookType.NOTE }
                4 -> filteredList.filter { it.pieceCategoryTag == BookType.TEST }
                5 -> filteredList.filter { it.pieceCategoryTag == BookType.RECOMMEND }
                else -> filteredList
            }

            filteredList = when(ingTab.selectedIndex) {
                1 -> filteredList.filter { !it.isCompleted() }
                2 -> filteredList.filter { it.isCompleted() }
                else -> filteredList
            }

            this@StudyHistoryActivity.filteredContents = filteredList
            recyclerView.adapter?.notifyDataSetChanged()

            if(getContentList().isEmpty()) {
                emptyGuideTv.visibility = View.VISIBLE
            } else {
                emptyGuideTv.visibility = View.INVISIBLE
            }
        }
    }

    var contents: List<Content> = emptyList()
    var filteredContents: List<Content>? = null

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StudyHistoryActivity::class.java)
        }
    }
    private val binding: ActivityStudyHistoryBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_study_history, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpUI()
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            user?.getStudyList(this@StudyHistoryActivity) {
                this@StudyHistoryActivity.contents = it
                if(this@StudyHistoryActivity.contents.isEmpty())
                    emptyGuideTv.visibility = View.VISIBLE
                else
                    emptyGuideTv.visibility = View.INVISIBLE


                if(recyclerView.adapter == null) {
                    recyclerView.layoutManager = LinearLayoutManager(this@StudyHistoryActivity, LinearLayoutManager.VERTICAL, false)
                    recyclerView.adapter = StudyListAdapter()
                } else {
                    recyclerView.adapter?.notifyDataSetChanged()
                }

                recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                        super.onScrolled(recyclerView, dx, dy)
//                    Log.d("스크롤", "scrollY=$dy")
                        if(dy > 0) {
                            viewShadow.visibility = View.VISIBLE
                        } else {
                            viewShadow.visibility = View.INVISIBLE
                        }
                    }
                })
            }
        }
    }

    fun setUpUI() {
        binding.apply {
            backBtn.setOnClickListener { finish() }
            categoryTab.labels = listOf("전체", "유형학습", "모의고사", "오답학습", "테스트", "추천학습")
            ingTab.labels = listOf("전체", "학습 중", "학습 완료")

            categoryTab.listener = this@StudyHistoryActivity
            ingTab.listener = this@StudyHistoryActivity

        }


    }

    private fun getContentList(): List<Content> {
        return filteredContents ?: contents
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    inner class StudyListAdapter: RecyclerView.Adapter<StudyListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyListViewHolder {
            return StudyListViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_list, parent, false))
        }

        override fun getItemCount(): Int {
            return getContentList().size
        }

        override fun onBindViewHolder(holder: StudyListViewHolder, position: Int) {
            val content = getContentList()[position]
            holder.set(content)
            if(position == getContentList().size - 1) {
                holder.listBinding.borderView.visibility = View.INVISIBLE
            } else {
                holder.listBinding.borderView.visibility = View.VISIBLE
            }

            holder.listBinding.reportBtn.setOnClickListener {
                LogUtils.logEvent(this@StudyHistoryActivity, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역보고서")
                when(content.pieceCategoryTag) {
                    BookType.MO -> {
                        getMockWithOptionalSubjects(content) { mock ->
                            val intent = MockReportActivity.getIntent(this@StudyHistoryActivity, mock)
                            startActivity(intent)
                        }
                    }
                    BookType.TEST -> {
                        val test = Test(content)
                        when(test.getTestType()) {
                            Test.TestType.weekly ->  {
                                val intent = WeeklyTestReportActivity.getIntent(this@StudyHistoryActivity, test)
                                startActivity(intent)
                            }
                            Test.TestType.wrong -> {
                                val intent = WrongTestReportActivity.getIntent(this@StudyHistoryActivity, test)
                                startActivity(intent)
                            }
                            else -> {
                                LogUtils.assert(false, "예상치 못한 테스트 타입 ${test.getTestType()}")
                            }
                        }
                    }
                    else -> {
                        LogUtils.assert(false, "예상치 못한 카테고리 ${content.category}")
                    }
                }
            }

            holder.listBinding.solveBtn.setOnClickListener {
                LogUtils.logEvent(this@StudyHistoryActivity, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역풀기")
                when(content.pieceCategoryTag) {
                    BookType.MO -> {
                        if(content.isCompleted()) {
                            getMockWithOptionalSubjects(content) { mock ->
                                val intent = SolveActivity.getReviewIntent(this@StudyHistoryActivity, mock)
                                startActivity(intent)
                            }
                        } else {
                            val exam = MockExam(content)
                            MockExamGuideDialog(this@StudyHistoryActivity, exam, true, this@StudyHistoryActivity).show()
                        }
                    }
                    BookType.CUSTOM_BOOK -> {
                        val intent = SolveActivity.getIntent(this@StudyHistoryActivity, Book(content))
                        startActivity(intent)
                    }
                    BookType.BOOK -> {
                        val intent = if(content.isCompleted())
                            SolveActivity.getReviewIntent(this@StudyHistoryActivity, Book(content))
                        else
                            SolveActivity.getIntent(this@StudyHistoryActivity, Book(content))
                        startActivity(intent)
                    }

                    BookType.NOTE, BookType.RECOMMEND -> {
                        val intent = if(content.isCompleted()) {
                            SolveActivity.getReviewIntent(this@StudyHistoryActivity, Piece(content))
                        } else {
                            SolveActivity.getIntent(this@StudyHistoryActivity, Piece(content))
                        }
                        startActivity(intent)
                    }

                    BookType.TEST -> {
                        val intent = if(content.isCompleted())
                            SolveActivity.getReviewIntent(this@StudyHistoryActivity, Test(content))
                        else
                            SolveActivity.getIntent(this@StudyHistoryActivity, Test(content))
                        startActivity(intent)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getMockWithOptionalSubjects(content: Content, cb: (summary: MockExam) -> Unit) {
        val mock = MockExam(content)
        MockExamManager.getMockSummary(this, content.mockID, user!!) { mockExamSummery ->
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
