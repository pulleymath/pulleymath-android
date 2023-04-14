package com.freewheelin.pulley.revision2023.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityWorkbookListBinding
import com.freewheelin.pulley.dialogs.CustomizeBookDialog
import com.freewheelin.pulley.dialogs.CustomizeBookDialogListener
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeInduceDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.revision2023.viewmodel.WorkbookListViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.GridMarginDecoration
import com.freewheelin.pulley.views.snackBar.SnackBar
import com.freewheelin.pulley.views.snackBar.SnackBarView
import com.freewheelin.pulley.views.snackBar.SnackBarViewListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkbookListActivity : AppCompatActivity(), LifecycleObserver, PlanListenerV2, BookFilterListener,
    CustomizeBookDialogListener {
    val binding: ActivityWorkbookListBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_workbook_list, null, false)
    }
    private val viewModel: WorkbookListViewModel by viewModels()
//    private val totalPlanAdapter = PatternStudyTotalPlanAdapter (this, null, null)
    lateinit var planAdapter: PatternStudyMyPlanAdapter
    lateinit var userUpdateReceiver: BroadcastReceiver

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
            btnBack.setOnClickListener {
                finish()
            }
            createWorkbookCl.setOnClickListener {
                LogUtils.logEvent(this@WorkbookListActivity, user, PulleyEvent.BUTTON_CLICK,"워크북","워크북만들기")

                if (user?.serviceType?.isGuestUser == true) {
                    LogUtils.logEvent(this@WorkbookListActivity, user, PulleyEvent.INDUCE,"워크북","가입유도")
                    val dialog = JoinInduceForGuestDialog {
                        viewModel.errorStatusReset()
                    }
                    supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
                } else {
                    val isStartChallengeInProgress = viewModel.showStartChallengeStamp.value == true
                    CustomizeBookDialog(this@WorkbookListActivity, isStartChallengeInProgress, this@WorkbookListActivity).show()
                }
            }

//            DialogUtils.confirmDialog(this@WorkbookListActivity, "[테스트]구독중이 아닙니다.", "열려라 참깨")

        }
        viewModel.apply {
            playTotalLoadingView.observe(this@WorkbookListActivity) {
                if (it) {
                    binding.totalLoadingView.playAnimation()
                } else {
                    binding.totalLoadingView.cancelAnimation()

                }
            }
            customBooks.observe(this@WorkbookListActivity) {
                if (latestFilters == binding.filterView.selectedFilterTypes || latestFilters == binding.filterView.customBookInitFilterTypes) {
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
            }
            joinedChallengeList.observe(this@WorkbookListActivity) {
                val isWorkbookStartChallengeInProgress = it.find { it.startChallenge?.isWorkbooksInProgress == true } != null
                showStartChallengeStamp.postValue(isWorkbookStartChallengeInProgress)
            }
            checkActionOfStartChallenge {
                val guideDialog = ChallengeGuideManager.getStartGuideMission4()
                supportFragmentManager.let { guideDialog.show(it, "getStartGuideMission3") }
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
            val spanCount = if (isTablet) 4 else 3
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
        }
    }

    fun initUI () {
        binding.apply {
            viewModel.showEmptyContainer.postValue(false)
            viewModel.showRecyclerView.postValue(false)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)

            initFilterView()
            fetchCustomBook()
            filterView.listener = this@WorkbookListActivity
        }
    }

    override fun onResume() {
        super.onResume()
        fetchCustomBook()
    }


    private fun initFilterView() {
        binding.apply {
            filterView.selectedFilterTypes.clear()
            filterView.selectedFilterTypes.addAll(filterView.customBookInitFilterTypes)
        }
    }
    private fun fetchCustomBook() {
        val filters = binding.filterView.selectedFilterTypes.toSet()
        viewModel.fetchCustomBook(filters)
    }

    private fun setSnackBar() {
        val snackBar = SnackBar(this, "핀 설정은 나의 문제집에서 확인할 수 있습니다.", "바로가기")
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

    override fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>) {
        fetchCustomBook()
    }

    override fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book) {
        fetchCustomBook()
        viewModel.completedWorkbookChallenge { startChallenge ->
            val moveEvent: (ChallengeCourse?) -> Unit = { it ->
                ChallengeManager.getMainTabMoveIntent(it).let {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(it)
                    finish()
                }
            }
            val completedDialog = ChallengeCompletedDialog(startChallenge,
                ChallengeManager.CourseName.스타트챌린지_워크북.id,
                moveEvent = moveEvent,
                exitEvent = {
                    val nextCourse = startChallenge.getNextCourse(ChallengeManager.CourseName.스타트챌린지_워크북.id)
                    if (nextCourse != null) {
                        val induceDialog = ChallengeInduceDialog(
                            ChallengeInduceDialog.Type.OneMore,
                            course = nextCourse,
                            moveEvent = moveEvent
                        )
                        supportFragmentManager.let { induceDialog.show(it, "challengeInduceDialog") }
                    }
                }
            )

            supportFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog4") }
        }
    }

    override fun onDeniedUser() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "워크북", "결제유도", "다이얼로그-다음")
        val dialog = PurchaseGuideDialog()
        supportFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
    }

    override fun onGuestUser() {
        LogUtils.logEvent(this, user, PulleyEvent.INDUCE, "워크북", "가입유도")
        val dialog = JoinInduceForGuestDialog {
            viewModel.errorStatusReset()
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
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습-워크북", "나의문제집빼기")
                viewModel.removeFromMyPlan(book)
            }
        }
    }

    override fun onSolveClicked(book: Book) {
        val intent = SolveActivity.getIntent(this, book)
        startActivity(intent)
    }

    override fun filterFromTagOnCard(filterType: String) {
        val type = FilterType.convertTagAtFiltertType(filterType)
        binding.filterView.selectedFilterTypes.add(type)
        binding.filterView.selectedFilterTypes.removeAll(type.exclusiveSet)
        binding.filterView.adapter?.notifyDataSetChanged()
    }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userUpdateReceiver)
    }
}