package com.freewheelin.pulley.activities.learning

//import com.microsoft.appcenter.AppCenter
//import com.microsoft.appcenter.distribute.Distribute

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.LessonActivity
import com.freewheelin.pulley.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.AnalysisFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.StudyHistoryActivity
import com.freewheelin.pulley.activities.learning.tabFragment.book.BookFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.MainFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_ANALYSIS
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_MOCK
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_TEST
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_UNIT
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity.Companion.RESULT_SNACK_WRONG
import com.freewheelin.pulley.activities.learning.tabFragment.main.marketing.MarketingManager
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.activities.learning.tabFragment.snackTest.SnackTestFragment
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component.WrongNoteFragment
import com.freewheelin.pulley.activities.mypage.MyMainPageFragment
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.activities.mypage.MyPageSettingDialogListener
import com.freewheelin.pulley.activities.mypage.MyStudyInfoSettingDialog
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.manage.*
import com.freewheelin.pulley.core.manage.TestManager.ARG_FROM_INIT_TEST
import com.freewheelin.pulley.dialogs.CompleteDialogConfirm
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.StudentManagerDialog
import com.freewheelin.pulley.bases.*
import com.freewheelin.pulley.core.API_LESSON_DOMAIN
import com.freewheelin.pulley.model.Notice
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.SnackBar.SnackBar
import com.freewheelin.pulley.views.SnackBar.SnackBarView
import com.freewheelin.pulley.views.SnackBar.SnackBarViewListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_learning.*
import kotlinx.android.synthetic.main.dialog_daebak.*
import kotlinx.android.synthetic.main.fragment_my_main_page.*
import kotlinx.android.synthetic.main.fragment_signup_student_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


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

class LearningTabActivity : BaseActivity(),
        ViewPager.OnPageChangeListener,
        DrawerLayout.DrawerListener,
        LifecycleObserver,
        AppUsageMonitorListener,
        LearningTabInterface {

    var snackBar: SnackBar? = null
    var mypageFragment = MyMainPageFragment()
    val tabFragment by lazy { arrayListOf(
            MainFragment.newInstance(),
            AnalysisFragment.newInstance(),
            SnackTestFragment.newInstance(),
            BookFragment.newInstance(),
            MockExamFragment.newInstance(),
            WrongNoteFragment.newInstance()
    )
    }

    var spySeal1 = 1
    var spySeal2 = 3
    var spySeal3 = 2

    var firstClickCnt = 0
    var secondClickCnt = 0
    var thirdClickCnt = 0

    var doubleBackToExitPressedOnce = false

    lateinit var tabMoveReceiver: BroadcastReceiver

    companion object {
        const val LEARNING_MAIN = "LEARNING_MAIN"
        const val LEARNING_TEST = "LEARNING_TEST"
        const val LEARNING_UNIT = "LEARNING_UNIT"
        const val LEARNING_MOCK = "LEARNING_MOCK"
        const val LEARNING_WRONG = "LEARNING_WRONG"
        const val LEARNING_LIST = "LEARNING_LIST"

        const val FILTER_SESSION_EXPIRED = "FILTER_SESSION_EXPIRED"

        fun getIntent(context: Context, isFromInitTest: Boolean = false, needLeading: Boolean = false) : Intent {
            val intent = Intent(context, LearningTabActivity::class.java)
            intent.putExtra(ARG_FROM_INIT_TEST, isFromInitTest)
            intent.putExtra(BookManager.ARG_NEED_LEADING, needLeading)
            return intent
        }

        // for Session expired dialog in Api.class
        var referActivity: Activity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUsageMonitor.startAppUsage()

        try {
            if (savedInstanceState != null) {
                tabFragment[0] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_MAIN) as LearningTabFragment
                tabFragment[1] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_TEST) as LearningTabFragment
                tabFragment[2] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_UNIT) as LearningTabFragment
                tabFragment[3] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_MOCK) as LearningTabFragment
                tabFragment[4] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_WRONG) as LearningTabFragment
                tabFragment[5] = supportFragmentManager.getFragment(savedInstanceState, LEARNING_LIST) as LearningTabFragment
            }
        } catch (e:IllegalStateException) {
            e.printStackTrace()
        }

        setContentView(R.layout.activity_learning)

        requestNotice()

        viewPager.adapter = TabAdapter(supportFragmentManager)

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 5

//        tabLayout.setupWithViewPager(viewPager)

        val isFromInitTest = intent.getBooleanExtra(ARG_FROM_INIT_TEST, false)
        if(isFromInitTest) {
//            viewPager.currentItem = 3
//            (tabFragment[3] as BookFragment).isStartWithInitTest = true
        }

        viewPager.addOnPageChangeListener(this)

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    when(position) {
                        6 -> { startActivity(Intent(baseContext, LessonActivity::class.java)) }
                        else -> { viewPager.currentItem = position }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        // 핸드폰이면 과외 메뉴 숨기기
        if(!isTablet) {
            if(tabLayout.tabCount > 6) tabLayout.removeTabAt(6)
        }

        drawerView.addDrawerListener(this)
        drawerView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        moveTo(mypageFragment, false)
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
            onMypageBtnClicked()
        }

        if(isSPYMode) {
            spyBtn.show()
        }

        spyBtn.setOnClickListener { onSpyBtnClicked() }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        tabMoveReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
//                openStudyHistory("단원 분석", "단원 학습지 만들기")
                intent?.let { intent ->
                    val tabIndex = intent.getIntExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 0)
                    setSelectedTab(tabIndex)
                    when(tabIndex) {
                        1 -> {
                            (tabFragment[tabIndex] as AnalysisFragment).setTodayStudyNewOne()
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(tabMoveReceiver, IntentFilter(PieceManager.EVENT_MOVE_TAB))

        // for Api.class
        if(referActivity == null) referActivity = this
        registerReceiver(mainEventReceiver, IntentFilter(FILTER_SESSION_EXPIRED))

        userTest()
    }

    private fun userTest() {
        val url = "https://pulleymath.com/user_test/teachers_v2.json"
        // 작업하고있던 곳 맞음
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
            loadStudentBtn.visibility = View.VISIBLE
            loadStudentBtn.setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        if (VersionManager.isNeedToUpdate() == true) {
            updateSignView.visibility = View.VISIBLE
        } else {
            updateSignView.visibility = View.GONE
        }
        CoroutineScope(Dispatchers.IO).launch {
            ServerStatusManager.setServerInspectionDialog(this@LearningTabActivity)
        }
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
                setSelectedTab(2)
            }
            RESULT_SNACK_UNIT -> {
                setSelectedTab(3)
            }
            RESULT_SNACK_MOCK -> {
                setSelectedTab(4)
            }
            RESULT_SNACK_WRONG -> {
                setSelectedTab(5)

            }
            RESULT_SNACK_ANALYSIS -> {
                val intent = Intent(this, AnalysisTabActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tabMoveReceiver)
        AppUsageMonitor.finishAppUsage()

        referActivity = null
        unregisterReceiver(mainEventReceiver)

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(tabFragment[0].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_MAIN, tabFragment[0])
        if(tabFragment[1].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_TEST, tabFragment[1])
        if(tabFragment[2].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_UNIT, tabFragment[2])
        if(tabFragment[3].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_MOCK, tabFragment[3])
        if(tabFragment[4].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_WRONG, tabFragment[4])
        if(tabFragment[5].isAdded)
            supportFragmentManager.putFragment(outState, LEARNING_LIST, tabFragment[5])
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
                    intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 1)
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
            } else if(user?.token?.isEmpty() == true) {
                sendBroadcast(Intent(FILTER_SESSION_EXPIRED))
            } else {
                handleUser()
            }
        }
    }

    fun handleUser() {

        user!!.syncMyInfo(this) { user ->

            if(MyApplication.user?.studentType == null) {
                DialogUtils.showNeedInitTestDialog(this)
            }
            else if(!user.isValidPhone || user.isExceedDevice) { // 폰 변경, 기기중복 시 세션만료
                sendBroadcast(Intent(FILTER_SESSION_EXPIRED))
            }
            else if(user.isNeedToUpdateGrade()) {
                val dialog = DialogUtils.updateGradeDialog(this, user)
                dialog.show()
                dialog.leftBtn.setOnClickListener { btn ->
                    dialog.dismiss()
                    user.updateGrade(this@LearningTabActivity, user.grade)
                    DialogUtils.updateGradeNoDialog(this).show()
                }
                dialog.rightBtn.setOnClickListener { btn ->
                    dialog.dismiss()
                    if (user.grade == Grade.BeforeHigh || user.grade == Grade.High_1) {
                        MyStudyInfoSettingDialog(this@LearningTabActivity, user, object : MyPageSettingDialogListener {
                            override fun onModifyCompleted(user: User) {
                                user.updateGrade(this@LearningTabActivity, user.grade) { showCompleteAndConfirm() }
                            }
                        }).show()
                    } else {
                        user.updateGrade(this@LearningTabActivity, user.grade.nextGrade) { showCompleteAndConfirm() }
                    }
                }
            }
            /*
                preference 반영
             */
            else {
                MyApplication.user = user
                user.commit("LearningTab handleUser")
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
        drawerView.openDrawer(GravityCompat.END)
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
        if (drawerView.isDrawerOpen(GravityCompat.END)) {
            drawerView.closeDrawer(GravityCompat.END)
            return
        }


        if(doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        if (viewPager.currentItem == 3 && (tabFragment[3] as BookFragment).isStartWithInitTest == true) {
            LogUtils.logEvent(this, user, PulleyEvent.INDUCE, "기기-백버튼", "유형학습화면")
        }

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
        tabLayout.getTabAt(index)?.select()
    }

    private fun requestNotice() {
        API_V1.getNoticeList().enqueue(object : Callback<Template<List<Notice>>> {
            override fun onFailure(call: Call<Template<List<Notice>>>, t: Throwable) {}

            override fun onResponse(call: Call<Template<List<Notice>>>, response: Response<Template<List<Notice>>>) {
                NoticeManager.notices = response.body()?.data ?: listOf()
            }
        })
    }

    fun moveTo(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran?.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
        tran.add(R.id.container, frag)
        tran.addToBackStack(null)
        tran.commit()
    }

    fun back(frag: Fragment, withAnim: Boolean = true) {
        super.onBackPressed()
//        val tran = supportFragmentManager?.beginTransaction()
//        if (withAnim)
//            tran?.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
//        tran?.remove(frag)
//        tran?.commit()
        mypageFragment.rv?.adapter?.notifyDataSetChanged()
    }

    fun getSelectedTab(): LearningTabFragment {
        val index = tabLayout.selectedTabPosition
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

    private fun isNeedToRushDialog(): Boolean {
        return user!!.isExpiredUser() && Preferences.isAvailableRushDialog.get()
    }


    fun spyOff() {
        spyBtn.hide()
    }

    fun onSpyBtnClicked() {
        val intent = InitSettingActivity.getIntent(this)
        startActivity(intent)
//        val intent = SolveActivity.getIntent(this)
//        startActivity(intent)
    }

    override fun monitoringTick() {
        var sec = AppUsageMonitor.accumulatedUsageTime
        val min = sec / 60
        sec = sec % 60

        runOnUiThread {
            spyBtn.text =  String.format("%02d", min) + ":" + String.format("%02d", sec)
        }
//        Log.d("MONITOR", "[LEARNING] TICK - ${AppUsageMonitor.accumulatedUsageTime }")
    }

    override fun openMarketingDialog(mainProfile: MainProfile) {
        MarketingManager.setMarketingBanner(this, mainProfile)
    }

    // 네비게이션 드로워 메뉴에서 키보드 닫기 및 뒤로가기 처리
    var isOutSideClicked = false
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (drawerView.isDrawerOpen(container)) {
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
                    drawerView.requestFocus()
                }
            } else {
                onBackPressed()
            }
            return false
        }
        return super.dispatchTouchEvent(event)
    }
}

interface LearningTabInterface {
    fun openMarketingDialog(mainProfile: MainProfile)
}

