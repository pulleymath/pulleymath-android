package com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis

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
import com.freewheelin.pulley.activities.MockReportActivity
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.activities.WrongTestReportActivity
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.component.StudyListViewHolder
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.setPermissionClickListener
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import kotlinx.android.synthetic.main.activity_study_history.*
import kotlinx.android.synthetic.main.view_analysis_today_study_list.view.*

class UserHistoryActivity : AppCompatActivity(), DabakTabRadioListener, MockExamGuideDialogListener {
    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        var filteredList = contents

        filteredList = when(categoryTab.selectedIndex) {
            1 -> filteredList.filter { it.pieceCategoryTag == BookType.TEST }
            2 -> filteredList.filter { it.pieceCategoryTag == BookType.BOOK || it.pieceCategoryTag == BookType.CUSTOM_BOOK}
            3 -> filteredList.filter { it.pieceCategoryTag == BookType.MO }
            4 -> filteredList.filter { it.pieceCategoryTag == BookType.NOTE }
            5 -> filteredList.filter { it.pieceCategoryTag == BookType.RECOMMEND }
            else -> filteredList
        }

        filteredList = when(ingTab.selectedIndex) {
            1 -> filteredList.filter { !it.isCompleted() }
            2 -> filteredList.filter { it.isCompleted() }
            else -> filteredList
        }

        this.filteredContents = filteredList
        recyclerView.adapter?.notifyDataSetChanged()

        if(getContentList().isEmpty()) {
            emptyGuideTv.visibility = View.VISIBLE
        } else {
            emptyGuideTv.visibility = View.INVISIBLE
        }
    }

    lateinit var user: User

    var contents: List<Content> = emptyList()
    var filteredContents: List<Content>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_history)

        val studentID = intent.getStringExtra(UserAnalysisActivity.KEY_STUDENT_ID)?:"none"
        val name = intent.getStringExtra(UserAnalysisActivity.KEY_STUDENT_NAME)?:"none"

        user = User()
        user.studentID = studentID
        user.fullName = name

        setUpUI()
    }

    override fun onResume() {
        super.onResume()
        user?.getStudyList(this) {
            this.contents = it
            if(this.contents.isEmpty())
                emptyGuideTv.visibility = View.VISIBLE
            else
                emptyGuideTv.visibility = View.INVISIBLE


            if(recyclerView.adapter == null) {
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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

    fun setUpUI() {
        backBtn.setOnClickListener { finish() }
        categoryTab.labels = listOf("전체", "테스트", "유형학습", "모의고사", "오답학습", "추천학습")
        ingTab.labels = listOf("전체", "학습 중", "학습 완료")

        categoryTab.listener = this
        ingTab.listener = this

//        problemCntContainer.setOnClickListener {
//            val balloonWindow = BalloonWindow(this, questionIv, BalloonWindow.Position.below, 8.toPx())
//            balloonWindow.balloonColor = ContextCompat.getColor(this, R.color.purple_ACACFF)
//            val textView = TextView(this)
//            textView.text = "푼 문제 수(+오답학습 수)"
//            textView.typeface = Theme.bold(this)
//            textView.setTextColor(ContextCompat.getColor(this, R.color.white_ffffff))
//            balloonWindow.show(textView)
//        }


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


            // TODO 이부분 기획이 안되어있고 리포트나 솔브나 토큰 권한문제로 에러가 나기때문에
            // 제대로하려면 기획 + 서버 api 와의 협의가 필요함
            holder.listBinding.reportBtn.visibility = View.INVISIBLE
            holder.listBinding.solveBtn.visibility = View.INVISIBLE
        }
    }
}
