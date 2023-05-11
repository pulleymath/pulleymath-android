package com.freewheelin.pulley.revision2023.ui.fragment


import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.*
import com.freewheelin.pulley.activities.learning.tabFragment.main.marketing.MarketingManager
import com.freewheelin.pulley.revision2023.viewmodel.MainFViewModel
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentMain2Binding
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeMissionAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeHeaderListAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.StartChallengeInfoDialog
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseGuideActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeInduceDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : LearningTabFragment(), DDaySettingDialogListener, LifecycleObserver,
    LifecycleEventObserver {

    override var screenName: String = "메인"
    lateinit var binding: FragmentMain2Binding

    lateinit var profileReceiver: BroadcastReceiver
    lateinit var userUpdateReceiver: BroadcastReceiver

    val viewModel: MainFViewModel by viewModels()
    private val challengeHeaderListAdapter = ChallengeHeaderListAdapter { item ->
        viewModel.onChallengeHeaderClick(item)
    }
    private val challengeMissionAdapter = ChallengeMissionAdapter {
        viewModel.onMissionClick(it)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncProfile()
            }
        }
        userUpdateReceiver = object :BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                wasInitUI = false
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
        if (!::binding.isInitialized) return
        syncProfile()
        if (challengeHeaderListAdapter.currentList.size > 0) {
            val headerItem = challengeHeaderListAdapter.currentList[0]
            viewModel.onChallengeHeaderClick(headerItem)
        }
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
        viewModel.showWholeProgressBar.postValue(true)
        init()

//        (activity as LearningTabActivity).setUserObserveAttachedByMainFragment()
    }

    fun init() {
        if (!::binding.isInitialized) return
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
//            viewModel.initUserInfo()
            initRv()

            dDayTv.setOnClickListener { onDDayBtnClicked() }
            startStudyClBtn.setOnClickListener { onStartBtnClicked() }
            challengeActionBtn.setOnClickListener { onChallengeAction() }
        }
        viewModel.apply {
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
                println("currentMission update 2 ${user?.serviceType?.isGuestUser}")
                blurTitle.postValue("${user?.fullName}님 ${it.challengeName}에 참여해\n${it.reward?.name}을 받아보세요!")
                if (user?.serviceType?.isGuestUser == true) return@observe
                println("currentMission update 3")
                val scInfo = Preferences.startChallengeAlreadyAppeared
                val appearedIds = scInfo.studentIds
                println("currentMission update 4 ${appearedIds}, studentId : ${user?.studentID}")
                val isAlreadyAppearedUser = appearedIds.contains(user?.studentID)

                if (isAlreadyAppearedUser) return@observe
                println("currentMission update 5 it.userStatus : ${it.userStatus}")
                if (it.userStatus == ChallengeUserStatus.YET) {
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
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
            userInRepo.observe(viewLifecycleOwner) {
                showPaidView.postValue(it?.serviceType?.isPaidUser)
                userPaidServiceType.postValue(it?.serviceType)
            }
        }
    }

    override fun initUI() {}

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
        val setOnSpyMode = { (activity as LearningTabActivity).setOnSpyMode() }
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
        (activity as? LearningTabActivity)?.showGuestJoinInduceDialog {
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
    //        val dialog = PurchaseGuideDialog(2)
    //        childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
            startActivity(PurchaseGuideActivity.getIntent(requireContext()))
        }
    }

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
            (activity as LearningTabActivity).setSelectedTab(1)
            (activity as LearningTabActivity).setConceptCourseAvailableFirstSubject()
            (activity as LearningTabActivity).launchConceptCourseTutorial()
        }
        viewModel.joinChallengeById(challengeId) {
            val dialog = ChallengeGuideManager.getStartGuideMission1(
                nextEvent = nextEvent,
                exitEvent = {
                    val induceDialog = ChallengeInduceDialog(ChallengeInduceDialog.Type.Disappointed,
                        nextEvent = nextEvent,
                        exitEvent = {
                            MarketingManager.setMarketingBanner(requireContext())
                        }
                    )
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
                val dialog = StartChallengeInfoDialog(challenge.challengeId, true) { _ ->
                    startActivity(PurchaseGuideActivity.getIntent(requireContext()))
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
    }
}
