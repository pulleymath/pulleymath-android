package com.freewheelin.pulley.activities.learning.tabFragment.mockExam


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.model.contents.MarkingState
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.ArduousSpinner
import com.freewheelin.pulley.views.ArduousSpinnerListener
import com.freewheelin.pulley.views.DaebakToast
import kotlinx.android.synthetic.main.fragment_new_mock.*
import kotlinx.android.synthetic.main.item_new_test.view.*
import java.util.*

class NewMockFragment : Fragment(), ArduousSpinnerListener, EmailInputDialogListener, MockExamGuideDialogListener {
    var examList: List<MockExam>? = null
    var filteredMockList: List<MockExam>? = null
    var typeTreeSet = TreeSet<MockExam.Type>()
    var gradeTreeSet = TreeSet<Int>()
    var yearTreeSet = TreeSet<Int>()
    var monthTreeSet = TreeSet<Int>()
    var listener: MockTabListener? = null
    lateinit var receiver: BroadcastReceiver
    lateinit var clearReceiver: BroadcastReceiver

    companion object {
        @JvmStatic
        fun newInstance(): NewMockFragment {
            val fragment = NewMockFragment()
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                MockExamManager.getNewMockExamList(context, user!!) {
                    this@NewMockFragment.examList = it
                    var tests = testFilter(examList ?: listOf(), typeFilter)
                    tests = testFilter(ArrayList(tests), gradeFilter)
                    tests = testFilter(ArrayList(tests), yearFilter)
                    tests = testFilter(ArrayList(tests), monthFilter)

                    filteredMockList = ArrayList(tests)
                    mockRv.adapter?.notifyDataSetChanged()
                    setVisibilityEmptyGuide()
                }
            }
        }
        clearReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                this@NewMockFragment.initUI()
                initUI()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_SCORING))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(clearReceiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_CLEAR))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(clearReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_mock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MockExamFragment.REQUEST_MOCK_TEST
                && resultCode == MockExamFragment.RESULT_MOCK_FINISH) {
            listener?.onMockTestFinished()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        val itemName = when(view) {
            typeFilter -> "필터-계열"
            monthFilter -> "필터-월"
            gradeFilter -> "필터-학년"
            else -> "필터-연도"
        }

        val itemValue = view.items[position]

        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", itemName, itemValue)
        var tests = testFilter(examList ?: listOf(), typeFilter)
        tests = testFilter(ArrayList(tests), gradeFilter)
        tests = testFilter(ArrayList(tests), yearFilter)
        tests = testFilter(ArrayList(tests), monthFilter)

        filteredMockList = ArrayList(tests)

        mockRv.adapter?.notifyDataSetChanged()
    }

    private fun setVisibilityEmptyGuide() {
        if (mockRv.adapter?.itemCount == 0) {
            mockRv.visibility = View.INVISIBLE
            emptyGuideContainer.visibility = View.VISIBLE
        } else {
            mockRv.visibility = View.VISIBLE
            emptyGuideContainer.visibility = View.INVISIBLE
        }
    }

    override fun onSentEmail() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "메일보내기")
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(requireContext(), mockExam)
        intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, makeNew)
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(requireContext(), mockExam)
        intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, makeNew)
        Log.d("NewMock", "이어풀기 isRestart=$makeNew")
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    private fun initUI() {
        mockRv.adapter = MockListAdapter()
        mockRv.layoutManager = LinearLayoutManager(context)

        typeFilter.listener = this
        monthFilter.listener = this
        gradeFilter.listener = this
        yearFilter.listener = this

        typeTreeSet = TreeSet(MockExam.Type.list)
        gradeTreeSet = TreeSet(listOf(1, 2, 3))

        val typeStrArrayList = typeTreeSet.map {
            it.getStr()
        }.toMutableList()
        typeStrArrayList.add(0, "계열 전체")
        typeFilter.items = typeStrArrayList

        val gradeStrArrayList = ArrayList(gradeTreeSet.map {
            "고$it"
        })
        gradeStrArrayList.add(0, "학년 전체")
        gradeFilter.items = gradeStrArrayList

        val user = requireActivity().application!!.user!!
        MockExamManager.getNewMockExamList(requireContext(), user) {
            this@NewMockFragment.examList = it
            this@NewMockFragment.filteredMockList = this@NewMockFragment.examList
            mockRv.adapter?.notifyDataSetChanged()

            examList?.let{ list ->
                yearTreeSet = TreeSet(list.groupBy { item -> item.year }.map { item -> item.key }.sorted())
                monthTreeSet = TreeSet(list.groupBy { item -> item.month }.map { item -> item.key }.sorted())

                val monthStrArrayList = monthTreeSet.map { month -> "${month}월" }.toMutableList()
                monthStrArrayList.add(0, "출제월 전체")
                monthFilter.items = monthStrArrayList

                val yearStrArrayList = ArrayList(yearTreeSet.reversed().map { "${it}년" })
                yearStrArrayList.add(0, "출제 연도 전체")
                yearFilter.items = yearStrArrayList
            }
        }

        mockRv.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            if (!mockRv.canScrollVertically(-1)){
                scrollShadow.visibility = View.INVISIBLE
                view2.visibility = View.VISIBLE
            } else{
                scrollShadow.visibility = View.VISIBLE
                view2.visibility = View.INVISIBLE
            }
        }
    }

    private fun testFilter(exams: List<MockExam>, filter: ArduousSpinner): List<MockExam> {
        val position = filter.position
        if (position != null && position > 0) {
            if (filter === typeFilter) {
                return exams.filter {
                    it.type == ArrayList(typeTreeSet).get(position - 1)
                }
            } else if (filter === gradeFilter) {
                return exams.filter {
                    it.grade == ArrayList(gradeTreeSet).get(position - 1)
                }
            } else if (filter === yearFilter) {
                return exams.filter {
                    it.year == ArrayList(yearTreeSet.reversed()).get(position - 1)
                }
            } else {
                return exams.filter {
                    it.month == ArrayList(monthTreeSet).get(position - 1)
                }
            }
        } else {
            return exams
        }
    }

    inner class MockListAdapter : RecyclerView.Adapter<MockListHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockListHolder {
            return MockListHolder(LayoutInflater.from(context).inflate(R.layout.item_new_test, parent, false))
        }

        override fun getItemCount(): Int {
            return (filteredMockList ?: examList)?.size ?: 0
        }

        override fun onBindViewHolder(holder: MockListHolder, position: Int) {
            val tests = filteredMockList ?: examList
            val test = tests!![position]

            if (tests.last() == test)
                holder.horizontalBorder.visibility = View.GONE
            else
                holder.horizontalBorder.visibility = View.VISIBLE

            holder.testBtn.setPermissionClickListener {
                if (holder.testBtn.isEnableUI()) {
                    MockExamGuideDialog(requireContext(), test, false, this@NewMockFragment).show()
                }

            }
            holder.set(test)
        }
    }
}


class MockListHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var typeTv = view.findViewById<TextView>(R.id.typeTv)
    var gradeTv = view.findViewById<TextView>(R.id.gradeTv)
    var yearTv = view.findViewById<TextView>(R.id.yearTv)
    var monthTv = view.findViewById<TextView>(R.id.monthTv)
    var titleTv = view.findViewById<TextView>(R.id.titleTv)
    var testBtn = view.testBtn
    var horizontalBorder = view.findViewById<View>(R.id.horizontalBorder)
    var updateTag = view.findViewById<TextView>(R.id.updateTag)
//    val mailBtn = view.mailBtn

    fun set(exam: MockExam) {
        typeTv.text = exam.type.getStr()
        gradeTv.text = "고${exam.grade}"
        yearTv.text = "${exam.year}년"
        monthTv.text = "${exam.month}월"
        titleTv.text = exam.title

        if (exam.updated)
            updateTag.visibility = View.VISIBLE
        else
            updateTag.visibility = View.GONE

        when (exam.getMakringState()) {
            MarkingState.YET -> {
                testBtn.text = "풀기"
                testBtn.toEnableUI()
            }
            MarkingState.ING -> {
                testBtn.text = "푸는 중"
                testBtn.toProcessingUI()

            }
            MarkingState.COMPLETED -> {
                testBtn.text = "다시 풀기"
                testBtn.toEnableUI()
            }
        }
        if (exam.getMakringState() == MarkingState.ING) {
            typeTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            gradeTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            yearTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            monthTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            titleTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        } else {
            typeTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            gradeTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            yearTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            monthTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            titleTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
        }
    }
}