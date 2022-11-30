package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.DailyTestReportActivity
import com.freewheelin.pulley.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.activities.WrongTestReportActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.mypage.MyPageSettingDialogListener
import com.freewheelin.pulley.activities.mypage.MyRecommendSettingActivity
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentSnackTestBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import java.util.*
import kotlin.concurrent.timerTask

class SnackTestFragment : LearningTabFragment(),TestMainBaseListener, MyPageSettingDialogListener {

    var tests: List<Test> = listOf()

    companion object {
        @JvmStatic
        fun newInstance() = SnackTestFragment()
    }

    override var screenName = "테스트"

    lateinit var binding: FragmentSnackTestBinding

    var timer: Timer? = null
    var currentMainFragment: TestMainBaseFragment? = null
    var set: HashSet<TestPageBaseFragment> = HashSet()
    var scoringReceiver: BroadcastReceiver? = null
    var settingReceiver: BroadcastReceiver? = null
    var clearRecevier: BroadcastReceiver? = null

    var isStartWithInitTest = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scoringReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                syncTestList()
            }
        }

        settingReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncTestList()
            }
        }
        clearRecevier = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val fragmentActivity = activity
                if(fragmentActivity is LearningTabActivity
                        && fragmentActivity.isSelectedTab(this@SnackTestFragment)) {
                    syncTestList()
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(scoringReceiver!!, IntentFilter(TestManager.EVENT_TEST_SCORING))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(settingReceiver!!, IntentFilter(TestManager.EVENT_TEST_SETTING))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(clearRecevier!!, IntentFilter(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_snack_test, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(isStartWithInitTest) {
            initUI()
            syncTestList()
            wasInitUI = true
        }
    }

    override fun onDestroy() {
        if(scoringReceiver != null)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(scoringReceiver!!)
        if(settingReceiver != null)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(settingReceiver!!)
        if(clearRecevier != null)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(clearRecevier!!)
        super.onDestroy()
        deinitTimer()
    }

    override fun onReviewBtnClicked(test: Test) {
        val intent = SolveActivity.getReviewIntent(requireContext(), test)
        startActivity(intent)
    }

    override fun onSolveBtnClicked(test: Test) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "테스트 시작하기", test.getTestType().eventItemValue)
        if(Date() > test.endDate && (test.getTestType() == Test.TestType.weekly || test.getTestType() == Test.TestType.daily)) {
            val dialog = DialogUtils.makeDialog(requireContext(), "테스트를 볼 수 없습니다.", "시간이 만료되어 테스트를 볼 수 없습니다.\n다음 테스트를 기대해주세요. ", "확인", "")
            dialog.binding.rightBtn.visibility = View.GONE
            dialog.show()

        } else {
            val intent = SolveActivity.getIntent(requireContext(), test)
            startActivity(intent)
        }
    }

    override fun onSettingBtnClicked(test: Test) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "추천설정")

        val intent = MyRecommendSettingActivity.getIntent(requireContext())
        startActivity(intent)
    }

    override fun onReportBtnClicked(test: Test, fromGift: Boolean) {
        if(fromGift)
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "결과 상세보기", "선물상자화면")
        else
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "결과 상세보기", test.getTestType().eventItemValue)

        when(test.getTestType()) {
            Test.TestType.daily, Test.TestType.initial -> {
                val intent = DailyTestReportActivity.getIntent(requireContext(), test)
                startActivity(intent)
            }
            Test.TestType.weekly -> {
                val intent = WeeklyTestReportActivity.getIntent(requireContext(), test)
                startActivity(intent)
            }
            Test.TestType.wrong -> {
                val intent = WrongTestReportActivity.getIntent(requireContext(), test)
                startActivity(intent)
            }
            else -> {}
        }
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        syncTestList()
    }

    override fun onMoveBtnClikced(test: Test) {
        if(test.wrongInfo.totalProblemCount == 0) {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "오답테스트-링크", "유형학습")
            (activity as LearningTabActivity).setSelectedTab(3)
        } else {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "테스트", "오답테스트-링크", "오답노트")
            (activity as LearningTabActivity).setSelectedTab(5)
        }
    }

    override fun onModifyCompleted(user: User) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK,"테스트","추천설정","수정하기")
    }

    override fun initUI() {
        if (!::binding.isInitialized) return
        binding.apply {
            dailyContainer.setOnClickListener { onSelectorContainerClicked(it) }
            weeklyContainer.setOnClickListener { onSelectorContainerClicked(it) }
            wrongContainer.setOnClickListener { onSelectorContainerClicked(it) }
            runTimer()
        }
    }

    private fun syncTestList(reStudyTest: Test? = null) {
        TestManager.getTestList(requireContext(), user!!) {
            tests = it.filter { it.isPossibleTest() }

            if(reStudyTest == null) {

            } else {
                it.filter { test -> test.assignID == reStudyTest.assignID  }.firstOrNull()?.apply { this.isReStudy = true }
            }

            setSelectorUI()
            binding.apply {
                if (currentMainFragment == null) {
                    val test = tests.first()
                    when (test.getTestType()) {
                        Test.TestType.daily -> onSelectorContainerClicked(dailyContainer)
                        Test.TestType.weekly -> onSelectorContainerClicked(weeklyContainer)
                        Test.TestType.wrong -> onSelectorContainerClicked(wrongContainer)
                        else -> {}
                    }
                } else {
                    val testType = currentMainFragment!!.testType
                    when (testType) {
                        Test.TestType.daily -> onSelectorContainerClicked(dailyContainer)
                        Test.TestType.weekly -> onSelectorContainerClicked(weeklyContainer)
                        Test.TestType.wrong -> onSelectorContainerClicked(wrongContainer)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun onSelectorContainerClicked(view: View) {
        binding.apply {
            if (requireContext().isTablet) {
                dailyContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shadow_clear)
                weeklyContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shadow_clear)
                wrongContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shadow_clear)

                view.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.snack_shadow_border_purple
                )
            } else {
                dailyContainer.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_common_white_stroke_grey
                )
                weeklyContainer.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_common_white_stroke_grey
                )
                wrongContainer.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_common_white_stroke_grey
                )

                view.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_white_ffffff_stroke_purple_6d6dff_round
                )
            }
            val set = ConstraintSet()
            set.clone(rootView)

            when (view) {
                dailyContainer -> {
                    set.connect(arrowIv.id, TOP, dailyContainer.id, TOP)
                    set.connect(arrowIv.id, BOTTOM, dailyContainer.id, BOTTOM)
                    val test =
                        tests.filter { it.getTestType() == Test.TestType.daily }.firstOrNull()
                    // set test info to
                    setUserRecentSubject(test)

                    setMainFragment(test, Test.TestType.daily)
                    LogUtils.logEvent(
                        requireContext(),
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "테스트",
                        "데일리테스트"
                    )

                }
                weeklyContainer -> {
                    set.connect(arrowIv.id, TOP, weeklyContainer.id, TOP)
                    set.connect(arrowIv.id, BOTTOM, weeklyContainer.id, BOTTOM)
                    val test =
                        tests.filter { it.getTestType() == Test.TestType.weekly }.firstOrNull()
                    setMainFragment(test, Test.TestType.weekly)
                    LogUtils.logEvent(
                        requireContext(),
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "테스트",
                        "주간테스트"
                    )
                }
                wrongContainer -> {
                    set.connect(arrowIv.id, TOP, wrongContainer.id, TOP)
                    set.connect(arrowIv.id, BOTTOM, wrongContainer.id, BOTTOM)
                    tests.filter { it.getTestType() == Test.TestType.wrong }.firstOrNull()
                        ?.let { test ->
                            setMainFragment(test, Test.TestType.wrong)
                        }
                    LogUtils.logEvent(
                        requireContext(),
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "테스트",
                        "오답테스트"
                    )
                }
            }
            set.applyTo(rootView)
        }
    }

    private fun setUserRecentSubject(test:Test?) {
        test?.let {
            user?.setRecentStudyCode(test.dailyInfo.recentSubjectCode, test.dailyInfo.excludeSubjectCode)
        }
    }

    private fun setMainFragment(test: Test?, testType: Test.TestType) {
        when(testType) {
            Test.TestType.daily -> {
                if(test?.dailyInfo?.isNeedSetup() == true) {
                    val fragment = TestMainDailySetupFragment.newInstance(test)
                    addMainFragment(fragment)
                } else {
                    addMainFragment(TestMainDailyFragment.newInstance(test))
                }
            }

            Test.TestType.weekly -> {
                if(test?.isCompleted() == true)
                    addMainFragment(TestMainWeeklyFragment.newInstance(test))
                else {
                    val fragment = if (test?.isPossibleToSolve() == false) {
                        TestMainNeedMoreFragment.newInstance(test)
                    } else {
                        TestMainWeeklyFragment.newInstance(test)
                    }
                    addMainFragment(fragment)
                }
            }

            Test.TestType.wrong -> {
                if(test?.wrongInfo?.isNeedMoreProblem() == true)
                    addMainFragment(TestMainWrongXFragment.newInstance(test))
                else
                    addMainFragment(TestMainWrongFragment.newInstance(test))
            }
            else -> {}
        }
    }

    private fun addMainFragment(frag: TestMainBaseFragment) {
        currentMainFragment = frag
        currentMainFragment?.listener = this@SnackTestFragment
        currentMainFragment?.add(childFragmentManager)
    }


    fun runTimer() {
        timer = Timer()
        val task = timerTask {
            tick()
        }
        timer?.schedule(task, 1000, 1000)
    }

    fun deinitTimer() {
        timer?.cancel()
        timer = null
    }

    fun tick() {
        activity?.runOnUiThread {
            set.forEach {
                it.tick()
            }
            currentMainFragment?.tick()
        }
    }

    private fun setSelectorUI() {
        val dailyTest = tests.filter { it.getTestType() == Test.TestType.daily }.firstOrNull()
        val weeklyTest = tests.filter { it.getTestType() == Test.TestType.weekly }.firstOrNull()
        val wrongTest = tests.filter { it.getTestType() == Test.TestType.wrong }.firstOrNull()
        binding.apply {
            dailyContainer.setUpUI(dailyTest)
            weeklyContainer.setUpUI(weeklyTest)
            wrongContainer.setUpUI(wrongTest)
        }
    }
}
