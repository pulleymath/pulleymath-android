package com.freewheelin.pulley.legacy.activities.learning


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.lesson.LessonActivity
import com.freewheelin.pulley.legacy.activities.auth.InitSettingActivity
import com.freewheelin.pulley.legacy.activities.auth.InitTestActivity.Companion.COMPLETED_SNACK_TEST
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.AnalysisFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.StudyHistoryActivity
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.SnackTestFragment
import com.freewheelin.pulley.legacy.activities.mypage.*
import com.freewheelin.pulley.legacy.bases.*
import com.freewheelin.pulley.legacy.core.manage.*
import com.freewheelin.pulley.legacy.core.manage.UserManager.EVENT_SCHOOL_CHANGE
import com.freewheelin.pulley.legacy.core.manage.UserManager.EVENT_USER_UPDATE
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.legacy.core.manage.UserManager.SCHOOL_TYPE
import com.freewheelin.pulley.databinding.ActivityLearningBinding
import com.freewheelin.pulley.revision2021.activity.AlarmActivity
import com.freewheelin.pulley.revision2021.activity.dialog.UpdateGradeDialog
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.*
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager.IS_START_CHALLENGE_COMPLETED
import com.freewheelin.pulley.revision2023.model.challenge.OnceAppearInfoByStudentId
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity.Companion.PURCHASE_SUCCESS
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.viewmodel.LearningTabViewModel
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.snackBar.SnackBar
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarViewListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.tutorial.Tutor
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseInduceWebViewActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.TeacherUtilityDialog
import com.freewheelin.pulley.revision2023.ui.fragment.WrongNoteStudyFragment


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
    LifecycleEventObserver,
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
    lateinit var userUpdateReceiver: BroadcastReceiver
    lateinit var middleHighChangeReceiver: BroadcastReceiver

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

        fun getIntent(context: Context): Intent {
            return Intent(context, LearningTabActivity::class.java).apply {

            }
        }
    }

    var tabFragment: MutableList<LearningTabFragment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setScreenOrientation() // TODO 모바일 portrait 적용할때 사용

        // TODO 추후 푸시알람 실행시 어딘가로 보내야할수도 있다.
        val target = intent.getStringExtra("target_android")

        tabFragment = mutableListOf(
//            MainFragment.newInstance(),
//            ConceptCourseFragment.newInstance(),
//            PatternStudyFragment.newInstance(),
//            MockExamFragment.newInstance(),
//            SnackTestFragment.newInstance(),
//            WrongNoteStudyFragment.newInstance(),
//            AnalysisFragment.newInstance()
        )

        if (user?.showMainUnivTab == true) {
//            tabFragment.add(AffiliatedTestFragment.newInstance())
        }

        setContentView(binding.root)
        initReceiver()
        initObserve()

        with(binding) {
            vm = viewModel
            lifecycleOwner = this@LearningTabActivity
            updateHeaderColors(schoolType.isMiddle)
            viewPager.adapter = TabAdapter(supportFragmentManager)
            viewPager.setPagingEnabled(false)
            viewPager.offscreenPageLimit = 5

            viewPager.addOnPageChangeListener(this@LearningTabActivity)

            tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
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

            alarmBtn.setOnClickListener {
                val intent = AlarmActivity.getIntent(this@LearningTabActivity)
                startActivity(intent)
            }
            mypageBtn.setOnClickListener {
//                if(viewPager.currentItem == 0 && firstClickCnt < spySeal1)
//                    firstClickCnt += 1
//
//                if(viewPager.currentItem == 2 && firstClickCnt == spySeal1 && secondClickCnt < spySeal2) {
//                    secondClickCnt += 1
//                } else if(viewPager.currentItem == 2){
//                    firstClickCnt = 0
//                    secondClickCnt = 0
//                    thirdClickCnt = 0
//                }
//
//                if(viewPager.currentItem == 4 && firstClickCnt == spySeal1 && secondClickCnt == spySeal2 && thirdClickCnt < spySeal3) {
//                    thirdClickCnt += 1
//                    if(firstClickCnt == spySeal1 && secondClickCnt == spySeal2 && thirdClickCnt == spySeal3) {
//                        isSPYMode = true
//                        mypageFragment.spyOn()
//                        spyBtn.show()
//                    }
//                } else if(viewPager.currentItem == 4) {
//                    firstClickCnt = 0
//                    secondClickCnt = 0
//                    thirdClickCnt = 0
//                }
//
//                if (spyCount == 10) {
//                    isSPYMode = true
//                    mypageFragment.spyOn()
//                    spyBtn.show()
//                    spyCount = 0
//                }

                onMypageBtnClicked()
            }

            if(isSPYMode) {
                spyBtn.show()
            }

            spyBtn.setOnClickListener {
                val intent = PurchaseInduceWebViewActivity.getIntent(this@LearningTabActivity)
                startActivity(intent)
            }
            teacherBtn.setOnClickListener {
                val dialog = TeacherUtilityDialog()
                supportFragmentManager.let { dialog.show(it, "TeacherUtilityDialog") }
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@LearningTabActivity)

            if(referActivity == null) referActivity = this@LearningTabActivity
            registerReceiver(mainEventReceiver, IntentFilter(FILTER_SESSION_EXPIRED))
//            registerReceiver(firebasePushEventReceiver, IntentFilter("FIREBASE-PUSH"))

//            userTest()


            val anim = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            anim.duration = 250
            schoolSwitch.startAnimation(anim)

            showTooltipIfNeedOnAnim(Tutor.TooltipType.middleIntroduceOpening, anim)
        }
    }

    fun showTooltipIfNeedOnAnim(type: Tutor.TooltipType, anim: ScaleAnimation) {
        if(type.isNeedToShow()) {
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Tutor.showToolTipIfNeed(binding.schoolSwitch.binding.middleWrapperCl, type)
                    }, 500)
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
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
                tabMove(tabIndex)
                ChallengeManager.getConceptStudyMoveIntent(courseId).let { intent ->
                    LocalBroadcastManager.getInstance(this@LearningTabActivity).sendBroadcast(intent)
                }
            }
        }
        purchaseReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    viewModel.fetchUser {
                    }
                }
            }
        }
        userUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    viewModel.setPageProgress(true)
                    viewModel.fetchUser {
                        MyApplication.user = it
                        MyApplication.token = it.token
                        viewModel.putFcmToken()
                        (tabFragment.first() as MainFragment).initChallenge()
                        val reConfigureReceiverIntent = Intent(RE_CONFIGURE_UI)
                        LocalBroadcastManager.getInstance(this@LearningTabActivity).sendBroadcast(reConfigureReceiverIntent)
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(700)
                            viewModel.setPageProgress(false)
                            tabMove(0)
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        supportFragmentManager.findFragmentByTag("joinInduceDialog")?.let {
                            (it as? DialogFragment)?.dismiss()
                        }
                        if (binding.drawerView.isDrawerOpen(GravityCompat.END)) {
                            binding.drawerView.closeDrawer(GravityCompat.END)
                        }
                    }
                }
            }
        }
        middleHighChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                intent?.let {
                    val schoolTypeStr = it.getStringExtra(SCHOOL_TYPE) ?: SchoolType.HIGH.name
                    val type = SchoolType.convertFromStr(schoolTypeStr)
                    viewModel.updateSchoolType(type)
                }
            }
        }

        listOf(
            Pair(tabMoveReceiver, IntentFilter(PieceManager.EVENT_MOVE_TAB)),
            Pair(challengeReceiver, IntentFilter(ChallengeManager.MAIN_SCREEN_TAB_MOVE_EVENT)),
            Pair(purchaseReceiver, IntentFilter(PURCHASE_SUCCESS)),
            Pair(userUpdateReceiver, IntentFilter(EVENT_USER_UPDATE)),
            Pair(middleHighChangeReceiver, IntentFilter(EVENT_SCHOOL_CHANGE)),
        ).forEach {
            val receiver = it.first
            val filter = it.second
            LocalBroadcastManager.getInstance(this@LearningTabActivity).registerReceiver(receiver, filter)
        }
    }

    private fun initObserve() {
        viewModel.apply {
            userInRepo.observe(this@LearningTabActivity) { user ->
                user?.let {
                    if (MyApplication.user == null) {
                        MyApplication.user = it
                    } else {
                        MyApplication.user!!.update(it)
                    }

                    MyApplication.user?.run {
                        it.noShowAddOptionalDate = noShowAddOptionalDate
                        it.noShowAddOptionalSubject = noShowAddOptionalSubject
//                        it.excludeSubjectCode = excludeSubjectCode
//                        it.recentSubjectCode = recentSubjectCode
                    }
                    if (it.token.isNotEmpty()) {
                        MyApplication.user = it
                        MyApplication.token = it.token
                    }
                    it.commit("LearningTabAct observe")
                }
            }
            errorAction.observe(this@LearningTabActivity) { type ->
                when(type) {
                    HttpException403, GuestException -> {
                        LogUtils.logEvent(this@LearningTabActivity, user, PulleyEvent.INDUCE, "앱메인화면", "가입유도")
                        showGuestJoinInduceDialog {
                            viewModel.errorStatusReset()
                        }
                    }
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
            schoolType.observe(this@LearningTabActivity) {
                Preferences.schoolType.set(it.name)
                MyApplication.schoolType = it
                binding.schoolSwitch.changeSchoolType(it.isMiddle)
                updateHeaderColors(it.isMiddle)
                updateHeaderItems(it.isHigh)
            }
        }
    }
    private fun updateHeaderItems(isHighSchool: Boolean) {
        binding.apply {
            if (viewPager.currentItem == 3 && !isHighSchool) {
                tabLayout.selectTab(tabLayout.getTabAt(2))
            }

            tabLayout.getTabAt(3)?.let {
                it.view.visibleIf(isHighSchool)
            }
        }
    }
    private fun updateHeaderColors(isMiddle: Boolean) {
        val colors: Array<ColorDrawable> = if (isMiddle) arrayOf(
            ColorDrawable(ContextCompat.getColor(this, R.color.black_200)),
            ColorDrawable(ContextCompat.getColor(this, R.color.white))
        ) else {
            arrayOf(
                ColorDrawable(ContextCompat.getColor(this, R.color.white)),
                ColorDrawable(ContextCompat.getColor(this, R.color.black_200))
            )
        }
        val transitionDrawable = TransitionDrawable(colors)
        binding.apply {
            toolbarBackgroundCl.background = transitionDrawable
            transitionDrawable.startTransition(400)

        }

        binding.tabLayout.setTabTextColors(
            ContextCompat.getColor(this, R.color.gray_700),
            ContextCompat.getColor(this, if (isMiddle) R.color.gray_800 else R.color.white)
        )

    }
    var spyCount = 0

    fun setOnSpyMode() {
//        spyCount += 1
//        if (spyCount > 10) {
//            isSPYMode = true
//            mypageFragment.spyOn()
//            binding.spyBtn.show()
//            spyCount = 0
//        }
    }
    fun setTeacherSpyMode(isShow: Boolean) {
        binding.teacherBtn.visibleIf(isShow)
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

//    private fun userTest() {
//        val url = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/mes/teachers.json"
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val data = Jsoup.connect(url).ignoreContentType(true).execute().body()
//                if (data != null && data.isNotEmpty()) {
//                    Gson().fromJson(data, StudentManagerDialog.StudentManagerResponse::class.java)
//                        .let { runOnUiThread { checkTeachers(it) } }
//                }
//            } catch (e: Exception) {
//                Log.d("마케팅에러", "error=${e.localizedMessage}")
//            }
//        }
//    }

//    private fun checkTeachers(response: StudentManagerDialog.StudentManagerResponse) {
//        val isRegisteredTeacherEmail = response.admins.contains(user?.email)
//
//        if(isRegisteredTeacherEmail) {
//            binding.loadStudentBtn.visibility = View.VISIBLE
//            binding.loadStudentBtn.setOnClickListener {
//                StudentManagerDialog(this, {},{}).show()
//            }
//        }
//    }

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
        showGuestWelcomeMessage()
        saveSignedEmail()
    }

    fun saveSignedEmail() {
        val userEmail = if (user?.serviceType?.isGuestUser == true) {
            ""
        } else {
            user?.email ?: ""
        }
        Preferences.signedEmail.set(userEmail)
    }

    fun showGuestJoinInduceDialog(dismissCallback: () -> Unit) {
        val dialog = JoinInduceForGuestDialog {
            dismissCallback()
            viewModel.errorStatusReset()
        }
        supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
    }
    private fun showGuestWelcomeMessage() {
        if (user?.serviceType?.isGuestUser == true) {
            val appearedInfo = Preferences.guestWelcomeMessageAppeared
            val appearedIds = appearedInfo.studentIds
            val isWelcomeMessageAlreadyAppeared = appearedIds.contains(user?.studentID)
            if (!isWelcomeMessageAlreadyAppeared) {
                val userName = user?.fullName ?: "고객"
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    DaebakToast.show(this@LearningTabActivity, "${userName}님, 풀리수학에 오신것을 환영해요!")
                    setGuestWelcomeMessage(appearedInfo)
                }
            }
        }
    }
    private fun setGuestWelcomeMessage(info: OnceAppearInfoByStudentId) {
        val studentId = user?.studentID ?: ""
        val newList = info.studentIds + listOf(studentId)
        info.studentIds = newList.toSet().toList()
        Preferences.guestWelcomeMessageAppeared = info
    }

    @SuppressLint("CheckResult")
    private fun checkAffiliatedTestExist() {
        if (user?.showMainUnivTab != true) {
            val tabName = binding.tabLayout.getTabAt(binding.tabLayout.tabCount - 1)?.text ?: return
//            if (tabName == AffiliatedTestFragment.newInstance().screenName) {
//                binding.tabLayout.removeTabAt(binding.tabLayout.tabCount - 1)
//                return
//            }
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
            COMPLETED_SNACK_TEST -> {

            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        listOf(tabMoveReceiver, challengeReceiver, purchaseReceiver, userUpdateReceiver, middleHighChangeReceiver)
            .forEach { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
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
            } else {
                if (MyApplication.isAppFirstLaunch) {
                    fetchUser()
                }
                MyApplication.isAppFirstLaunch = false
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                VersionManager.requestVersionInfo(this) { update, _ ->
                    if(update == VersionManager.Required.MAJOR) {
                        DialogUtils.needAppUpgradeDialog(this)
                    } else {
                        if (MyApplication.isAppFirstLaunch) { fetchUser() }
                        MyApplication.isAppFirstLaunch = false
                    }
                }
            }
            else -> {}
        }
    }

    fun fetchUser() {
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

    fun hideSnackBar() {
        this.snackBar?.dismiss()
    }
    fun onMypageBtnClicked() {
        val mypage = supportFragmentManager.fragments.find { it is MyMainPageFragment }
        if (mypage !is MyMainPageFragment) {
            addMyPage(mypageFragment)
        }

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
    fun setConceptCourseAvailableFirstSubject() {
        (tabFragment[1] as? ConceptCourseFragment)?.let {
            it.moveAvailableFirstSubject()
        }
    }

    fun launchConceptCourseTutorial() {
        (tabFragment[1] as? ConceptCourseFragment)?.let {
            it.launchTutorialActivity()
        }
    }

    fun addMyPage(frag: Fragment, withAnim: Boolean = true) {
        supportFragmentManager.beginTransaction().apply {
            if (withAnim) setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
            add(R.id.container, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun removeMyPageTo(frag: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
            remove(frag)
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
        mypageFragment.binding?.rv?.adapter?.notifyDataSetChanged()
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

