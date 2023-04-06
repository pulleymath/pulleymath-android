package com.freewheelin.pulley.activities.learning


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.lesson.LessonActivity
import com.freewheelin.pulley.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.auth.InitTestActivity.Companion.COMPLETED_SNACK_TEST
import com.freewheelin.pulley.activities.learning.tabFragment.affiliatedTest.AffiliatedTestFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.AnalysisFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.StudyHistoryActivity
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_ANALYSIS
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_MOCK
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_TEST
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_UNIT
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_WRONG
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.activities.learning.tabFragment.snackTest.SnackTestFragment
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.StudentManagerDialog
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.WrongNoteFragment
import com.freewheelin.pulley.activities.mypage.*
import com.freewheelin.pulley.bases.*
import com.freewheelin.pulley.core.manage.*
import com.freewheelin.pulley.databinding.ActivityLearningBinding
import com.freewheelin.pulley.dialogs.CompleteDialogConfirm
import com.freewheelin.pulley.revision2021.activity.AlarmActivity
import com.freewheelin.pulley.revision2021.activity.dialog.UpdateGradeDialog
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager.IS_START_CHALLENGE_COMPLETED
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity.Companion.PURCHASE_SUCCESS
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.viewmodel.LearningTabViewModel
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.snackBar.SnackBar
import com.freewheelin.pulley.views.snackBar.SnackBarView
import com.freewheelin.pulley.views.snackBar.SnackBarViewListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


abstract class LearningTabFragment : Fragment() {
    var wasInitUI: Boolean = false

    abstract var screenName: String
    abstract fun initUI()

    open fun onFragmentSelected() {
        if(context != null && activity != null) {
            val firebase = FirebaseAnalytics.getInstance(requireContext())
            firebase.setCurrentScreen(requireActivity(), screenName, LearningTabFragment::class.java.name)
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, screenName)
        }
    }
}

class LearningTabActivity : PermissionActivity(),
    ViewPager.OnPageChangeListener,
    DrawerLayout.DrawerListener,
    LifecycleObserver,
    AppUsageMonitorListener {

    var snackBar: SnackBar? = null
    var mypageFragment = MyMainPageFragment()
    private val viewModel: LearningTabViewModel by viewModels()

    private val binding: ActivityLearningBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_learning, null, false)
    }

    var spySeal1 = 1
    var spySeal2 = 3
    var spySeal3 = 2

    var firstClickCnt = 0
    var secondClickCnt = 0
    var thirdClickCnt = 0

    var doubleBackToExitPressedOnce = false

    lateinit var tabMoveReceiver: BroadcastReceiver
    lateinit var challengeReceiver: BroadcastReceiver
    lateinit var purchaseReceiver: BroadcastReceiver

    var currentPagePosition = 0

    val lessonPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS)
    val lessonRequest = 1001

    companion object {
        const val LEARNING_MAIN = "LEARNING_MAIN"
        const val LEARNING_ANALYSIS = "LEARNING_ANALYSIS"
        const val LEARNING_TEST = "LEARNING_TEST"
        const val LEARNING_AFFILIATED_TEST = "LEARNING_AFFILIATED_TEST"
        const val LEARNING_UNIT = "LEARNING_UNIT"
        const val LEARNING_MOCK = "LEARNING_MOCK"
        //        const val LEARNING_WRONG = "LEARNING_WRONG"
        const val LEARNING_NOTE = "LEARNING_NOTE"
        const val LEARNING_LIST = "LEARNING_LIST"
        const val LEARNING_COURSE = "LEARNING_COURSE"

        const val FILTER_SESSION_EXPIRED = "FILTER_SESSION_EXPIRED"

        // for Session expired dialog in Api.class
        var referActivity: Activity? = null
    }

    var tabFragment: MutableList<LearningTabFragment> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setScreenOrientation() // TODO 모바일 portrait 적용할때 사용

        // TODO 추후 푸시알람 실행시 어딘가로 보내야할수도 있다.
        val target = intent.getStringExtra("target_android")
        tabFragment = mutableListOf(
            MainFragment.newInstance(),
            ConceptCourseFragment.newInstance(),
            PatternStudyFragment.newInstance(),
            MockExamFragment.newInstance(),
            SnackTestFragment.newInstance(),
            WrongNoteFragment.newInstance(),
            AnalysisFragment.newInstance()
        )

        if (user?.showMainUnivTab == true) {
            tabFragment.add(AffiliatedTestFragment.newInstance())
        }

        setContentView(binding.root)
        initReceiver()
        initObserve()

        with(binding) {
            viewPager.adapter = TabAdapter(supportFragmentManager)

            viewPager.setPagingEnabled(false)
            viewPager.offscreenPageLimit = 5

            viewPager.addOnPageChangeListener(this@LearningTabActivity)

            tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    println("asoaso onTabSelected ${tab?.position}")
                    tab?.position?.let { position ->
                        if (position == 0) { checkStartChallengeFinish() }
                        if (isTablet) {
                            when (position) {
                                7 -> openLesson()
                                else -> {
                                    viewPager.currentItem = position
                                    currentPagePosition = position
                                }
                            }
                        } else {
                            viewPager.currentItem = position
                            currentPagePosition = position
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) { }
                override fun onTabReselected(tab: TabLayout.Tab?) { }
            })


            initUnivTab()


            // 핸드폰이면 과외 메뉴 숨기기
            if(!isTablet) {
                if(tabLayout.tabCount > 7) tabLayout.removeTabAt(7)
//                tabLayout.removeTabAt(1)
            }

            drawerView.addDrawerListener(this@LearningTabActivity)
            drawerView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            moveTo(mypageFragment, false)
            alarmBtn.setOnClickListener {
                val intent = AlarmActivity.getIntent(this@LearningTabActivity)
                startActivity(intent)
            }
            mypageBtn.setOnClickListener {
                if(viewPager.currentItem == 0 && firstClickCnt < spySeal1)
                    firstClickCnt += 1

                if(viewPager.currentItem == 2 && firstClickCnt == spySeal1 && secondClickCnt < spySeal2) {
                    secondClickCnt += 1
                } else if(viewPager.currentItem == 2){
                    firstClickCnt = 0
                    secondClickCnt = 0
                    thirdClickCnt = 0
                }

                if(viewPager.currentItem == 4 && firstClickCnt == spySeal1 && secondClickCnt == spySeal2 && thirdClickCnt < spySeal3) {
                    thirdClickCnt += 1
                    if(firstClickCnt == spySeal1 && secondClickCnt == spySeal2 && thirdClickCnt == spySeal3) {
                        isSPYMode = true
                        mypageFragment.spyOn()
                        spyBtn.show()
                    }
                } else if(viewPager.currentItem == 4) {
                    firstClickCnt = 0
                    secondClickCnt = 0
                    thirdClickCnt = 0
                }

                if (spyCount == 10) {
                    isSPYMode = true
                    mypageFragment.spyOn()
                    spyBtn.show()
                    spyCount = 0
                }

                onMypageBtnClicked()
            }

            if(isSPYMode) {
                spyBtn.show()
            }

            spyBtn.setOnClickListener {
                val intent = Intent(this@LearningTabActivity, AnalysisTabActivity::class.java)
                startActivity(intent)
//            onSpyBtnClicked()
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@LearningTabActivity)


            LocalBroadcastManager.getInstance(this@LearningTabActivity).registerReceiver(tabMoveReceiver, IntentFilter(PieceManager.EVENT_MOVE_TAB))
            LocalBroadcastManager.getInstance(this@LearningTabActivity).registerReceiver(challengeReceiver, IntentFilter(ChallengeManager.MAIN_SCREEN_TAB_MOVE_EVENT))
            LocalBroadcastManager.getInstance(this@LearningTabActivity).registerReceiver(purchaseReceiver, IntentFilter(PURCHASE_SUCCESS))
// for Api.class
            if(referActivity == null) referActivity = this@LearningTabActivity
            registerReceiver(mainEventReceiver, IntentFilter(FILTER_SESSION_EXPIRED))
//            registerReceiver(firebasePushEventReceiver, IntentFilter("FIREBASE-PUSH"))

            userTest()
        }
    }
    fun initUnivTab() {
//        if (user?.showMainUnivTab == true) {
//            tabFragment.add(AffiliatedTestFragment.newInstance())
//        }

        val univTabIndex = 8
        if (user?.isUnivUser == false) {
            if(binding.tabLayout.tabCount > univTabIndex) binding.tabLayout.removeTabAt(univTabIndex)
        } else {
            val univTabName = when(user?.schoolID) {
                6000 -> "KU진단"
                7000 -> "숭실대"
                else -> "대학"
            }
            binding.tabLayout.getTabAt(univTabIndex)?.view?.findViewById<TextView>(R.id.tabTitleTv)?.text = univTabName

        }
    }
    fun initReceiver () {
        tabMoveReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
//                openStudyHistory("단원 분석", "단원 학습지 만들기")
                intent?.let { intent ->
                    val tabIndex = intent.getIntExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 0)
                    setSelectedTab(tabIndex)
                    when(tabIndex) {
                        6 -> {
                            (tabFragment[tabIndex] as AnalysisFragment).setTodayStudyNewOne()
                        }
                    }

                    val wantScroll = intent.getBooleanExtra(PieceManager.EVENT_SCROLL, false)
                    if (!wantScroll) return

                    when {
                        intent.getBooleanExtra(PieceManager.EVENT_SCROLL_UNIT_TOTAL_LABEL, false) -> {
                            val subject = intent.getStringExtra(PieceManager.EVENT_FILTER) ?: return
                            // TODO scroll
//                            (tabFragment[2] as? PatternStudyFragment)?.let {
//                                it.goPulleyMathBooks()
//                            }
//                            (tabFragment[2] as BookFragment).scrollToTotalLabel(subject)
                        }
                        else -> {}
                    }
                }
            }
        }
        challengeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val challengeCourseId = it.getIntExtra(ChallengeManager.COURSE_ID, -1)
                    when (challengeCourseId) {
                        ChallengeManager.CourseName.스타트챌린지_개념.id -> {
                            tabMoveAndSendConceptBroadcast(1, challengeCourseId)
                        }
                        ChallengeManager.CourseName.스타트챌린지_유형.id,
                        ChallengeManager.CourseName.스타트챌린지_북스.id,
                        ChallengeManager.CourseName.스타트챌린지_워크북.id -> {
                            tabMoveAndSendPatternStudyBroadcast(2, challengeCourseId)
                        }
                        else -> {
                            tabMove(0)
                        }
                    }
                }
            }
            fun tabMoveAndSendPatternStudyBroadcast(tabIndex: Int, courseId: Int?) {
                tabMove(tabIndex)
                ChallengeManager.getPatternStudyMoveIntent(courseId).let { intent ->
                    LocalBroadcastManager.getInstance(this@LearningTabActivity).sendBroadcast(intent)
                }
            }
            fun tabMoveAndSendConceptBroadcast(tabIndex: Int, courseId: Int?) {
                println("asoaso tabMoveAnd Send Concept")
                tabMove(tabIndex)
                ChallengeManager.getConceptStudyMoveIntent(courseId).let { intent ->
                    LocalBroadcastManager.getInstance(this@LearningTabActivity).sendBroadcast(intent)
                }
            }
        }
        purchaseReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    println("qwoqwo purchaseReceiver onreceive 1")
                    viewModel.fetchUser {
                        println("qwoqwo purchaseReceiver onreceive 2")
                    }
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.apply {
            user.observe(this@LearningTabActivity) { user ->
                user?.let {
                    if (MyApplication.user == null) {
                        MyApplication.user = it
                    } else {
                        MyApplication.user!!.update(it)
                    }

                    MyApplication.user?.run {
                        it.noShowAddOptionalDate = noShowAddOptionalDate
                        it.noShowAddOptionalSubject = noShowAddOptionalSubject
                        it.excludeSubjectCode = excludeSubjectCode
                        it.recentSubjectCode = recentSubjectCode
                    }
                    if (it.token.isNotEmpty()) {
                        MyApplication.user = it
                        MyApplication.token = it.token
                    }
                    it.commit("LearningTabAct observe")
                }
            }
        }
    }
    fun setUserObserveAttachedByMainFragment() {
        viewModel.user.observe(this) {
            tabFragment.find { it.screenName == "메인" }?.let { frag ->
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    (frag as MainFragment).viewModel.initUserInfo()
                }
            }
        }
    }
    var spyCount = 0

    fun setOnSpyMode() {
        spyCount += 1
        if (spyCount > 10) {
            isSPYMode = true
            mypageFragment.spyOn()
            binding.spyBtn.show()
            spyCount = 0
        }
    }

    private fun openLesson() {
        requirePermissions(lessonPermissions, lessonRequest)
    }

    private fun processLesson() {
        startActivity(Intent(baseContext, LessonActivity::class.java))
        thread(start=true) {
            Thread.sleep(500)
            runOnUiThread { binding.tabLayout.getTabAt(currentPagePosition)?.select() }
        }
    }

    private fun userTest() {
        val url = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/mes/teachers.json"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
                if (data != null && data.isNotEmpty()) {
                    Gson().fromJson(data, StudentManagerDialog.StudentManagerResponse::class.java)
                        .let { runOnUiThread { checkTeachers(it) } }
                }
            } catch (e: Exception) {
                Log.d("마케팅에러", "error=${e.localizedMessage}")
            }
        }
    }

    private fun checkTeachers(response: StudentManagerDialog.StudentManagerResponse) {
        val isRegisteredTeacherEmail = response.admins.contains(user?.email)

        if(isRegisteredTeacherEmail) {
            binding.loadStudentBtn.visibility = View.VISIBLE
            binding.loadStudentBtn.setOnClickListener {
                StudentManagerDialog(this, {},{}).show()
            }
        }
    }

    var mainEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(javaClass.simpleName, "Api path intent=${intent}")
            user?.token = ""
            user?.commit("DialogUtils.expiredSessionDialog")
            DialogUtils.expiredSessionDialog(this@LearningTabActivity)
        }
    }
//    var firebasePushEventReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            Log.d(javaClass.simpleName, "firebase push intent=${intent}")
//
//            println("tpehf, call firebasePushEventReceiver, check new alarm ")
//            checkNewAlarm()
//        }
//    }

    fun tabMove(index: Int) {
        binding.apply {
            tabLayout.selectTab(tabLayout.getTabAt(index))
        }
    }
    fun checkStartChallengeFinish() {
        val isStartChallengeCompleted = intent.getBooleanExtra(IS_START_CHALLENGE_COMPLETED, false)
        println("asoaso  - - - - - iss cc ${isStartChallengeCompleted} , ${viewModel.showStartChallengeFinishEffect()}")
        if (isStartChallengeCompleted || viewModel.showStartChallengeFinishEffect()) {

            tabFragment.find { it.screenName == "메인" }?.let { frag ->
                (frag as MainFragment).showStartChallengeCompletedGuide()
                viewModel.updateChallengeFinishFlag()
            }
        }
    }
    fun moveConceptCourseSubject(id: Int) {
        val conceptFragment = tabFragment.find { it.screenName == "개념" } as ConceptCourseFragment?
        conceptFragment?.moveSubjectId(id)
    }
    override fun onResume() {
        super.onResume()
        AppUsageMonitor.startAppUsage()
        viewModel.fetchUserChallenges()
        binding.apply {
            updateSignView.visibility =
                if (VersionManager.isNeedToUpdate() == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            CoroutineScope(Dispatchers.IO).launch {
                ServerStatusManager.setServerInspectionDialog(this@LearningTabActivity)
            }

            checkAffiliatedTestExist()
            checkNewAlarm()
        }
    }

    @SuppressLint("CheckResult")
    private fun checkAffiliatedTestExist() {
        if (user?.showMainUnivTab != true) {
            val tabName = binding.tabLayout.getTabAt(binding.tabLayout.tabCount - 1)?.text ?: return
            if (tabName == AffiliatedTestFragment.newInstance().screenName) {
                binding.tabLayout.removeTabAt(binding.tabLayout.tabCount - 1)
                return
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun checkNewAlarm() {
        // TODO 새 알람 있는지 체크하는 api가 생기면 바꿔야함
        AlarmRepository().fetchMessages()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "fetchMessages list=>${res.data}")

                var isNewAlarmExist = false
                res.data.forEach {
                    if (!it.isRead) {
                        isNewAlarmExist = true
                        return@forEach
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    binding.updateAlarmView.visibility = if (isNewAlarmExist) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

            }, { error ->
                Log.e(javaClass.simpleName, "fetchMessages error=${error.localizedMessage}")
            })
    }

    private fun openStudyHistory(category: String, item_name: String) {
        LogUtils.logEvent(baseContext, user, PulleyEvent.BUTTON_CLICK, category, item_name)
        val intent = StudyHistoryActivity.getIntent(baseContext)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode) {
            RESULT_SNACK_TEST -> {
                setSelectedTab(4)
            }
            RESULT_SNACK_UNIT -> {
                setSelectedTab(2)
            }
            RESULT_SNACK_MOCK -> {
                setSelectedTab(3)
            }
            RESULT_SNACK_WRONG -> {
                setSelectedTab(5)
            }
            RESULT_SNACK_ANALYSIS -> {
                val intent = Intent(this, AnalysisTabActivity::class.java)
                startActivity(intent)
            }
            COMPLETED_SNACK_TEST -> {

            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tabMoveReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(challengeReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(purchaseReceiver)
        AppUsageMonitor.finishAppUsage()

        referActivity = null
        unregisterReceiver(mainEventReceiver)
//        unregisterReceiver(firebasePushEventReceiver)

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isTablet) {
            if(tabFragment[0].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_MAIN, tabFragment[0])
            if(tabFragment[1].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_COURSE, tabFragment[1])
            if(tabFragment[2].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_UNIT, tabFragment[2])
            if(tabFragment[3].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_MOCK, tabFragment[3])
            if(tabFragment[4].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_TEST, tabFragment[4])
            if(tabFragment[5].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_NOTE, tabFragment[5])
            if(tabFragment[6].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_ANALYSIS, tabFragment[6])

            if (tabFragment.size > 7 && tabFragment[7].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_LIST, tabFragment[7])

            if (user?.showMainUnivTab == true) {
                if (tabFragment.size > 8 && tabFragment[8].isAdded) {
                    supportFragmentManager.putFragment(outState, LEARNING_AFFILIATED_TEST, tabFragment[7])
                }
            }
        } else {
            if(tabFragment[0].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_MAIN, tabFragment[0])
            if(tabFragment[1].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_UNIT, tabFragment[1])
            if(tabFragment[2].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_MOCK, tabFragment[2])
            if(tabFragment[3].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_TEST, tabFragment[3])
            if(tabFragment[4].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_NOTE, tabFragment[4])
            if(tabFragment[5].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_ANALYSIS, tabFragment[5])

            if (tabFragment.size > 6 && tabFragment[6].isAdded)
                supportFragmentManager.putFragment(outState, LEARNING_LIST, tabFragment[6])

            if (user?.showMainUnivTab == true) {
                if (tabFragment.size > 7 && tabFragment[7].isAdded) {
                    supportFragmentManager.putFragment(outState, LEARNING_AFFILIATED_TEST, tabFragment[6])
                }
            }
        }
    }

    fun showSnackBar(text: String, buttonText: String, action: (() -> Unit)? = null) {
        if (snackBar?.isShowing == true) {
            snackBar?.dismiss()
        }

        if (snackBar == null) {
            val snackBarWindow = SnackBar(this, text, buttonText)

            this.snackBar = snackBarWindow
        } else {
            snackBar!!.contentText = text
            snackBar!!.actionText = buttonText
        }
        snackBar?.setSnackBarViewListener(object : SnackBarViewListener {
            override fun onXBtnClicked(view: SnackBarView) {
                snackBar?.dismiss()
            }

            override fun onActionBtnClicked(view: SnackBarView) {
                if (action == null) {
//                    tabLayout.getTabAt(5)?.select()
//                    openStudyHistory("오답 노트", "오답학습하기")
                    val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                    intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 6)
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
                } else {
                    action()
                }
                snackBar?.dismiss()

            }
        })

        snackBar!!.show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        VersionManager.requestVersionInfo(this) { update, info ->
            if(update == VersionManager.Required.MAJOR) {
                DialogUtils.needAppUpgradeDialog(this)
//            } else if(user?.token?.isEmpty() == true) {
//                sendBroadcast(Intent(FILTER_SESSION_EXPIRED))
            } else {
                if (MyApplication.isAppFirstLaunch) {
                    handleUser()
                }
                MyApplication.isAppFirstLaunch = false
            }
        }
    }

    fun handleUser() {
        viewModel.fetchUser { user ->
            if(!user.isValidPhone) { // 폰 변경시 세션만료
                sendBroadcast(Intent(FILTER_SESSION_EXPIRED))
            }
            else if(user.isNeedToUpdateGrade()) {
                try {
                    UpdateGradeDialog {
                        DaebakToast.show(this, "저장 완료! 업데이트 되었습니다.")
                    }.apply {
                        isCancelable = false
                    }.show(supportFragmentManager, "updateSchool")
                } catch (e: IllegalStateException) {
                    println("error : ${e}")
                }
            }
        }
    }

    fun showCompleteAndConfirm() {
        val completeDialog = CompleteDialogConfirm(this@LearningTabActivity,
            "업데이트 완료!",
            "메인 > 이름을 눌러서 학년 수정이 가능합니다 :)")
        completeDialog.show()
    }

    fun hideSnackBar() {
        this.snackBar?.dismiss()
    }
    fun onMypageBtnClicked() {
//        replaceTo(mypageFragment, false)
        binding.drawerView.openDrawer(GravityCompat.END)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        hideSnackBar()
    }

    override fun onPageSelected(position: Int) {
        val fragment = tabFragment[position]
        if (!fragment.wasInitUI) {
            fragment.initUI()
            fragment.wasInitUI = true
        }
        fragment.onFragmentSelected()
        CoroutineScope(Dispatchers.IO).launch {
            ServerStatusManager.setServerInspectionDialog(this@LearningTabActivity)
        }
    }

    override fun onBackPressed() {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if(fragment is MyPageBaseFragment) {
                fragment.onBackBtnClicked()
                return
            }
        }
        if (binding.drawerView.isDrawerOpen(GravityCompat.END)) {
            binding.drawerView.closeDrawer(GravityCompat.END)
            return
        }


        if(doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

//        if (binding.viewPager.currentItem == 2 && (tabFragment[2] as BookFragment).isStartWithInitTest) {
//            LogUtils.logEvent(this, user, PulleyEvent.INDUCE, "기기-백버튼", "유형학습화면")
//        }

        doubleBackToExitPressedOnce = true
        DaebakToast.show(this, "뒤로 가기를 한번 더 누르면 종료됩니다.")
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onDrawerStateChanged(newState: Int) {}
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
    override fun onDrawerOpened(drawerView: View) {}
    override fun onDrawerClosed(drawerView: View) {
        val tran = supportFragmentManager?.beginTransaction()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is MyPageBaseFragment) {
                tran?.remove(fragment)
            }

        }
        tran?.commit()
    }

    fun setSelectedTab(index: Int) {
        binding.tabLayout.getTabAt(index)?.select()
    }
    fun setConceptCourseSubjectId(index: Int) {
        (tabFragment[1] as? ConceptCourseFragment)?.let {
            it.viewModel.selectedSubjectId.postValue(index)
        }
    }
    fun launchConceptCourseTutorial() {
        println("asoaso launchConceptCourseTutorial! 1")
        (tabFragment[1] as? ConceptCourseFragment)?.let {
            println("asoaso launchConceptCourseTutorial! 2")
            it.launchTutorialActivity()
        }
    }

    fun moveTo(frag: Fragment, withAnim: Boolean = true) {
        supportFragmentManager.beginTransaction().apply {
            if (withAnim) setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
            add(R.id.container, frag)
            addToBackStack(null)
            commit()
        }
    }

    fun back(frag: Fragment, withAnim: Boolean = true) {
        super.onBackPressed()
//        val tran = supportFragmentManager?.beginTransaction()
//        if (withAnim)
//            tran?.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
//        tran?.remove(frag)
//        tran?.commit()
        mypageFragment.binding.rv?.adapter?.notifyDataSetChanged()
    }

    fun getSelectedTab(): LearningTabFragment {
        val index = binding.tabLayout.selectedTabPosition
        return tabFragment[index]
    }

    fun isSelectedTab(frag: LearningTabFragment): Boolean {
        return frag === getSelectedTab()
    }


    inner class TabAdapter : FragmentPagerAdapter {

        constructor(fragmentManager: FragmentManager) : super(fragmentManager)

        override fun getCount(): Int {
            return tabFragment.size
        }

        override fun getItem(position: Int): Fragment {
            return tabFragment[position]
        }

//        override fun getPageTitle(position: Int): CharSequence? {
//            return when (position) {
//                0 -> "메인"
//                1 -> "분석"
//                2 -> "테스트"
//                3 -> "유형학습"
//                4 -> "모의고사"
//                else -> "오답노트"
//            }
//        }
    }

    fun spyOff() {
        binding.spyBtn.hide()
    }

    fun onSpyBtnClicked() {
        val intent = InitSettingActivity.getIntent(this)
        startActivity(intent)
//        val intent = SolveActivity.getIntent(this)
//        startActivity(intent)
    }
    private fun setScreenOrientation() {
        requestedOrientation = if (resources.getBoolean(R.bool.isPortrait)) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    override fun monitoringTick() {
        var sec = AppUsageMonitor.accumulatedUsageTime
        val min = sec / 60
        sec = sec % 60

        runOnUiThread {
            binding.spyBtn.text =  String.format("%02d", min) + ":" + String.format("%02d", sec)
        }
//        Log.d("MONITOR", "[LEARNING] TICK - ${AppUsageMonitor.accumulatedUsageTime }")
    }

    // 네비게이션 드로워 메뉴에서 키보드 닫기 및 뒤로가기 처리
    var isOutSideClicked = false
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (binding.drawerView.isDrawerOpen(binding.container)) {
                val content = findViewById<View>(R.id.container)
                val contentLocation = IntArray(2)
                content.getLocationOnScreen(contentLocation)
                val rect = Rect(contentLocation[0],
                    contentLocation[1],
                    contentLocation[0] + content.width,
                    contentLocation[1] + content.height)
                val toolbarView = findViewById<View>(R.id.toolbar)
                val toolbarLocation = IntArray(2)
                toolbarView.getLocationOnScreen(toolbarLocation)
                val toolbarViewRect = Rect(toolbarLocation[0],
                    toolbarLocation[1],
                    toolbarLocation[0] + toolbarView.width,
                    toolbarLocation[1] + toolbarView.height)
                isOutSideClicked = !rect.contains(event.x.toInt(), event.y.toInt()) && !toolbarViewRect.contains(event.x.toInt(), event.y.toInt())
            } else {
                return super.dispatchTouchEvent(event)
            }
        } else if (event.action == MotionEvent.ACTION_DOWN && isOutSideClicked) {
            isOutSideClicked = false
            return super.dispatchTouchEvent(event)
        } else if (event.action == MotionEvent.ACTION_MOVE && isOutSideClicked) {
            return super.dispatchTouchEvent(event)
        }
        if (isOutSideClicked) {
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            if (imm.isAcceptingText) {
                val view = this.currentFocus
                if (view != null) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                    binding.drawerView.requestFocus()
                }
            } else {
                onBackPressed()
            }
            return false
        }
        return super.dispatchTouchEvent(event)
    }

    override fun permissionGranted(requestCode: Int) {
        when(requestCode) {
            lessonRequest -> processLesson()
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when(requestCode) {
            lessonRequest -> DaebakToast.show(this, "카메라와 마이크 권한요청을 수락해야지만 과외서비스를 사용할 수 있습니다.")
        }
    }
}

