package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.component.*
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.marketing.MarketingManager
import com.freewheelin.pulley.revision2023.viewmodel.MainFViewModel
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentMain2Binding
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeMissionAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeHeaderListAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.StartChallengeInfoDialog
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity.Companion.FROM_MAIN_TAB
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity.Companion.FROM_PULLEYMATH_BOOKS
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity.Companion.FROM_CONCEPT_TAB
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity.Companion.FROM_MAIN_TAB
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity.Companion.WHERE_ARE_YOU_FROM
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanTag
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.PlannerActivity
import com.freewheelin.pulley.revision2023.ui.activity.PlannerActivity.Companion.PLANNER_MONDAY
import com.freewheelin.pulley.revision2023.ui.activity.PlannerActivity.Companion.PLANNER_SUNDAY
import com.freewheelin.pulley.revision2023.ui.adapter.MainPlannerListAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeInduceDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import org.joda.time.LocalDate

class MainFragment : MainTabFragment(), DDaySettingDialogListener, LifecycleObserver,
    LifecycleEventObserver {

    override var type: MainTab = MainTab.메인
    lateinit var binding: FragmentMain2Binding

    lateinit var profileReceiver: BroadcastReceiver
    lateinit var userUpdateReceiver: BroadcastReceiver
    lateinit var reconfigureReceiver: BroadcastReceiver

    var wasInitUI = false
    private var isViewCreated = false
    val viewModel: MainFViewModel by viewModels()
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private val challengeHeaderListAdapter = ChallengeHeaderListAdapter { item ->
        viewModel.onChallengeHeaderClick(item)
    }
    private val challengeMissionAdapter = ChallengeMissionAdapter {
        viewModel.onMissionClick(it)
    }
    private val plannerAdapter = MainPlannerListAdapter {
        // TODO
        viewModel.selectedPlan.postValue(it)
    }

    companion object {
        const val PLANNER_RESULT = 205
        const val SOLVE_RESULT = 206
        const val OPEN_PULLEY_WORKBOOK = "OPEN_PULLEY_WORKBOOK"
        const val OPEN_PRACTICE = "OPEN_PRACTICE"
        const val OPEN_CONCEPT = "OPEN_CONCEPT"
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isViewCreated = true
        initActivityResult()
        profileReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncProfile()
            }
        }
        userUpdateReceiver = object :BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncProfile()
            }
        }
        reconfigureReceiver = object :BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncProfile()
            }
        }
    }
    fun initChallenge() {
        viewModel.apply {
            cancelChallengeHeaderJob()
            initChallenge()
        }
    }

    override fun onResume() {
        super.onResume()
        showMarketingBanner()
    }

    private fun showMarketingBanner() {
        val isGuestUser = user?.serviceType?.isGuestUser == true
        val isAppFirstLaunch = MyApplication.isAppFirstLaunch

        if (isGuestUser && isAppFirstLaunch) {
            MarketingManager.setMarketingBanner(requireContext())
            return
        }

        val scInfo = Preferences.startChallengeAlreadyAppeared
        val appearedIds = scInfo.studentIds

        val isAlreadyStartChallengeAppeared = appearedIds.contains(user?.studentID)
        if (isAlreadyStartChallengeAppeared && isAppFirstLaunch) {
            MarketingManager.setMarketingBanner(requireContext())
            return
        }

        viewModel.isStartChallengeUserStatusNotYet { isNotYet ->
            if (isNotYet && isAppFirstLaunch) {
                MarketingManager.setMarketingBanner(requireContext())
            }
        }
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
//        syncProfile()
//        if (challengeHeaderListAdapter.currentList.size > 0) {
//            val headerItem = challengeHeaderListAdapter.currentList[0]
//            viewModel.onChallengeHeaderClick(headerItem)
//        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(profileReceiver, IntentFilter(UserManager.EVENT_USER_MODIFYING))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(userUpdateReceiver, IntentFilter(UserManager.EVENT_USER_UPDATE))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reconfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
        viewModel.showWholeProgressBar.postValue(true)
        init()

//        (activity as LearningTabActivity).setUserObserveAttachedByMainFragment()
    }

    fun init() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
//            viewModel.initUserInfo()
            initRv()
            viewModel.updateMainUserPlanner()

            dDayTv.setOnClickListener { onDDayBtnClicked() }
            dDayLl.setOnClickListener { onDDayBtnClicked() }
            startStudyClBtn.setOnClickListener { onStartBtnClicked() }
            startStudyClBtn2.setOnClickListener { onStartBtnClicked() }
            challengeActionBtn.setOnClickListener { onChallengeAction() }
            plannerPrevNaviBtn.setOnClickListener {
                viewModel.movePrevPlannerWeek()
            }
            plannerNextNaviBtn.setOnClickListener {
                viewModel.moveNextPlannerWeek()
            }
            dateRangeCl.setOnClickListener {
                (plannerRv.layoutManager as? LinearLayoutManager)?.let {lm ->
                    val plan = plannerAdapter.currentList.find { it.isToday }
                    val index = plannerAdapter.currentList.indexOf(plan)
                    lm.scrollToPositionWithOffset(index, 24)
                }
            }
            todayBtn.setOnClickListener {
                (plannerRv.layoutManager as? LinearLayoutManager)?.let {lm ->
                    val plan = plannerAdapter.currentList.find { it.isToday }
                    val index = plannerAdapter.currentList.indexOf(plan)
                    if (index == -1) {
                        viewModel.updateMainUserPlanner()
                    } else {
                        lm.scrollToPositionWithOffset(index, 24)
                    }
                }
            }
            makePlanLl.setOnClickListener {
                val intent = Intent(requireContext(), PlannerActivity::class.java).apply {
                    val monday = LocalDate(viewModel.selectedMondayOfTheWeek.value).toString("yyyy-MM-dd")
                    val sunday = LocalDate(viewModel.selectedSundayOfTheWeek.value).toString("yyyy-MM-dd")
                    putExtra(PLANNER_MONDAY, monday)
                    putExtra(PLANNER_SUNDAY, sunday)
                }
                getResult.launch(intent)
            }
        }
        viewModel.apply {
            selectedPlan.observe(viewLifecycleOwner) {
                when (it.tag) {
                    WeeklyPlanTag.PULLEY_WORKBOOK -> {
                        val intent = Intent(requireContext(), SolveActivity::class.java).apply {
                            putExtra(OPEN_PULLEY_WORKBOOK, it.workbookId)
                            putExtra(WHERE_ARE_YOU_FROM, SolveActivity.FROM_MAIN_TAB)
                        }
                        getResult.launch(intent)
                    }
                    WeeklyPlanTag.PRACTICE -> {
                        // TODO 아직 없어서 테스트 못함 230802
                    }
                    WeeklyPlanTag.CONCEPT -> {
                        viewModel.createLearningCourseOnStudentId(it.workbookId) {
                            val chapterId = it.workbookId
                            val name = it.title
                            val intent = LearningCourseActivity.getIntent(requireContext(), chapterId, name)
                            intent.putExtra(WHERE_ARE_YOU_FROM, LearningCourseActivity.FROM_MAIN_TAB)
                            getResult.launch(intent)
                        }
                    }
                    else -> {}
                }
            }
            challengeHeaders.observe(viewLifecycleOwner) {
                challengeHeaderListAdapter.submitList(it)
                if (it.isNotEmpty() && !wasInitUI) {
                    wasInitUI = true
                    viewModel.initChallengeSetting()
                }
            }
            challengeMission.observe(viewLifecycleOwner) { list ->
                binding.missionRv.apply {
                    if (list.isEmpty()) return@observe
                    list.first().parentDetailItem?.courses?.size?.let { spanCount ->
                        layoutManager = GridLayoutManager(requireContext(), spanCount, LinearLayoutManager.VERTICAL, false).also {
                            it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (position) {
                                        0 -> spanCount
                                        else -> 1
                                    }
                                }
                            }
                        }
                    }
                }
                challengeMissionAdapter.submitList(list)

            }

            mainUserPlannerItems.observe(requireActivity()) { list ->
                plannerAdapter.submitList(list)
            }
            plannerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
                ) {
                    (binding.plannerRv.layoutManager as? LinearLayoutManager)?.let { lm ->
                        val plan = plannerAdapter.currentList.find { it.isToday }
                        val index = plannerAdapter.currentList.indexOf(plan)
                        val marginTop = 24
                        lm.scrollToPositionWithOffset(index, marginTop)
                    }
                }
            })
            toastMessage.observe(viewLifecycleOwner) {
                DaebakToast.show(requireContext(), it)
            }
            mainProfile.observe(viewLifecycleOwner) { _ ->
                binding.apply {
                    showWholeProgressBar.postValue(false)
                }
            }

            currentMission.observe(viewLifecycleOwner) {
                println("currentMission update 1")
                if (!it.isStartChallenge) return@observe
                println("currentMission update 2 게스트입니까?=${user?.serviceType?.isGuestUser}")
                blurTitle.postValue("${user?.fullName}님 ${it.challengeName}에 참여해\n${it.reward?.name}을 받아보세요!")
                if (user?.serviceType?.isGuestUser == true) return@observe
                println("currentMission update 3")
                val scInfo = Preferences.startChallengeAlreadyAppeared
                val appearedIds = scInfo.studentIds
                println("currentMission update 4 ${appearedIds}, studentId : ${user?.studentID}")
                val isAlreadyAppearedUser = appearedIds.contains(user?.studentID)
                println("currentMission update 4-최초 접속시 startChallenge 권유가 이미 동작하였습니까?=${isAlreadyAppearedUser}")

                if (isAlreadyAppearedUser) return@observe
                println("currentMission update 5 it.userStatus : ${it.userStatus}")
                if (it.userStatus == ChallengeUserStatus.YET) {
                    // 강제참여
                    this@MainFragment.joinChallenge(it.challengeId) {
                        println("currentMission update 6 after join")
                        val studentId = user?.studentID ?: ""
                        val newList = scInfo.studentIds + listOf(studentId)
                        scInfo.studentIds = newList.toSet().toList()
                        Preferences.startChallengeAlreadyAppeared = scInfo
                    }
                }
            }
            joinedChallengeList.observe(viewLifecycleOwner) {
                val challenge = it.find {
                        it.challengeId == currentMission.value?.challengeId
                    } ?: return@observe
                collectChallengeDetail(challenge.challengeId)
            }
            isLoading.observe(viewLifecycleOwner) { loading ->
                binding.apply {
                    if (loading) {
                        challengeLoadingContainer.visibleIf(true)
                        loadingLottie.playAnimation()
                    } else {
                        challengeLoadingContainer.hide(300)
                    }
                }
            }
            errorAction.observe(viewLifecycleOwner) { type ->
                when(type) {
                    CoroutineExceptionType.HttpException403 -> showGuestJoinInduceDialog()
                    CoroutineExceptionType.NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
            userInRepo.observe(viewLifecycleOwner) {
                showPaidView.postValue(it?.serviceType?.isPaidUser)
                userPaidServiceType.postValue(it?.serviceType)
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    private fun initRv() {
        binding.apply {
            challengeHeaderListRv.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = challengeHeaderListAdapter
                viewModel.challengeListAdapter = challengeHeaderListAdapter
            }
            missionRv.apply {
                adapter = challengeMissionAdapter
                viewModel.challengeDescAdapter = challengeMissionAdapter
            }
            plannerRv.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = plannerAdapter
                viewModel.plannerAdapter = plannerAdapter
            }

            initChallenge()
        }
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            viewModel.fetchUserProfile()
        }
    }

    fun syncProfile() {
        Log.d("마케팅", "syncProfile() is called!!!")
        viewModel.fetchUserProfile()
    }

    private fun onDDayBtnClicked() {
//        val spyCount = (activity as LearningTabActivity).spyCount
        val setOnSpyMode = { (activity as MainActivity).setOnSpyMode() }
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "디데이꺽쇠")
        val dialog = DDaySettingDialog(requireContext(), setOnSpyMode)
        dialog.listener = this
        dialog.show()

//        val dialog = ChallengeGuideManager.getStartGuideMission1(
//            nextEvent = {
//                (activity as LearningTabActivity).setSelectedTab(1)
//                (activity as LearningTabActivity).setConceptCourseSubjectId(LCSubject.SubjectIndicator.Tutorial.rawValue)
//                CoroutineScope(Dispatchers.Main).launch {
//                    delay(700)
//                    (activity as LearningTabActivity).launchConceptCourseTutorial()
//                }
//            }
//        )
//        childFragmentManager.let { dialog.show(it, "StartGuide") }

//        val finishGuideDialog = ChallengeGuideManager
//            .getFinishGuideFromMission1(nextEvent = {  })
//        childFragmentManager.let { finishGuideDialog.show(it, "finishGuideDialog") }
    }

    private fun showGuestJoinInduceDialog() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "메인", "가입유도")
        (activity as? MainActivity)?.showGuestJoinInduceDialog {
            viewModel.errorStatusReset()
        }
    }
    private fun onStartBtnClicked() {
        if (user?.serviceType?.isGuestUser == true) {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "가입유도","풀리수학으로공부시작")
            showGuestJoinInduceDialog()
        } else {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "결제유도","풀리수학으로공부시작")
            FacebookEvent.log(requireContext(), FacebookEvent.SUBSCRIBE_STARTED)
    //        IntentUtils.openWebLink(requireContext(), URL.구매촉구_메인, requireContext().packageManager)
            val dialog = PurchaseGuideDialog.newInstance(2)
            childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
        }
    }

    // TODO 마이페이지로 옮겨질것임
    private fun onChallengeAction() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "챌린지액션")
        viewModel.apply {
            currentMission.value?.let {
                when (it.userStatus) {
                    ChallengeUserStatus.YET -> joinChallenge(it.challengeId)
                    ChallengeUserStatus.DONE -> {
                        if (it.remainRewardsCount != 0) {
                            askForRedeemOfChallenge(it)
                        } else {
                            DaebakToast.show(requireContext(), "쿠폰 발급이 완료됐어요! 마이페이지에서 쿠폰함을 확인하세요 :)")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun joinChallenge(challengeId: Int, cb: () -> Unit = {}) {
        val nextEvent = {
            (activity as MainActivity).tabMove(1)
            (activity as MainActivity).setConceptCourseSubjectId(0)
            (activity as MainActivity).launchConceptCourseTutorial()
        }
        viewModel.joinChallengeById(challengeId) {
            val dialog = ChallengeGuideManager.getStartGuideMission1(
                nextEvent = nextEvent,
                exitEvent = {
                    val induceDialog = ChallengeInduceDialog.newInstance(
                        ChallengeInduceDialog.Type.Disappointed
                    )
                    induceDialog.nextEvent = nextEvent
                    induceDialog.exitEvent = {
                        MarketingManager.setMarketingBanner(requireContext())
                    }

                    childFragmentManager.let { induceDialog.show(it, "challengeInduceDialog") }
                }
            )
            childFragmentManager.let { dialog.show(it, "StartGuide") }
            cb()
        }
    }
    fun showStartChallengeCompletedGuide() {
        val dialog = ChallengeGuideManager.getFinishGuideFromAllMission()
        childFragmentManager.beginTransaction().add(dialog, "getFinishGuideFromAllMission").commitAllowingStateLoss()

    }
    private fun askForRedeemOfChallenge(challenge: Challenge) {
        if (challenge.userChallengeId == null) return responseFailed(requireContext(), Throwable("userChallengeId cannot Null"))
        viewModel.askForRedeemOfChallenge(challenge.userChallengeId) {
            if (user?.serviceType?.isNoneUser == true) {
                val dialog = StartChallengeInfoDialog.newInstance(challenge.challengeId, true)
                dialog.startCallback = { _ ->
                    val pgDialog = PurchaseGuideDialog.newInstance(2)
                    childFragmentManager.let { pgDialog.show(it, "purchaseGuideDialog") }
                }
                childFragmentManager.let { dialog.show(it, "StartChallengeEndInfoDialog") }
            } else {
                DaebakToast.show(requireContext(), "쿠폰 발급이 완료됐어요! 마이페이지에서 쿠폰함을 확인하세요 :)")
            }
        }
    }
    override fun onOnDDaySettingCompleted() {
        syncProfile()
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(userUpdateReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reconfigureReceiver)
    }
    fun firstHeaderMove() {
        if (isViewCreated) {
            viewModel.firstHeaderDetailForceMove()
        }
    }
    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val fromMainTabCode = LearningCourseActivity.FROM_MAIN_TAB
            when (it.resultCode) {
                PLANNER_RESULT -> {
                    val mondayStr = it.data?.getStringExtra("monday")
                    val sundayStr = it.data?.getStringExtra("sunday")
                    viewModel.updateMainUserPlanner(mondayStr, sundayStr)
                }
                SOLVE_RESULT, fromMainTabCode -> {
                    val monday = viewModel.selectedMondayOfTheWeek.value?.toString("yyyy-MM-dd")
                    val sunday = viewModel.selectedSundayOfTheWeek.value?.toString("yyyy-MM-dd")
                    viewModel.updateMainUserPlanner(monday, sunday)
                }
                else -> {}
            }
        }
    }
}
