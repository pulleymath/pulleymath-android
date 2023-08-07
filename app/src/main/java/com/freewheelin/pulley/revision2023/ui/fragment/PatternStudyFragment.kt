package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.gridlayout.widget.GridLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.is10InchUI
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.databinding.FragmentPatternStudyBinding
import com.freewheelin.pulley.databinding.TooltipAnalysisBinding
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.dialogs.*
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.activity.PulleyMathBooksActivity
import com.freewheelin.pulley.revision2023.ui.activity.WorkbookListActivity
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter.OriginType
import com.freewheelin.pulley.revision2023.viewmodel.PatternStudyViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.MarginDecoration
import com.freewheelin.pulley.legacy.views.balloonWindow.BalloonWindow
import com.freewheelin.pulley.revision2021.utils.observeThrottle
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity
import com.freewheelin.pulley.revision2023.ui.activity.TestActivity
import com.freewheelin.pulley.revision2023.ui.activity.WrongNoteActivity
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.freewheelin.pulley.revision2023.ui.view.StudyMenuCard
import com.pulleymath.android.pdf.utils.onThrottleClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PatternStudyFragment : MainTabFragment(),
    PlanListenerV2,
    EmailInputDialogListener {
    private lateinit var binding: FragmentPatternStudyBinding
    private val viewModel: PatternStudyViewModel by viewModels()

    override var type: MainTab = MainTab.문제풀이
    var isViewCreated = false
    private val myPlanAdapter = PatternStudyMyPlanAdapter (this, listOf(ActionType.pin, ActionType.mail), OriginType.MyPlan, isGridLayout = false)
    private lateinit var getResult: ActivityResultLauncher<Intent>
    lateinit var challengeReceiver: BroadcastReceiver
    lateinit var reConfigureReceiver: BroadcastReceiver

    companion object {
        val PLAN_PINNED = 401

        @JvmStatic
        fun newInstance() =
            PatternStudyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        challengeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val challengeCourseId = it.getIntExtra(ChallengeManager.COURSE_ID, -1)
                    when (challengeCourseId) {
                        ChallengeManager.CourseName.스타트챌린지_유형.id -> {
                            goPulleyMathBooks(true)
                        }
                        ChallengeManager.CourseName.스타트챌린지_북스.id -> {
                            goPdfList()
                        }
                        ChallengeManager.CourseName.스타트챌린지_워크북.id -> {
                            goWorkbooks()
                        }
                        else -> {}
                    }
                }
            }
        }
        reConfigureReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    viewModel.myPlanAdapterItemListener.postValue(Unit)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pattern_study, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(challengeReceiver, IntentFilter(ChallengeManager.PATTERN_STUDY_MOVE_EVENT))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reConfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == PLAN_PINNED) {
                binding.apply {
                    scrollRootView.smoothScrollTo(0, myPlanCl.top)
                }
            }
        }
        isViewCreated = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
                isMobile = requireContext().isMobile

                initAdapter()
                initGuide()

//                pulleyMathBookCv.onThrottleClick { goPulleyMathBooks() }
//                commercialBookCv.onThrottleClick { goPdfList() }
//                workBookCv.onThrottleClick { goWorkbooks() }
//                testCv.onThrottleClick { goTest() }
//                wrongNoteCv.onThrottleClick { goWrongNote() }
//                mockCv.onThrottleClick { goMock() }
            }
            viewModel.apply {
                myPlans.observe(viewLifecycleOwner) {
                    myPlanAdapter.submitList(it.myPieceStorageList)
                    val planCount = "총 ${it.myPieceStorageList.size}개 "
                    val pinText = "핀 설정 ${it.pinBookPlanCount}개 "
                    myPlanCount.postValue(planCount)
                    pinCount.postValue(pinText)
                    showMyPlanEmptyView.postValue(it.myPieceStorageList.isEmpty())
                    showMyPlan.postValue(it.myPieceStorageList.isNotEmpty())
                }
                joinedChallengeList.observe(viewLifecycleOwner) {
                    val isPulleyBooksChallengeInProgress = it.find { it.startChallenge?.isPulleyBooksCourseInProgress == true } != null
                    showPulleyMathChallengeStamp.postValue(isPulleyBooksChallengeInProgress)

                    val isCommercialBooksChallengeInProgress = it.find { it.startChallenge?.isCommercialBooksInProgress == true } != null
                    showCommercialBooksChallengeStamp.postValue(isCommercialBooksChallengeInProgress)

                    val isWorkbooksChallengeInProgress = it.find { it.startChallenge?.isWorkbooksInProgress == true } != null
                    showWorkbooksChallengeStamp.postValue(isWorkbooksChallengeInProgress)

                }
                errorAction.observe(viewLifecycleOwner) { type ->
                    when(type) {
                        CoroutineExceptionType.HttpException403 -> showGuestJoinInduceDialog()
                        CoroutineExceptionType.NONE -> {}
                        else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                    }
                }
                schoolType.observe(viewLifecycleOwner) {
                    initMenuGrid(it)
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(300)
                        myPlanAdapterItemListener.postValue(Unit)
                    }
                }
                myPlanAdapterItemListener.observeThrottle(viewLifecycleOwner) {
                    // viewpager의 onresume이 최초 이후 동작 안할때가 있어서 동작을 확실하게 하기위해
                    // onFragmentSelect와 thottle을통해 병행함.
                    collectAllMyPlans()
                }
            }
        }
    }

    private fun initMenuGrid(schoolType: SchoolType) {
        val menuGl = binding.menuGl
        menuGl.removeAllViews()
        StudyMenuCard.getMenuList(schoolType).forEach {
            val menuCard = StudyMenuCard(requireContext(), getResult)
            menuCard.type = it
            menuCard.setViewModel(viewModel, viewLifecycleOwner)
            menuCard.setParams(schoolType)
            menuGl.addView(menuCard)
        }
    }
    private fun showGuestJoinInduceDialog() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "유형학습", "가입유도")
        (activity as? MainActivity)?.showGuestJoinInduceDialog {
            viewModel.errorStatusReset()
        }
    }
    private fun initAdapter () {
        binding.apply {
            myPlanRv.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = myPlanAdapter
                val itemSpace = resources.getDimension(R.dimen.dp16).toInt()
                val sideItemSpace = resources.getDimension(R.dimen.dp48).toInt()
                addItemDecoration(MarginDecoration(itemSpace, sideItemSpace, sideItemSpace))
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                dragLogEvent()
                            }
                        }
                    }
                })
            }
            viewModel.myPlanAdapter = myPlanAdapter
            viewModel.myPlanAdapterItemListener.postValue(Unit)
        }
    }

    private fun initGuide() {
        binding.apply {
            myPlanDeleteGuideTv.extensionTouchArea(12.toPx())
            myPlanDeleteGuideTv.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "삭제기준보기")
                val window =
                    BalloonWindow(requireContext(), it, BalloonWindow.Position.below, 16.toPx())
                window.balloonColor = ContextCompat.getColor(requireContext(), R.color.purple_200)
                window.offset = if (context?.is10InchUI == true) -240 else -190
                window.setPadding(if (context?.is10InchUI == true) 32.toPx() else 24.toPx())
                val tooltipBinding: TooltipAnalysisBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tooltip_analysis, null, false)
                tooltipBinding.chartTopTv.text = "삭제 기준"
                tooltipBinding.chartContentTv.text = viewModel.tooltipText
                window.show(tooltipBinding.root)
            }
        }
    }

    override fun onFragmentSelected() {
        if (isViewCreated) {
            viewModel.myPlanAdapterItemListener.postValue(Unit)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.myPlanAdapterItemListener.postValue(Unit)
    }

    fun goPulleyMathBooks(isFocus: Boolean = false) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "풀리수학문제집")
        PulleyMathBooksActivity.getIntent(requireContext(), isFocus).let {
            getResult.launch(it)
        }
    }
    fun goPdfList() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "풀리북스")
        Intent(requireContext(), PdfListActivity::class.java).let {
            startActivity(it)
        }
    }
    fun goWorkbooks() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "워크북")
        WorkbookListActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }
    fun goTest() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "테스트")
        TestActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }
    fun goMock() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "테스트")
        MockListActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }
    fun goWrongNote() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "문제풀이", "테스트")
        WrongNoteActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }

    override fun onSentEmail() {
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }
    private fun dragLogEvent() {
        LogUtils.logEvent(
            requireContext(),
            user!!,
            PulleyEvent.BUTTON_CLICK,
            "유형학습",
            "스와이프",
            "최근문제집"
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(challengeReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reConfigureReceiver)
    }

    override fun onActionBtnClicked(action: ActionType, book: Book) {
        when (action) {
            ActionType.mail -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
                val dialog = EmailInputDialog(requireContext(), listOf(book), user!!, this)
                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.isPinned) "핀해제하기" else "핀설정하기"
                val itemValue = "문제집"
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", itemName, itemValue)

                val id = if(book.assignID == null) book.pieceID else book.assignID!!
                viewModel.togglePin(id, !book.isPinned) {
                    viewModel.myPlanAdapterItemListener.postValue(Unit)
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "최근문제집빼기")

                viewModel.removeFromMyPlan(book) {
                    viewModel.myPlanAdapterItemListener.postValue(Unit)
                }
            }
        }
    }
    override fun onSolveClicked(book: Book) {
        val intent = SolveActivity.getIntent(requireContext(), book)
        startActivity(intent)
    }

    override fun filterFromTagOnCard(filterType: String) {
        // blank
    }
}