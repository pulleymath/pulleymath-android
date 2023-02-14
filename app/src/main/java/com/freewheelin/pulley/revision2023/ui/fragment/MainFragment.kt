package com.freewheelin.pulley.revision2023.ui.fragment


import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentMain2Binding
import com.freewheelin.pulley.revision2021.utils.observeOnce
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
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog

class MainFragment : LearningTabFragment(), DDaySettingDialogListener, LifecycleObserver,
    LifecycleEventObserver {

    override var screenName: String = "메인"
    lateinit var binding: FragmentMain2Binding

    var profileReceiver: BroadcastReceiver? = null
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
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        if (!::binding.isInitialized) return
        syncProfile()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(profileReceiver!!, IntentFilter(UserManager.EVENT_USER_MODIFYING))
        viewModel.showWholeProgressBar.postValue(true)
        init()
    }

    fun init() {
        if (!::binding.isInitialized) return
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            viewModel.initUserInfo()
            initRv()

            dDayTv.setOnClickListener { onDDayBtnClicked() }
            startPayBtn?.setOnClickListener { onStartBtnClicked() }

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
                    // TODO how to mobile?
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
            mainProfile.observe(viewLifecycleOwner) { mainProfile ->
                binding.apply {
                    showWholeProgressBar.postValue(false)
                    MarketingManager.setMarketingBanner(requireContext(), mainProfile)
                }
            }

            currentMission.observeOnce(viewLifecycleOwner) {
                if (Preferences.startChallengeAlreadyAppearedFlog.get()) return@observeOnce
                if (it.userStatus == ChallengeUserStatus.YET) {
                    val dialog = StartChallengeInfoDialog(it.challengeId) { challengeId ->
                        viewModel.joinChallenge(challengeId) {
                            val dialog = ChallengeGuideManager
                                .getStartGuideMission1(
                                    nextEvent = {
                                        (activity as LearningTabActivity).setSelectedTab(1)
                                        (activity as LearningTabActivity).setConceptCourseSubjectId(0)
                                    }
                                )
                            childFragmentManager.let { dialog.show(it, "StartGuide") }
                            // TODO 주석처리 : 스챌 팝업플래그인데 앱출시할땐 주석 풀어야함
//                            Preferences.startChallengeAlreadyAppearedFlog.set(true)
                        }
                    }
                    childFragmentManager.let { dialog.show(it, "StartChallengeInfoDialog") }

                }
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

            viewModel.initChallenge()
        }
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            viewModel.fetchUserProfile(requireContext())
        }
    }

    fun syncProfile() {
        Log.d("마케팅", "syncProfile() is called!!!")
        viewModel.fetchUserProfile(requireContext())
    }

    private fun onDDayBtnClicked() {
//        val spyCount = (activity as LearningTabActivity).spyCount
//        val setOnSpyMode = { (activity as LearningTabActivity).setOnSpyMode() }
//        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "디데이꺽쇠")
//        val dialog = DDaySettingDialog(requireContext(), setOnSpyMode)
//        dialog.listener = this
//        dialog.show()

        val dialog = ChallengeCompletedDialog(viewModel.currentMission.value!!) {
            ChallengeManager.getMainTabMoveIntent(it).let {
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
            }
//            if (it == null) {
//                //메인으로 이동
//                ChallengeManager.getMainTabMoveIntent(it).let {
//                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
//                }
//            } else {
//                ChallengeManager.getMainTabMoveIntent(it).let {
//                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
//                }
//            }
        }
        childFragmentManager.let { dialog.show(it, "ChallengeCompletedDialog") }

//        val dialog = StartChallengeInfoDialog(true) {
//            viewModel.showStartChallengeGuide.postValue(true)
//        }
//        childFragmentManager.let { dialog.show(it, "StartChallengeInfoDialog") }

//        val dialog = ChallengeCompletedDialog() {
//
//            ChallengeManager.getChallengeIntent(StartChallenge.Companion.Sequence.워크북.seq).let {
//                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
//            }
//        }
//        childFragmentManager.let { dialog.show(it, "ChallengeCompletedDialog") }

    }

    private fun onStartBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "구독하기버튼")
        FacebookEvent.log(requireContext(), FacebookEvent.SUBSCRIBE_STARTED)
        IntentUtils.openWebLink(requireContext(), URL.구매촉구_메인, requireContext().packageManager)
    }

    override fun onOnDDaySettingCompleted() {
        syncProfile()
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
        if(profileReceiver != null)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver!!)
    }
}
