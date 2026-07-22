package com.freewheelin.pulley.revision2023.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityWorkbookListBinding
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.dialogs.CustomizeBookDialog
import com.freewheelin.pulley.legacy.dialogs.CustomizeBookDialogListener
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2021.utils.getStatusBarHeight
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.adapter.BookFilterAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.viewmodel.WorkbookListViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.GridMarginDecoration
import com.freewheelin.pulley.legacy.views.snackBar.SnackBar
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarViewListener
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkbookListActivity : AppCompatActivity(), LifecycleObserver, PlanListenerV2,
    CustomizeBookDialogListener {
    val binding: ActivityWorkbookListBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_workbook_list, null, false)
    }
    private val viewModel: WorkbookListViewModel by viewModels()
//    private val totalPlanAdapter = PatternStudyTotalPlanAdapter (this, null, null)
    lateinit var planAdapter: PatternStudyMyPlanAdapter
    lateinit var userUpdateReceiver: BroadcastReceiver
    lateinit var filterAdapter: BookFilterAdapter

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, WorkbookListActivity::class.java).apply {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            vm = viewModel
            lifecycleOwner = this@WorkbookListActivity
            initReceiver()
            initAdapter()
            initUI()
            initChatBot()
            addBackBtnCallback()
            btnBack.setOnClickListener {
                finish()
            }
            createWorkbookCl.setOnClickListener {
                LogUtils.logEvent(this@WorkbookListActivity, user, PulleyEvent.BUTTON_CLICK,"워크북","워크북만들기")

                if (user?.serviceType?.isGuestUser == true) {
                    LogUtils.logEvent(this@WorkbookListActivity, user, PulleyEvent.INDUCE,"워크북","가입유도")
                    val dialog = JoinInduceForGuestDialog().apply {
                        updateDismissCallback {
                            viewModel.errorStatusReset()
                        }
                    }
                    supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
                } else {
                    val isStartChallengeInProgress = viewModel.showStartChallengeStamp.value == true
                    val curriculumSubjects = viewModel.subjects.value ?: listOf()
                    CustomizeBookDialog(this@WorkbookListActivity, isStartChallengeInProgress, this@WorkbookListActivity).show()
                }
            }

//            DialogUtils.confirmDialog(this@WorkbookListActivity, "[테스트]구독중이 아닙니다.", "열려라 참깨")

        }
        viewModel.apply {
            fetchCurriculumSubjects()
            playTotalLoadingView.observe(this@WorkbookListActivity) {
                if (it) {
                    binding.totalLoadingView.playAnimation()
                } else {
                    binding.totalLoadingView.cancelAnimation()

                }
            }
            customBooks.observe(this@WorkbookListActivity) {
                planAdapter.submitList(it)
                if (it.isEmpty()) {
                    binding.totalEmptyContainer.show(300)
                    showEmptyContainer.postValue(true)
                } else {
                    val rvAnimController = AnimationUtils.loadLayoutAnimation(
                        this@WorkbookListActivity,
                        R.anim.recyclerview_grid_layout_animation
                    )
                    binding.totalRv.layoutAnimation = rvAnimController
                    binding.totalRv.scheduleLayoutAnimation()
                    showEmptyContainer.postValue(false)
                }

                showDummyBottomView.postValue(it.size < 7)
                showTotalLoadingView.postValue(false)
                playTotalLoadingView.postValue(false)
                showTotalPlanCover.postValue(false)
            }
            joinedChallengeList.observe(this@WorkbookListActivity) {
                val isWorkbookStartChallengeInProgress = it.find { it.startChallenge?.isWorkbooksInProgress == true } != null
                showStartChallengeStamp.postValue(isWorkbookStartChallengeInProgress)
            }
            filterElements.observe(this@WorkbookListActivity) {
                this@WorkbookListActivity.filterAdapter.submitList(it)
                fetchCustomBooksOnFilters(it)

            }
            checkActionOfStartChallenge {
                val guideDialog = ChallengeGuideManager.getStartGuideMission4()
                supportFragmentManager.let { guideDialog.show(it, "getStartGuideMission3") }
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
        userUpdateReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                println("asoaso userUpdateReceiver!")
                intent?.let {
                    finish()
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(userUpdateReceiver, IntentFilter(
            UserManager.EVENT_USER_UPDATE)
        )

    }
    fun initAdapter() {
        binding.apply {
            val spanCount = if (isTablet) 4 else 2
            totalRv.layoutManager = GridLayoutManager(this@WorkbookListActivity, spanCount)
            planAdapter = PatternStudyMyPlanAdapter (this@WorkbookListActivity, listOf(ActionType.pin), PatternStudyMyPlanAdapter.OriginType.Workbook)
            totalRv.adapter = planAdapter
            val columnSpace = resources.getDimension(R.dimen.dp24).toInt()
            totalRv.addItemDecoration(GridMarginDecoration(16.toPx(), columnSpace, spanCount))
            totalRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when(newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> { scrollLogEvent() }
                    }
                }
            })
            viewModel.adapter = planAdapter

            filterAdapter = BookFilterAdapter (object : BookFilterItemListener {
                override fun onToggle(isChecked: Boolean) {
                    viewModel.switchCheckedContainPin(isChecked)
                }

                override fun onFilterItemClick(item: BookFilterElement) {
                    viewModel.showEmptyContainer.postValue(false)
                    viewModel.showRecyclerView.postValue(false)
                    viewModel.showTotalLoadingView.postValue(true)
                    viewModel.playTotalLoadingView.postValue(true)
                    viewModel.onFilterItemClick(item)
                }

                override fun onCalendar() {}
            })
            val filterSpanCount = 6
            filterRv.layoutManager = GridLayoutManager(this@WorkbookListActivity, filterSpanCount).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        viewModel.filterElements.value?.let { list ->

                            return when (list[position].type) {
                                BookFilterElement.Type.Item -> {
                                    if (list[position].name == "전체") {
                                        6
                                    } else {
                                        3
                                    }
                                }
                                else -> filterSpanCount
                            }
                        }
                        return 1
                    }
                }
            }
            val screenHeight = DisplayUtils.getScreenHeight(this@WorkbookListActivity)

            filterRv.adapter = filterAdapter
            filterRv.minimumHeight = screenHeight - 48.toPx() - getStatusBarHeight()
        }
    }

    fun initUI () {
        binding.apply {
            viewModel.showEmptyContainer.postValue(false)
            viewModel.showRecyclerView.postValue(false)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)

            initFilterView()
//            fetchCustomBook()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchCustomBook()
    }


    private fun initFilterView() {
        viewModel.fetchBookFilter()
    }
    private fun fetchCustomBook() {
        viewModel.fetchCustomBooks()
    }

    private fun setSnackBar() {
        val snackBar = SnackBar(this, "핀 설정은 최근 문제집에서 확인할 수 있습니다.", "바로가기")
        snackBar.setSnackBarViewListener(object : SnackBarViewListener {
            override fun onXBtnClicked(view: SnackBarView) {
                snackBar.dismiss()
            }

            override fun onActionBtnClicked(view: SnackBarView) {
                snackBar.dismiss()
                finish()
                setResult(PatternStudyFragment.PLAN_PINNED, intent)
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            snackBar.show()
        }
    }

    private fun scrollLogEvent() {
        LogUtils.logEvent(this@WorkbookListActivity, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "스크롤", "전체문제집")
    }

//    override fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>) {
//        fetchCustomBook()
//    }

    override fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book) {
        fetchCustomBook()
        viewModel.completedWorkbookChallenge { startChallenge ->
            val moveEvent: (ChallengeCourse?) -> Unit = { it ->
                ChallengeManager.getMainTabMoveIntent(it).let {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(it)
                    finish()
                }
            }
            val completedDialog = ChallengeCompletedDialog.newInstance(
                challenge = startChallenge,
                ChallengeManager.CourseName.스타트챌린지_워크북.id,
            )
            completedDialog.moveEvent = moveEvent
//            completedDialog.useCouponEvent = {
//                val pgDialog = PurchaseGuideDialog.newInstance(2)
//                supportFragmentManager.let { pgDialog.show(it, "purchaseGuideDialog") }
//            }
            supportFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog4") }
        }
    }

    override fun onDeniedUser() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "워크북", "결제유도", "다이얼로그-다음")
        val dialog = PurchaseGuideDialog.newInstance()
        supportFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
    }

    override fun onGuestUser() {
        LogUtils.logEvent(this, user, PulleyEvent.INDUCE, "워크북", "가입유도")
        val dialog = JoinInduceForGuestDialog().apply {
            updateDismissCallback {
                viewModel.errorStatusReset()
            }
        }
        supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
    }

    override fun onActionBtnClicked(action: ActionType, book: Book) {
        when (action) {
            ActionType.mail -> {
//                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
//                val dialog = EmailInputDialog(this, listOf(book), user!!, this)
//                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.isPinned) "핀해제하기" else "핀설정하기"
                val itemValue = "전체문제집"
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습-워크북", itemName, itemValue)
                val id = if(book.assignID == null) book.pieceID else book.assignID!!
                viewModel.togglePin(id, !book.isPinned) {
                    setSnackBar()
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습-워크북", "최근문제집빼기")
                viewModel.removeFromMyPlan(book)
            }
        }
    }

    override fun onSolveClicked(book: Book) {
        val intent = SolveActivity.getIntent(this, book)
        startActivity(intent)
    }

    override fun filterFromTagOnCard(filterType: String) {
        val type = LearningFilterType.convertTagAtFilterType(filterType)
        viewModel.updateFilterTypes(filterType)
    }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userUpdateReceiver)
    }
}