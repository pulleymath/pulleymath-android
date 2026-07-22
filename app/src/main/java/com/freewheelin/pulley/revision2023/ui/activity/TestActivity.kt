package com.freewheelin.pulley.revision2023.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityTestBinding
import com.freewheelin.pulley.databinding.ActivityWorkbookListBinding
import com.freewheelin.pulley.databinding.FragmentSnackTestBinding
import com.freewheelin.pulley.legacy.activities.DailyTestReportActivity
import com.freewheelin.pulley.legacy.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.legacy.activities.WrongTestReportActivity
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.SnackTestFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainBaseFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainBaseListener
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainDailyFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainDailySetupFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainNeedMoreFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainWeeklyFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainWrongFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestMainWrongXFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.TestPageBaseFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyPageSettingDialogListener
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.ProblemManager
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.utils.autoCloseOnChatBotExit
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import com.freewheelin.pulley.revision2023.viewmodel.TestActViewModel
import java.util.Date
import java.util.HashSet
import java.util.Timer
import kotlin.concurrent.timerTask

class TestActivity : AppCompatActivity(), LifecycleObserver, TestMainBaseListener,
    MyPageSettingDialogListener {
    val binding: ActivityTestBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_test, null, false)
    }
    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, TestActivity::class.java).apply {

            }
        }
    }

    var tests: List<Test> = listOf()

    val viewModel: TestActViewModel by viewModels()

    var timer: Timer? = null
    var currentMainFragment: TestMainBaseFragment? = null
    var set: HashSet<TestPageBaseFragment> = HashSet()
    lateinit var scoringReceiver: BroadcastReceiver
    lateinit var settingReceiver: BroadcastReceiver
    lateinit var clearReceiver: BroadcastReceiver
    lateinit var reConfigureReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        initReceiver()
        initChatBot()
        addBackBtnCallback()
        syncTestList()
        viewModel.apply {
            schoolTypeInRepo.observe(this@TestActivity) {
                syncTestList()
            }
        }
    }
    private fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.chatBotBgCl.isVisible) {
                binding.chatBotBgCl.visibleIf(false)
                binding.chatBotCv.visibleIf(false)
                binding.chatBotBtn.startLongClickDescAnim()
                return@addCallback
            } else {
                finish()
            }
        }
    }
    private fun initChatBot() {
        binding.apply {
            chatBotBtn.setOnClickListener {
                if (chatBotBgCl.isVisible) {
                    chatBotBgCl.visibleIf(false)
                    chatBotCv.visibleIf(false)
                    chatBotBtn.startLongClickDescAnim()
                }  else {
                    val url = Network.webAppUrl + "/ottway?token=$token&uri=chat-bot"
                    binding.webView.loadUrl(url)
                    chatBotBgCl.visibleIf(true)
                    chatBotCv.visibleIf(true)
                }
            }
            webView.let {
                val onClose = {
                    runOnUiThread {
                        chatBotBgCl.visibleIf(false)
                        chatBotBtn.startLongClickDescAnim()
                    }
                }
                it.autoCloseOnChatBotExit(onClose)
                it.addJavascriptInterface(
                    ChatBotClientClickEventListener (
                        onCloseListener = onClose,
                        errorCloseListener = onClose
                    ), "android")

                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
            }
        }
    }

    private fun initReceiver() {

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
        clearReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
//                val fragmentActivity = activity
//                if(fragmentActivity is MainActivity
//                    && fragmentActivity.isSelectedTab(this@SnackTestFragment)) {
                syncTestList()
//                }
            }
        }
        reConfigureReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                init()
                syncTestList()
            }

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(scoringReceiver, IntentFilter(
            TestManager.EVENT_TEST_SCORING)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(settingReceiver, IntentFilter(
            TestManager.EVENT_TEST_SETTING)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(clearReceiver, IntentFilter(
            ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(reConfigureReceiver, IntentFilter(
            UserManager.RE_CONFIGURE_UI)
        )
    }

    override fun onDestroy() {
        listOf(scoringReceiver, settingReceiver, clearReceiver, reConfigureReceiver)
            .forEach {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
            }
        super.onDestroy()
        deinitTimer()
    }

    override fun onReviewBtnClicked(test: Test) {
        val intent = SolveActivity.getReviewIntent(this, test)
        startActivity(intent)
    }
    
    fun showGuestJoinInduceDialog(dismissCallback: () -> Unit) {
        val dialog = JoinInduceForGuestDialog().apply {
            updateDismissCallback {
                dismissCallback()
                viewModel.errorStatusReset()
            }
        }
        supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
    }

    override fun onSolveBtnClicked(test: Test) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "테스트 시작하기", test.getTestType().eventItemValue)
        if (user?.serviceType?.isGuestUser == true) {
            showGuestJoinInduceDialog {
                viewModel.errorStatusReset()
            }
            return
        }
        if(Date() > test.endDate && (test.getTestType() == Test.TestType.weekly || test.getTestType() == Test.TestType.daily)) {
//            val dialog = DialogUtils.makeDialog(this, "테스트를 볼 수 없습니다.", "시간이 만료되어 테스트를 볼 수 없습니다.\n다음 테스트를 기대해주세요. ", "확인", "")
//            dialog.binding.rightBtn.visibility = View.GONE
//            dialog.show()

            DialogUtils.confirmV2(
                context = this,
                title = "테스트를 볼 수 없습니다.",
                contents = "시간이 만료되어 테스트를 볼 수 없습니다.\n다음 테스트를 기대해주세요. ",
                isOneBtn = true,
                rightBtnText = "확인",
            )

        } else {
            val intent = SolveActivity.getIntent(this, test)
            startActivity(intent)
        }
    }

    override fun onSettingBtnClicked(test: Test) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "추천설정")

//        val intent = MyRecommendSettingActivity.getIntent(this, test)
//        startActivity(intent)
        val dialog = SnackTestRecommendSettingDialog.newInstance(test)
        supportFragmentManager.let { dialog.show(it, "SnackTestRecommendSettingDialog") }
    }

    override fun onReportBtnClicked(test: Test, fromGift: Boolean) {
        if(fromGift)
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "결과 상세보기", "선물상자화면")
        else
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "결과 상세보기", test.getTestType().eventItemValue)

        when(test.getTestType()) {
            Test.TestType.daily, Test.TestType.initial -> {
                val intent = DailyTestReportActivity.getIntent(this, test)
                startActivity(intent)
            }
            Test.TestType.weekly -> {
                val intent = WeeklyTestReportActivity.getIntent(this, test)
                startActivity(intent)
            }
            Test.TestType.wrong -> {
                val intent = WrongTestReportActivity.getIntent(this, test)
                startActivity(intent)
            }
            else -> {}
        }
    }

    override fun onMoveBtnClikced(test: Test) {
        // TODO 공부하러가기 or 오답풀러가기
        if(test.wrongInfo.totalProblemCount == 0) {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "오답테스트-링크", "유형학습")
            finish()
        } else {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "오답테스트-링크", "오답노트")
            finish()
        }
    }

    override fun onModifyCompleted(user: UserV4) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK,"테스트","추천설정","수정하기")
    }

    private fun init() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = this@TestActivity

            dailyContainer.setOnClickListener { onSelectorContainerClicked(it) }
            weeklyContainer.setOnClickListener { onSelectorContainerClicked(it) }
            wrongContainer.setOnClickListener { onSelectorContainerClicked(it) }
            runTimer()

            backBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun syncTestList(reStudyTest: Test? = null) {
        TestManager.getTestList(this) {
            tests = it.filter { it.isPossibleTest() }

            if (reStudyTest == null) {

            } else {
                it.filter { test -> test.assignID == reStudyTest.assignID }.firstOrNull()
                    ?.apply { this.isReStudy = true }
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
            if (this@TestActivity.isTablet) {
                dailyContainer.background =
                    ContextCompat.getDrawable(this@TestActivity, R.drawable.shadow_clear)
                weeklyContainer.background =
                    ContextCompat.getDrawable(this@TestActivity, R.drawable.shadow_clear)
                wrongContainer.background =
                    ContextCompat.getDrawable(this@TestActivity, R.drawable.shadow_clear)

                view.background = ContextCompat.getDrawable(
                    this@TestActivity,
                    R.drawable.snack_shadow_border_purple
                )
            } else {
                dailyContainer.background = ContextCompat.getDrawable(
                    this@TestActivity,
                    R.drawable.bg_common_white_stroke_grey
                )
                weeklyContainer.background = ContextCompat.getDrawable(
                    this@TestActivity,
                    R.drawable.bg_common_white_stroke_grey
                )
                wrongContainer.background = ContextCompat.getDrawable(
                    this@TestActivity,
                    R.drawable.bg_common_white_stroke_grey
                )

                view.background = ContextCompat.getDrawable(
                    this@TestActivity,
                    R.drawable.bg_white_stroke_purple_300_round
                )
            }
            val set = ConstraintSet()
            set.clone(rootView)

            when (view) {
                dailyContainer -> {
                    set.connect(arrowIv.id, ConstraintSet.TOP, dailyContainer.id, ConstraintSet.TOP)
                    set.connect(arrowIv.id,
                        ConstraintSet.BOTTOM, dailyContainer.id,
                        ConstraintSet.BOTTOM
                    )
                    val test =
                        tests.filter { it.getTestType() == Test.TestType.daily }.firstOrNull()
                    // set test info to
//                    setUserRecentSubject(test)

                    setMainFragment(test, Test.TestType.daily)
                    LogUtils.logEvent(
                        this@TestActivity,
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "테스트",
                        "데일리테스트"
                    )

                }
                weeklyContainer -> {
                    set.connect(arrowIv.id,
                        ConstraintSet.TOP, weeklyContainer.id,
                        ConstraintSet.TOP
                    )
                    set.connect(arrowIv.id,
                        ConstraintSet.BOTTOM, weeklyContainer.id,
                        ConstraintSet.BOTTOM
                    )
                    val test =
                        tests.filter { it.getTestType() == Test.TestType.weekly }.firstOrNull()
                    setMainFragment(test, Test.TestType.weekly)
                    LogUtils.logEvent(
                        this@TestActivity,
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "테스트",
                        "주간테스트"
                    )
                }
                wrongContainer -> {
                    set.connect(arrowIv.id, ConstraintSet.TOP, wrongContainer.id, ConstraintSet.TOP)
                    set.connect(arrowIv.id,
                        ConstraintSet.BOTTOM, wrongContainer.id,
                        ConstraintSet.BOTTOM
                    )
                    tests.filter { it.getTestType() == Test.TestType.wrong }.firstOrNull()
                        ?.let { test ->
                            setMainFragment(test, Test.TestType.wrong)
                        }
                    LogUtils.logEvent(
                        this@TestActivity,
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

    private fun setUserRecentSubject(test: Test?) {
//        test?.let {
//            user?.setRecentStudyCode(test.dailyInfo.recentSubjectCode, test.dailyInfo.excludeSubjectCode)
//        }
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
        currentMainFragment?.listener = this@TestActivity
        currentMainFragment?.add(supportFragmentManager)
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
        runOnUiThread {
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