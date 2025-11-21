package com.freewheelin.pulley.revision2023.ui.activity

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityMainBinding
import com.freewheelin.pulley.legacy.activities.auth.InitSettingCompleteActivity
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.AnalysisFragment
import com.freewheelin.pulley.legacy.activities.lesson.LessonActivity
import com.freewheelin.pulley.legacy.activities.mypage.MyMainPageFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.legacy.bases.*
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.assessmentDesignSkin
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.core.manage.*
import com.freewheelin.pulley.legacy.core.tutorial.Tutor
import com.freewheelin.pulley.legacy.model.SignInChannel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.snackBar.SnackBar
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarViewListener
import com.freewheelin.pulley.revision2021.activity.AlarmActivity
import com.freewheelin.pulley.revision2021.activity.dialog.UpdateGradeDialog
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.AssessmentDesignSkin
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.model.challenge.OnceAppearInfoByStudentId
import com.freewheelin.pulley.revision2023.ui.adapter.SchoolSpinnerAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.SpyDialog
import com.freewheelin.pulley.revision2023.ui.fragment.AssessmentFragment
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import com.freewheelin.pulley.revision2023.ui.fragment.MainLessonFragment
import com.freewheelin.pulley.revision2023.ui.fragment.MainTabFragment
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.freewheelin.pulley.revision2023.utils.StringUtils
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import com.freewheelin.pulley.revision2023.viewmodel.MainActViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : PermissionActivity(),
    LifecycleEventObserver, AppUsageMonitorListener {

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false)
    }
    private val viewModel: MainActViewModel by viewModels()
    private lateinit var getResult: ActivityResultLauncher<Intent>

    val lessonRequest = 1001
    companion object {
        const val lessonFinished = 200

        var referActivity: Activity? = null
    }
    private var tabFragments: MutableList<MainTabFragment> = mutableListOf()

    lateinit var tabMoveReceiver: BroadcastReceiver
    lateinit var challengeReceiver: BroadcastReceiver
    lateinit var challengeUpdateReceiver: BroadcastReceiver
    lateinit var purchaseReceiver: BroadcastReceiver
    lateinit var userUpdateReceiver: BroadcastReceiver
    lateinit var middleHighChangeReceiver: BroadcastReceiver
    lateinit var mainEventReceiver: BroadcastReceiver

    val myPageFragment by lazy {
        viewModel.fetchMainProfile()
        MyMainPageFragment()
    }
    var snackBar: SnackBar? = null

    var doubleBackToExitPressedOnce = false
    var exitDialogContinualShowCount = 0
    private fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            tabFragments.forEach { it.resetHeaderControlParams() }
            binding.headerCl.showExpandVertical(true)
            val fragments = supportFragmentManager.fragments
            val myPageFrags = fragments.filterIsInstance<MyPageBaseFragment>()
            if (myPageFrags.isNotEmpty()) {
                myPageFrags.last().onBackBtnClicked()
                return@addCallback
            } else if (binding.rootDl.isDrawerOpen(GravityCompat.END)) {
                viewModel.toggleDrawer()
                return@addCallback
            } else if (binding.chatBotBgCl?.isVisible == true) {
                binding.chatBotBgCl?.visibleIf(false)
                binding.chatBotCv?.visibleIf(false)
                binding.chatBotBtn?.startLongClickDescAnim()
                return@addCallback
            }

            if (user?.serviceType?.isGuestUser == true) {
                if (exitDialogContinualShowCount > 2) {
                    finish()
                    return@addCallback
                }
                exitDialogContinualShowCount ++

                DialogUtils.showMainDontExitDialog(this@MainActivity, {
                    finish()
                }, {
                    exitDialogContinualShowCount = 0
                })
            } else {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return@addCallback
                }
                doubleBackToExitPressedOnce = true

                DaebakToast.show(this@MainActivity, "뒤로 가기를 한번 더 누르면 종료됩니다.")
                Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setScreenOrientation()
        setContentView(binding.root)
        initReceiver()
        initObserve()
        initActivityResult()
        initTabFragment()
        addBackBtnCallback()
        initSchoolSpinner()
        if(referActivity == null) referActivity = this@MainActivity

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@MainActivity
            mainTl.setViewModel(viewModel)

            rootDl.addDrawerListener(object: DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    val tran = supportFragmentManager.beginTransaction()
                    for (fragment in supportFragmentManager.fragments) {
                        if (fragment is MyPageBaseFragment) {
                            tran.remove(fragment)
                        }
                    }
                    tran.commit()
                }
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {}
            })
            addMyPage(myPageFragment, false)
            vp.adapter = MainPagerAdapter(tabFragments, supportFragmentManager, lifecycle)
            vp.isUserInputEnabled = false
            vp.offscreenPageLimit = 5
            vp.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    snackBar?.dismiss()
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    CoroutineScope(Dispatchers.IO).launch {
                        ServerStatusManager.setServerInspectionDialog(this@MainActivity)
                    }
                }
            })
            mainTl.addOnTabListener { position, newType, prevPosition, prevType ->
                viewModel.prevTab = Pair(prevType, prevPosition)
                when (newType) {
                    MainTab.메인 -> {
//                        checkStartChallengeFinish()
                        vp.setCurrentItem(position, 200)
                    }
                    MainTab.과외 -> openLesson()
                    else -> {
                        vp.setCurrentItem(position, 200)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(400)
                    if (tabFragments.size > position) {
                        tabFragments[position].onFragmentSelected()
                    }
                }
            }
            mainTl.selectTap(0, MainTab.메인)

            alarmBtn.setOnClickListener {

                val intent = AlarmActivity.getIntent(this@MainActivity)
                startActivity(intent)
            }
            setSpy()
            initChatBot()

        }
    }

    fun initChatBot() {
        binding.apply {
            chatBotBtn?.setInitPosition()
            chatBotBtn?.setOnClickListener {
                if (chatBotBgCl?.isVisible === true) {
                    chatBotBgCl?.visibleIf(false)
                    chatBotBtn?.startLongClickDescAnim()
                } else {
                    val url = Network.webAppUrl + "/ottway?token=${token}&uri=chat-bot"
                    binding.webView?.loadUrl(url)
                    chatBotBgCl?.visibleIf(true)
                }
            }
            webView?.let {
                it.addJavascriptInterface(ChatBotClientClickEventListener (
                    onCloseListener = {
                        runOnUiThread {
                            chatBotBgCl?.visibleIf(false)
                            chatBotBtn?.startLongClickDescAnim()
                        }
                    }, errorCloseListener = {
                        runOnUiThread {
                            chatBotBgCl?.visibleIf(false)
                        }
                    }
                ), "android")

                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
            }

        }

    }

    fun showTooltipIfNeedOnAnim(type: Tutor.TooltipType, anim: ScaleAnimation) {
        if(type.isNeedToShow()) {
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        Tutor.showToolTipIfNeed(binding.schoolSwitch.binding.middleWrapperCl, type)
//                    }, 500)
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
        }
    }

    override fun onResume() {
        super.onResume()
        AppUsageMonitor.startAppUsage()
        viewModel.fetchUserChallenges()
        viewModel.fetchAssessmentGroupMetadata()
        binding.updateSignView.visibleIf(VersionManager.isNeedToUpdate() == true)
        checkNewAlarm()
        CoroutineScope(Dispatchers.IO).launch {
            ServerStatusManager.setServerInspectionDialog(this@MainActivity)
        }
        showGuestWelcomeMessage()
        viewModel.syncSchoolType()
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
                    DaebakToast.show(this@MainActivity, "${userName}님, 풀리수학에 오신것을 환영해요!")
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
    private fun saveSignedEmail() {
        val userEmail = if (MyApplication.user?.serviceType?.isGuestUser == true) {
            ""
        } else {
            MyApplication.user?.accountEmail ?: ""
        }
        val nextLoginPresentedEmail = if (MyApplication.user?.signInChannel == SignInChannel.PULLEY) {
            userEmail
        } else {
            ""
        }
        Preferences.signedEmail.set(nextLoginPresentedEmail)
    }

    private fun checkNewAlarm() {
        viewModel.checkNewAlarm { hasNewAlarm ->
            CoroutineScope(Dispatchers.Main).launch {
                binding.updateAlarmView.visibleIf(hasNewAlarm)
            }
        }
    }

    private fun setSpy() {
        binding.apply {
            setSpyEmoji()
            spyBtn.setOnClickListener {
                SpyDialog().apply {

                }.show(supportFragmentManager, "SpyDialog")
            }
        }
    }
    private fun setSpyEmoji() {
        val emoji = StringUtils.getEmojiByUnicode(0x1F977)
        binding.spyBtn.text = emoji
    }

    private lateinit var schoolSpinnerAdapter: SchoolSpinnerAdapter
    private val spinnerItems = mutableListOf<String>()
    private fun initSchoolSpinner() {
        schoolSpinnerAdapter = SchoolSpinnerAdapter(this, R.layout.item_school_spinner_textview, spinnerItems)
        binding.schoolSpinnerAdapter = schoolSpinnerAdapter
    }
    private fun setSchoolSpinner() {
        val currentUser = viewModel.user.value
        val newSpinnerItems: List<String>

        binding.schoolSpinner.visibility = View.VISIBLE
        if (spinnerItems.size > 0) return
        if (currentUser?.affiliationInfo?.showUiBySchoolLevel == true) {
            when (currentUser.affiliationInfo?.institutionType) {
                "ELEMENTARY" -> {
                    binding.schoolSpinner.visibility = View.INVISIBLE
                    newSpinnerItems = emptyList()
                }
                "MIDDLE" -> {
                    newSpinnerItems = listOf(SchoolType.ELEMENTARY.inKorean, SchoolType.MIDDLE.inKorean)
                }
                else -> {
                    newSpinnerItems = listOf(SchoolType.ELEMENTARY.inKorean, SchoolType.MIDDLE.inKorean, SchoolType.HIGH.inKorean)
                }
            }
        } else {
            newSpinnerItems = listOf(SchoolType.ELEMENTARY.inKorean, SchoolType.MIDDLE.inKorean, SchoolType.HIGH.inKorean)
        }
        if (spinnerItems != newSpinnerItems) {
            spinnerItems.clear()
            spinnerItems.addAll(newSpinnerItems)
            schoolSpinnerAdapter.notifyDataSetChanged()
            if (newSpinnerItems.isNotEmpty()) {
                val currentUserSchoolType = SchoolType.convertFromStr(currentUser?.affiliationInfo?.institutionType ?: SchoolType.ELEMENTARY.name)
                binding.schoolSpinner.setSelection(currentUserSchoolType.mainSpinnerPosition)
            }
        }

    }

    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                lessonFinished -> {
                    val prevPosition = viewModel.prevTab.second
                    val prevTab = viewModel.prevTab.first
                    binding.mainTl.selectTap(prevPosition, prevTab)
                }
            }
        }
    }
    fun fetchUser() {
        viewModel.fetchUser { user ->
            if(!user.isValidPhone) { // 폰 변경, 기기중복 시 세션만료
                sendBroadcast(Intent(UserManager.FILTER_SESSION_EXPIRED))
            }
            else if(user.canUpdateGrade) {
                try {
                    val dialog = UpdateGradeDialog.newInstance()
                    dialog.callback = {
                        sendBroadcast(Intent(UserManager.EVENT_USER_MODIFYING))
                        DaebakToast.show(this, "저장 완료! 업데이트 되었습니다.")
                    }
                    dialog.isCancelable = false
                    dialog.show(supportFragmentManager, "updateSchool")
                } catch (e: IllegalStateException) {
                    println("error : ${e}")
                }
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

    private fun initTabFragment() {
        viewModel.fetchAssessmentExamList()
        tabFragments = if (isTablet) {
            mutableListOf(
                MainFragment.newInstance(),
                ConceptCourseFragment.newInstance(),
                PatternStudyFragment.newInstance(),
                AnalysisFragment.newInstance(),
                MainLessonFragment.newInstance(),
                AssessmentFragment.newInstance()
            )
        } else {
            mutableListOf(
                MainFragment.newInstance(),
                ConceptCourseFragment.newInstance(),
                PatternStudyFragment.newInstance(),
                AnalysisFragment.newInstance(),
                AssessmentFragment.newInstance()
            )
        }
    }

    fun initReceiver () {
        tabMoveReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                intent?.let { it ->
                    binding.headerCl.showExpandVertical(true)
                    val tabIndex = it.getIntExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 0)
                    tabMove(tabIndex)
                    when(tabIndex) {
                        MainTab.문제풀이.indexOnTablet -> {
                            if (isTablet) {
                                val actionName = it.getStringExtra(PieceManager.EVENT_ADDITIONAL_ACTION) ?: ""
                                when (actionName) {
                                    "WRONG_NOTE" -> {
                                        p0?.let { ctx ->
                                            WrongNoteActivity.getIntent(ctx).let {
                                                getResult.launch(it)
                                            }
                                        }
                                    }
                                    "PULLEY_MATH_BOOKS" -> {
                                        p0?.let { ctx ->
                                            PulleyMathBooksActivity.getIntent(ctx).let {
                                                getResult.launch(it)
                                            }
                                        }
                                    }
                                    SchoolType.HIGH.name -> viewModel.updateSchoolSpinnerPosition(SchoolType.HIGH)
                                    SchoolType.MIDDLE.name -> viewModel.updateSchoolSpinnerPosition(SchoolType.MIDDLE)
                                    SchoolType.ELEMENTARY.name -> viewModel.updateSchoolSpinnerPosition(SchoolType.ELEMENTARY)
                                }
                            }
                        }
                        6 -> {
                            (tabFragments[tabIndex] as AnalysisFragment).setTodayStudyNewOne()
                        }
                    }

                    val wantScroll = it.getBooleanExtra(PieceManager.EVENT_SCROLL, false)
                    if (!wantScroll) return

                    when {
                        it.getBooleanExtra(PieceManager.EVENT_SCROLL_UNIT_TOTAL_LABEL, false) -> {
//                            val subject = intent.getStringExtra(PieceManager.EVENT_FILTER) ?: return
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
        challengeUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModel.fetchUserChallenges()
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
                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                }
            }
            fun tabMoveAndSendConceptBroadcast(tabIndex: Int, courseId: Int?) {
                tabMove(tabIndex)
                ChallengeManager.getConceptStudyMoveIntent(courseId).let { intent ->
                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
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
                intent?.let { intent ->
                    viewModel.setPageProgress(true)
                    viewModel.fetchUser {
                        MyApplication.user = it
                        MyApplication.token = it.token
                        viewModel.putFcmToken()
                        (tabFragments.first() as MainFragment).initChallenge()
                        val reConfigureReceiverIntent = Intent(UserManager.RE_CONFIGURE_UI)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(reConfigureReceiverIntent)

                        if (tabFragments.map { it.type }.contains(MainTab.대학).not()) {
                            tabFragments.add(AssessmentFragment.newInstance())
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            delay(700)
                            viewModel.setPageProgress(false)
                            tabMove(0)
                            binding.mainTl.initOnDevice()
                        }
                        val signupJustAMomentAgoFromGuestUser = intent.getBooleanExtra(InitSettingCompleteActivity.IS_GUEST_USER, false)
                        if (signupJustAMomentAgoFromGuestUser) {
                            (tabFragments.first() as MainFragment).firstHeaderMove()
                        }

                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        supportFragmentManager.findFragmentByTag("joinInduceDialog")?.let {

                            (it as? DialogFragment)?.dismiss()
                        }
                        if (binding.rootDl.isDrawerOpen(GravityCompat.END)) {
                            binding.rootDl.closeDrawer(GravityCompat.END)
                        }
                    }
                }
            }
        }
        middleHighChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                intent?.let {
                    val schoolTypeStr = it.getStringExtra(UserManager.SCHOOL_TYPE) ?: SchoolType.HIGH.name
                    val type = SchoolType.convertFromStr(schoolTypeStr)
                    viewModel.updateSchoolType(type)
                }
            }
        }
        mainEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(javaClass.simpleName, "Api path intent=${intent}")
                user?.token = ""
                user?.commit("DialogUtils.expiredSessionDialog")
                DialogUtils.expiredSessionDialog(this@MainActivity)
            }
        }
        listOf(
            Pair(tabMoveReceiver, IntentFilter(PieceManager.EVENT_MOVE_TAB)),
            Pair(challengeReceiver, IntentFilter(ChallengeManager.MAIN_SCREEN_TAB_MOVE_EVENT)),
            Pair(challengeUpdateReceiver, IntentFilter(ChallengeManager.CHALLENGE_UPDATE)),
            Pair(purchaseReceiver, IntentFilter(PurchaseWebViewActivity.PURCHASE_SUCCESS)),
            Pair(userUpdateReceiver, IntentFilter(UserManager.EVENT_USER_UPDATE)),
            Pair(middleHighChangeReceiver, IntentFilter(UserManager.EVENT_SCHOOL_CHANGE)),
            Pair(mainEventReceiver, IntentFilter(UserManager.FILTER_SESSION_EXPIRED)),
        ).forEach {
            val receiver = it.first
            val filter = it.second
            LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(receiver, filter)
        }
    }

    fun tabMove(index: Int) {
        binding.mainTl.selectTap(index)
    }
    private fun initObserve() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        viewModel.apply {
            assessmentMetadata.observe(this@MainActivity) {
                val _skin = AssessmentDesignSkin.convertGroupCodeToSkin(it?.group_code)
                assessmentDesignSkin = _skin
                binding.mainTl.univTabName(_skin.univTabText)

            }
            assessmentExamGroup.observe(this@MainActivity) {
                binding.mainTl.univTabVisibility(it.isNotEmpty())
            }
            user.observe(this@MainActivity) { user ->
                user?.let {
                    MyApplication.user = it

                    if (it.affiliationInfo?.showUiBySchoolLevel == true) {
                        when (it.affiliationInfo?.institutionType) {
                            "ELEMENTARY" -> {
                                MyApplication.schoolType = SchoolType.ELEMENTARY
                            }
                            "MIDDLE" -> {
                                MyApplication.schoolType = SchoolType.MIDDLE
                            }
                            else -> { MyApplication.schoolType = SchoolType.HIGH }
                        }
                    }
                    saveSignedEmail()
                    if (it.token.isNotEmpty()) {
                        MyApplication.token = it.token
                    }
                    it.commit("LearningTabAct observe")
                    setSchoolSpinner()
                }
            }
            showDrawer.observe(this@MainActivity) { show ->
                binding.apply {
                    if (show) {
                        rootDl.openDrawer(GravityCompat.END)
                        binding.rootDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
                    } else if (rootDl.isDrawerOpen(GravityCompat.END)) {
                        rootDl.closeDrawer(GravityCompat.END)
                    }
                }
            }
            showSpy.observe(this@MainActivity) { show ->
//                myPageFragment.setSpyMode(show)
                isSPYMode = show
            }
            errorAction.observe(this@MainActivity) { type ->
                when(type) {
                    CoroutineExceptionType.HttpException403, CoroutineExceptionType.GuestException -> {
                        LogUtils.logEvent(this@MainActivity, user.value, PulleyEvent.INDUCE, "앱메인화면", "가입유도")
                        showGuestJoinInduceDialog {
                            viewModel.errorStatusReset()
                        }
                    }
                    CoroutineExceptionType.NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
            schoolType.observe(this@MainActivity) {
                Preferences.schoolType.set(it.name)
                MyApplication.schoolType = it
                updateHeaderItems(it)
                updateHeaderColors(it)
            }
            schoolSpinnerPosition.observe(this@MainActivity) {
                if (it == null) return@observe
                val convertedType = SchoolType.convertSwitchPositionToType(it)
                viewModel.updateSchoolType(convertedType)
            }
        }
    }
    private fun updateHeaderItems(type: SchoolType) {
        binding.apply {
            mainTl.updateSchoolType(type)
            val alarmBtnColor = when (type) {
                SchoolType.ELEMENTARY -> R.color.purple_200
                SchoolType.MIDDLE -> R.color.gray_500
                SchoolType.HIGH -> R.color.gray_700
                SchoolType.UNIVERSITY -> R.color.gray_700
            }
            alarmBtn.setColorFilter(ContextCompat.getColor(this@MainActivity, alarmBtnColor))
        }
    }

    private fun updateHeaderColors(type: SchoolType) {
        binding.mainTl.setTabTextColorsBySchoolType(type)
        val transitionDrawable = binding.mainTl.makeHeaderTransitionDrawable(type)

        binding.apply {
            headerCl.background = transitionDrawable
            transitionDrawable.startTransition(400)
        }
    }

    fun addMyPage(frag: Fragment, withAnim: Boolean = true) {
        supportFragmentManager.beginTransaction().apply {
            if (withAnim) setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
            add(R.id.drawerContainerFl, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun setDrawerLockMode(flag: Boolean) {
        if (flag) {
            binding.rootDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
        } else {
            binding.rootDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }
    fun backMyPage(frag: Fragment, withAnim: Boolean = true) {
        removeSettingFragment(frag)
        myPageFragment.binding.myPageMenuRv.adapter?.notifyDataSetChanged()
    }

    private fun removeSettingFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
            remove(frag)
            commit()
        }
    }
    fun checkStartChallengeFinish() {
        val isStartChallengeCompleted = intent.getBooleanExtra(ChallengeManager.IS_START_CHALLENGE_COMPLETED, false)
        if (isStartChallengeCompleted || viewModel.showStartChallengeFinishEffect()) {
            tabFragments.find { it.type == MainTab.메인 }?.let { frag ->
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    (frag as MainFragment).showStartChallengeCompletedGuide()
                    viewModel.updateChallengeFinishFlag()
                }

            }
        }
    }

    fun showSnackBar(text: String, buttonText: String, action: (() -> Unit)? = null) {
        if (snackBar?.isShowing == true) { snackBar?.dismiss() }

        val newSnackBar = snackBar ?: SnackBar(this)
        newSnackBar.setText(text, buttonText)
        newSnackBar.setSnackBarViewListener(object : SnackBarViewListener {
            override fun onXBtnClicked(view: SnackBarView) {
                newSnackBar.dismiss()
            }

            override fun onActionBtnClicked(view: SnackBarView) {
                if (action == null) {
                    val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                    intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 6)
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
                } else {
                    action()
                }
                newSnackBar.dismiss()

            }
        })

        snackBar = newSnackBar
        newSnackBar.show()
    }


    private val lessonPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS)

    private fun openLesson() {
        requirePermissions(lessonPermissions, lessonRequest)
    }
    private fun processLesson() {
        getResult.launch(Intent(baseContext, LessonActivity::class.java))
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

    inner class MainPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    override fun monitoringTick() {
        var sec = AppUsageMonitor.accumulatedUsageTime
        val min = sec / 60
        sec %= 60
        println("asoaso monitoringf Tick!? min :${min}, sec: ${sec}")

//        runOnUiThread {
//            binding.spyBtn.text =  String.format("%02d", min) + ":" + String.format("%02d", sec)
//        }
    }
    private fun setScreenOrientation() {
        requestedOrientation = if (resources.getBoolean(R.bool.isPortrait)) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }
    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)

        listOf(tabMoveReceiver, challengeUpdateReceiver, challengeReceiver, purchaseReceiver, userUpdateReceiver, middleHighChangeReceiver)
            .forEach { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
        AppUsageMonitor.finishAppUsage()
        WebStorage.getInstance().deleteAllData()

        super.onDestroy()
    }
    override fun onStop() {
        super.onStop()
        viewModel.clearCompositeDisposable()
    }
    fun hideSnackBar() {
        this.snackBar?.dismiss()
    }
    fun setConceptCourseTutorial() {
        tabFragments.find { it.type == MainTab.개념 }?.let { frag ->
            (frag as ConceptCourseFragment).moveTutorial()
        }
    }
    fun spyOff() {
        viewModel.showSpy.postValue(false)
    }
    fun setOnSpyMode() {
        viewModel.showSpy.postValue(true)
    }
    var isOutSideClicked = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (binding.rootDl.isDrawerOpen(binding.drawerContainerFl)) {
                val content = findViewById<View>(R.id.drawerContainerFl)
                val contentLocation = IntArray(2)
                content.getLocationOnScreen(contentLocation)
                val rect = Rect(contentLocation[0],
                    contentLocation[1],
                    contentLocation[0] + content.width,
                    contentLocation[1] + content.height)

                val toolbarView = findViewById<View>(R.id.headerCl)
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
                    binding.rootDl.requestFocus()
                }
            } else {
                onBackPressed()
            }
            return false
        }
        return super.dispatchTouchEvent(event)
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
    fun launchConceptCourseTutorial() {
        (tabFragments[1] as? ConceptCourseFragment)?.launchTutorialActivity()
    }

    fun isSelectedTab(frag: MainTabFragment): Boolean {
        return frag.type == binding.mainTl.getCurrentTab()
    }

    fun showTabHeader(value: Boolean) {
        binding.headerCl.showExpandVertical(value)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (binding.mainTl.getCurrentTab() != MainTab.대학) {
            return super.onKeyDown(keyCode, event)
        }

        val tabIndex = if (isTablet) MainTab.대학.indexOnTablet else MainTab.대학.indexOnMobile
        (tabFragments[tabIndex] as? AssessmentFragment)?.let { frag ->
            val binding = frag.binding

            println("host check =========> ${binding.webView.url}")

            val univCommunityHost = when (Preferences.onServerAPI.get()) {
                Network.Server.live.toString() -> "https://pulleymath.com/community?is_mobile"
                Network.Server.staging.toString() -> "https://dev.pulleymath.com/community?is_mobile"
                Network.Server.dev.toString() -> "https://dev.pulleymath.com/community?is_mobile"
                else -> "https://pulleymath.com"
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return super.onKeyDown(keyCode, event)
            } else if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.url?.startsWith(univCommunityHost) == true) {
                return super.onKeyDown(keyCode, event)
            } else if (keyCode == KeyEvent.KEYCODE_BACK)  {
                binding.webView.goBack()
            }
        }


        return true
    }
}