package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityStudyHistoryBinding
import com.freewheelin.pulley.legacy.activities.OMRActivity
import com.freewheelin.pulley.legacy.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.legacy.activities.WrongTestReportActivity
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.component.StudyListViewHolder
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.manage.MockExamManager
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.BookType
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.utils.autoCloseOnChatBotExit
import com.freewheelin.pulley.legacy.views.DabakTabRadioListener
import com.freewheelin.pulley.legacy.views.DaebakTabRadio
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudyHistoryActivity : AppCompatActivity(), DabakTabRadioListener, MockExamGuideDialogListener {
    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        binding.apply {
            var filteredList = contents

            filteredList = if (schoolType.isHigh) {
                when (categoryTab.selectedIndex) {
                    1 -> filteredList.filter { it.pieceCategoryTag == BookType.BOOK }
                    2 -> filteredList.filter { it.pieceCategoryTag == BookType.CUSTOM_BOOK }
                    3 -> filteredList.filter { it.pieceCategoryTag == BookType.MO }
                    4 -> filteredList.filter { it.pieceCategoryTag == BookType.PRACTICE }
                    5 -> filteredList.filter { it.pieceCategoryTag == BookType.RECOMMEND || it.pieceCategoryTag == BookType.NOTE || it.pieceCategoryTag == BookType.TEST || it.pieceCategoryTag == BookType.TEACHER }
                    else -> filteredList
                }
            } else {
                when (categoryTab.selectedIndex) {
                    1 -> filteredList.filter { it.pieceCategoryTag == BookType.BOOK }
                    2 -> filteredList.filter { it.pieceCategoryTag == BookType.CUSTOM_BOOK }
                    3 -> filteredList.filter { it.pieceCategoryTag == BookType.PRACTICE }
                    4 -> filteredList.filter { it.pieceCategoryTag == BookType.RECOMMEND || it.pieceCategoryTag == BookType.NOTE || it.pieceCategoryTag == BookType.TEST || it.pieceCategoryTag == BookType.TEACHER }
                    else -> filteredList
                }
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


    private fun getStudyList(studentId: String, successCB: (contents: List<Content>) -> Unit) {
        API_V3.getStudyList(studentId).enqueue(object: Callback<ResponseListBody<Content>> {
            override fun onFailure(call: Call<ResponseListBody<Content>>, t: Throwable) {
                responseFailed(this@StudyHistoryActivity, t)
            }

            override fun onResponse(call: Call<ResponseListBody<Content>>, response: Response<ResponseListBody<Content>>) {
                if(response.isSuccessful) {
                    val contents = response.body()?.data ?: emptyList()
                    successCB(contents)
                } else {
                    responseError(this@StudyHistoryActivity, response)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            user?.let {
                getStudyList(it.studentID) {
                    this@StudyHistoryActivity.contents = it
                    if (this@StudyHistoryActivity.contents.isEmpty())
                        emptyGuideTv.visibility = View.VISIBLE
                    else
                        emptyGuideTv.visibility = View.INVISIBLE


                    if (recyclerView.adapter == null) {
                        recyclerView.layoutManager = LinearLayoutManager(
                            this@StudyHistoryActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        recyclerView.adapter = StudyListAdapter()
                    } else {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }

                    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                            super.onScrolled(recyclerView, dx, dy)
//                    Log.d("스크롤", "scrollY=$dy")
                            if (dy > 0) {
                                viewShadow.visibility = View.VISIBLE
                            } else {
                                viewShadow.visibility = View.INVISIBLE
                            }
                        }
                    })
                }
            }
        }
    }

    fun setUpUI() {
        binding.apply {
            backBtn.setOnClickListener { finish() }
            categoryTab.labels = if (schoolType.isHigh) {
                listOf("전체", "유형학습", "워크북", "모의고사", "연습문제", "기타")
            } else {
                listOf("전체", "유형학습", "워크북", "연습문제", "기타")
            }
            ingTab.labels = listOf("전체", "학습 중", "학습 완료")

            categoryTab.listener = this@StudyHistoryActivity
            ingTab.listener = this@StudyHistoryActivity
            initChatBot()
        }


    }

    fun initChatBot() {
        binding.apply {
            chatBotBtn?.setOnClickListener {
                if (chatBotBgCl?.isVisible === true) {
                    chatBotBgCl?.visibleIf(false)
                    chatBotBtn?.startLongClickDescAnim()
                } else {
                    val url = Network.webAppUrl + "/ottway?token=$token&uri=chat-bot"
                    binding.webView?.loadUrl(url)
                    chatBotBgCl?.visibleIf(true)
                    chatBotCv?.visibleIf(true)
                }
            }
            webView?.let {
                it.addJavascriptInterface(ChatBotClientClickEventListener (
                    onCloseListener = {
                        runOnUiThread {
                            chatBotBgCl?.visibleIf(false)
                            chatBotBtn?.startLongClickDescAnim()
                        }
                    }, errorCloseListener = {
                        runOnUiThread {
                            chatBotBgCl?.visibleIf(false)
                        }
                    }
                ), "android")
                it.autoCloseOnChatBotExit {
                    runOnUiThread {
                        chatBotBgCl?.visibleIf(false)
                        chatBotBtn?.startLongClickDescAnim()
                    }
                }

                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
            }

        }

    }

    private fun getContentList(): List<Content> {
        return filteredContents ?: contents
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
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
                if (content.isLocked) {
                    LogUtils.logEvent(this@StudyHistoryActivity, user, PulleyEvent.BUTTON_CLICK, "분석_전체학습내역", "결제유도", "리포트")
                    val dialog = PurchaseGuideDialog.newInstance()
                    supportFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                } else {
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
                            LogUtils.assert(false, "예상치 못한 카테고리 ")
                        }
                    }
                }
            }

            holder.listBinding.solveBtn.setOnClickListener {
                if (content.isLocked) {
                    LogUtils.logEvent(this@StudyHistoryActivity, user, PulleyEvent.BUTTON_CLICK, "분석_전체학습내역", "결제유도", "풀기/리뷰")
                    val dialog = PurchaseGuideDialog.newInstance()
                    supportFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
//                    DialogUtils.confirmDialog(this@StudyHistoryActivity, "[테스트]구독중이 아닙니다.", "열려라 참깨")
                } else {
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
                        BookType.BOOK, BookType.TEACHER -> {
                            val intent = if(content.isCompleted())
//                                SolveActivity.getReviewIntent(this@StudyHistoryActivity, Book(content))
                                SolveActivity.getIntent(this@StudyHistoryActivity, Book(content))
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
                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun getMockWithOptionalSubjects(content: Content, cb: (summary: MockExam) -> Unit) {
        val mock = MockExam(content)
        MockExamManager.getMockSummary(this, content, user!!) { mockExamSummery ->
            mockExamSummery?.let {
                mock.selectOptionalSubjectSummary = mockExamSummery.optionalSubjectSummary.toList()
                mock.examType = mockExamSummery.examType.let {
                    MockExam.ExamType.valueOnString(it)
                }
                mock.grade = mockExamSummery.grade
            }

            cb(mock)
        }
    }
}
