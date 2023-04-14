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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.databinding.FragmentPatternStudyBinding
import com.freewheelin.pulley.databinding.TooltipAnalysisBinding
import com.freewheelin.pulley.dialogs.*
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.activity.PulleyMathBooksActivity
import com.freewheelin.pulley.revision2023.ui.activity.WorkbookListActivity
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter.OriginType
import com.freewheelin.pulley.revision2023.viewmodel.PatternStudyViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.MarginDecoration
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import com.pulleymath.android.pdf.utils.onThrottleClick

class PatternStudyFragment : LearningTabFragment(),
    PlanListenerV2,
    EmailInputDialogListener {
    private lateinit var binding: FragmentPatternStudyBinding
    private val viewModel: PatternStudyViewModel by viewModels()

    override var screenName = "유형"

    private val myPlanAdapter = PatternStudyMyPlanAdapter (this, listOf(ActionType.pin, ActionType.mail, ActionType.delete), OriginType.MyPlan, isGridLayout = false)
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
                    viewModel.initMyPlanAdapterItem()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel

                initAdapter()
                initGuide()

                pulleyMathBookCv.onThrottleClick { goPulleyMathBooks() }
                commercialBookCv.onThrottleClick { goPdfList() }
                workBookCv.onThrottleClick { goWorkbooks() }
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
                        else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                    }
                }
            }
        }
    }
    private fun showGuestJoinInduceDialog() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "유형학습", "가입유도")
        (activity as? LearningTabActivity)?.showGuestJoinInduceDialog {
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
            viewModel.initMyPlanAdapterItem()
        }
    }

    private fun initGuide() {
        binding.apply {
            myPlanDeleteGuideTv.extensionTouchArea(12.toPx())
            myPlanDeleteGuideTv.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "삭제기준보기")
                val window =
                    BalloonWindow(requireContext(), it, BalloonWindow.Position.below, 16.toPx())
                window.balloonColor = ContextCompat.getColor(requireContext(), R.color.purple_ACACFF)
                window.offset = if (context?.is10InchUI == true) -240 else -190
                window.setPadding(if (context?.is10InchUI == true) 32.toPx() else 24.toPx())
                val tooltipBinding: TooltipAnalysisBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tooltip_analysis, null, false)
                tooltipBinding.chartTopTv.text = "삭제 기준"
                tooltipBinding.chartContentTv.text = viewModel.tooltipText
                window.show(tooltipBinding.root)
            }
        }
    }
    override fun initUI() {

    }

    override fun onResume() {
        super.onResume()
        viewModel.initMyPlanAdapterItem()
    }

    fun goPulleyMathBooks(isFocus: Boolean = false) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "유형학습", "풀리수학문제집")
        PulleyMathBooksActivity.getIntent(requireContext(), isFocus).let {
            getResult.launch(it)
        }
    }
    fun goPdfList() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "유형학습", "풀리북스")
        Intent(requireContext(), PdfListActivity::class.java).let {
            startActivity(it)
        }
    }
    fun goWorkbooks() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "유형학습", "워크북")
        WorkbookListActivity.getIntent(requireContext()).let {
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
            "나의문제집"
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
                    viewModel.initMyPlanAdapterItem()
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "나의문제집빼기")

                viewModel.removeFromMyPlan(book) {
                    viewModel.initMyPlanAdapterItem()
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