package com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.OMRActivity
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.FragmentNewMockBinding
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.legacy.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.legacy.model.contents.MarkingState
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.MockFViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity
import com.freewheelin.pulley.revision2023.ui.activity.MockTabListener
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewMockFragment : Fragment(), EmailInputDialogListener, MockExamGuideDialogListener {

//    var typeTreeSet = TreeSet<MockExam.Type>()
//    var gradeTreeSet = TreeSet<Int>()
//    var yearTreeSet = TreeSet<Int>()
//    var monthTreeSet = TreeSet<Int>()
    var listener: MockTabListener? = null
    lateinit var receiver: BroadcastReceiver
    lateinit var clearReceiver: BroadcastReceiver
    lateinit var reconfigureReceiver: BroadcastReceiver

    val viewModel: MockFViewModel by viewModels()

    private val mockAdapter = MockListAdapter()
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
                binding.loadingContainer.visibleIf(true)
                MockExamManager.getNewMockExamList(context, user!!) {
                    viewModel.mockOrgList.value = it
                    viewModel.filter()

//                        binding.mockRv.adapter?.notifyDataSetChanged()
                        viewModel.newMockFragmentProgressHidePending = true
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.loadingContainer.hide(300)
                        }
                        setVisibilityEmptyGuide()
//                    }
                }
            }
        }
        clearReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                initUI()
            }
        }
        reconfigureReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                mockAdapter.notifyDataSetChanged()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_SCORING))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(clearReceiver, IntentFilter(MockExamManager.EVENT_MOCK_EXAM_CLEAR))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reconfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
    }
    fun setHidePending() {
        if (::viewModel.isLateinit) {
            if (viewModel.newMockFragmentProgressHidePending) {
                binding.loadingContainer.hide(300)
                viewModel.newMockFragmentProgressHidePending = false
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(clearReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reconfigureReceiver)
        super.onDestroy()
    }
    lateinit var binding: FragmentNewMockBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_mock, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        viewModel.apply {
            userInRepo.observe(viewLifecycleOwner) {
//                binding.mockRv.adapter?.notifyDataSetChanged()
            }
            yearSelectedPosition.observe(viewLifecycleOwner) { filter() }
            monthSelectedPosition.observe(viewLifecycleOwner) { filter() }
            gradeSelectedPosition.observe(viewLifecycleOwner) { filter() }
            typeSelectedPosition.observe(viewLifecycleOwner) { filter() }

            filteredMockList.observe(viewLifecycleOwner) {
                mockAdapter.submitList(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MockListActivity.RESULT_MOCK_FINISH
                && resultCode == MockListActivity.RESULT_MOCK_FINISH) {
            listener?.onMockTestFinished()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setVisibilityEmptyGuide() {
        with(binding) {
            if (mockRv.adapter?.itemCount == 0) {
                mockRv.visibility = View.INVISIBLE
                emptyGuideContainer.visibility = View.VISIBLE
            } else {
                mockRv.visibility = View.VISIBLE
                emptyGuideContainer.visibility = View.INVISIBLE
            }
        }
    }

    override fun onSentEmail() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "메일보내기")
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(requireContext(), mockExam)
        intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, makeNew)
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(requireContext(), mockExam)
        intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, makeNew)
        Log.d("NewMock", "이어풀기 isRestart=$makeNew")
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
    }
    var isShowFilter = true
    fun expandFilterLl() {
//        isShowFilter = true
        binding.filterLl.showExpandVertical(true)
    }
    fun getFilterLlHeight(): Int {
        return binding.filterLl.height
    }

    private fun makeFilterSumUpText() {
        binding.apply {

        }
    }
    private fun initUI() {
        with(binding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            mockRv.adapter = mockAdapter
            mockRv.layoutManager = LinearLayoutManager(context)
            mockRv.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val scrollY = mockRv.computeVerticalScrollOffset()

                    if (scrollY > 180 && isShowFilter) {
                        isShowFilter = false
                        showSumUpText = true
                        filterLl.showExpandVertical(isShowFilter)
                        if (requireContext().isMobile) {
                            (activity as? MockListActivity)?.expandHeader(isShowFilter)
                        }
                    } else if (scrollY < 10 && !isShowFilter) {
                        isShowFilter = true
                        showSumUpText = false
                        filterLl.showExpandVertical(isShowFilter)
                        if (requireContext().isMobile) {
                            (activity as? MockListActivity)?.expandHeader(isShowFilter)
                        }
                    }
                }
            })

            val typeList = listOf("계열 전체") + MockExam.Type.list.map { it.getStr() }.toList()
            typeSpinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner_textview, typeList)
            typeList.forEach { viewModel.type.put(it, it) }

            val gradeList = listOf("학년 전체", "고1", "고2", "고3")
            gradeSpinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner_textview, gradeList)
            gradeList.forEach { viewModel.grade.put(it, it) }

            binding.loadingContainer.visibleIf(true)
            user?.let { u ->
                MockExamManager.getNewMockExamList(requireContext(), u) { list ->
                    viewModel.mockOrgList.postValue(list)
                    viewModel.filteredMockList.postValue(list)
                    mockRv.adapter?.notifyDataSetChanged()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        binding.loadingContainer.hide()
                    }


                    val yearSorted = list.groupBy { item -> item.year }.map { item -> item.key }.sorted()
                    val yearList = listOf("출제 연도 전체") + yearSorted.reversed().map { "${it}년"}
                    activity?.applicationContext?.let {
                        yearSpinnerAdapter = ArrayAdapter<String>(it, R.layout.item_spinner_textview, yearList)
                        yearList.forEach { viewModel.year.put(it, it) }

                        val monthSorted = list.groupBy { item -> item.month }.map { item -> item.key }.sorted()
                        val monthList = listOf("출제월 전체") + monthSorted.map { "${it}월"}
                        monthSpinnerAdapter = ArrayAdapter<String>(it, R.layout.item_spinner_textview, monthList)
                        monthList.forEach { viewModel.month.put(it, it) }
                    }

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
    }

    inner class MockListAdapter() : ListAdapter<MockExam, MockListHolder>(DiffCallback<MockExam>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockListHolder {
            return MockListHolder(LayoutInflater.from(context).inflate(R.layout.item_new_test, parent, false))
        }

        override fun onBindViewHolder(holder: MockListHolder, position: Int) {
            val exam = getItem(position)
            holder.set(exam)

            if (exam.isTwins) {
                holder.testBtn.setOnPaidUserClickListener(
                    cb = { MockExamGuideDialog(requireContext(), exam, false, this@NewMockFragment).show() },
                    deniedCb = {
                        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "새모의고사", "풀기-쌍둥이")
                        if (user?.serviceType?.isGuestUser == true) {
                            LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "새모의고사", "가입유도", "풀기-쌍둥이")
                            val dialog = JoinInduceForGuestDialog().apply {
                                updateDismissCallback {
                                    viewModel.errorStatusReset()
                                }
                            }
                            childFragmentManager.let { dialog.show(it, "joinInduceDialog") }
                        } else {
                            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "새모의고사", "결제유도", "풀기 쌍둥이")
                            val dialog = PurchaseGuideDialog.newInstance()
                            childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                        }
                    }
                )
                holder.testInProgressBtn.setOnPaidUserClickListener(
                    cb = { MockExamGuideDialog(requireContext(), exam, false, this@NewMockFragment).show() },
                    deniedCb = {
                        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "새모의고사", "결제유도", "푸는중 쌍둥이")
                        val dialog = PurchaseGuideDialog.newInstance()
                        childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                    }
                )
            } else {
                holder.testBtn.setOnClickListener {
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "새모의고사", "풀기")
                    if (user?.serviceType?.isGuestUser == true) {
                        LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "새모의고사", "가입유도")
                        val dialog = JoinInduceForGuestDialog().apply {
                            updateDismissCallback {
                                viewModel.errorStatusReset()
                            }
                        }
                        childFragmentManager.let { dialog.show(it, "joinInduceDialog") }
                    } else {
                        MockExamGuideDialog(requireContext(), exam, false, this@NewMockFragment).show()
                    }
                }
                holder.testInProgressBtn.setOnClickListener {
                    MockExamGuideDialog(requireContext(), exam, false, this@NewMockFragment).show()
                }
            }

            if (currentList.last() == exam)
                holder.setLastHolderUI()
            else
                holder.setMidHolderUI()
        }
    }
}


class MockListHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var typeTv: TextView = view.findViewById(R.id.typeTv)
    var gradeTv: TextView = view.findViewById(R.id.gradeTv)
    private var yearTv: TextView = view.findViewById(R.id.yearTv)
    var monthTv: TextView = view.findViewById(R.id.monthTv)
    var titleTv: TextView = view.findViewById(R.id.titleTv)
    var testBtn: CommonButton = view.findViewById(R.id.testBtn)
    var testInProgressBtn: CommonButton = view.findViewById(R.id.testInProgressBtn)
    var horizontalBorder: View = view.findViewById(R.id.horizontalBorder)
    var updateTag: TextView = view.findViewById(R.id.updateTag)
    var outContainer: View = view.findViewById(R.id.outContainer)

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

        setNormalButton()

        when (exam.getMakringState()) {
            MarkingState.YET -> {
                testBtn.visibility = View.VISIBLE
                testInProgressBtn.visibility = View.GONE
                testBtn.text = "풀기"
//                testBtn.isEnabled = true
                setLockIv(exam)
            }
            MarkingState.ING -> {
                testBtn.visibility = View.GONE
                testInProgressBtn.visibility = View.VISIBLE
                testInProgressBtn.text = "푸는 중"
//                testBtn.toProcessingUI()

            }
            MarkingState.COMPLETED -> {
                testBtn.visibility = View.VISIBLE
                testInProgressBtn.visibility = View.GONE
                testBtn.text = "다시 풀기"

//                testBtn.isEnabled = true
            }
            else -> {}
        }

        if (exam.getMakringState() == MarkingState.ING) {
            typeTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_500))
            gradeTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_500))
            yearTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_500))
            monthTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_500))
            titleTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_500))
        } else {
            typeTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_800))
            gradeTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_800))
            yearTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_800))
            monthTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_800))
            titleTv.setTextColor(ContextCompat.getColor(view.context, R.color.gray_800))
        }
    }

    fun setLockIv(exam: MockExam) {
        if (user?.serviceType?.isGuestUser == true) {
            testBtn.showStartIcon(false)
            return
        }
        val isPaidUser = user?.serviceType?.isPaidUser == true
        testBtn.showStartIcon(!isPaidUser && exam.isTwins)
    }

    private fun setNormalButton() {
        testBtn.showStartIcon(false)

    }

    fun setMidHolderUI() {
        horizontalBorder.visibility = View.VISIBLE
        outContainer.layoutParams.apply {
            height = view.context.resources.getDimension(R.dimen.dp64).toInt()
        }
        outContainer.background =
            ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_middle)
    }

    fun setLastHolderUI() {
        horizontalBorder.visibility = View.GONE
        outContainer.layoutParams.apply {
            height = 112.toPx()
        }
        outContainer.background =
            ContextCompat.getDrawable(view.context, R.drawable.bg_shadow_bottom)
    }
}