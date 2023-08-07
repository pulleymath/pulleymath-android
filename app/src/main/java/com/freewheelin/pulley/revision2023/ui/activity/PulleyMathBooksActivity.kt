package com.freewheelin.pulley.revision2023.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import com.freewheelin.pulley.databinding.ActivityPulleyMathBooksBinding
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity.Companion.FROM_PULLEYMATH_BOOKS
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity.Companion.WHERE_ARE_YOU_FROM
import com.freewheelin.pulley.legacy.dialogs.EmailInputDialog
import com.freewheelin.pulley.legacy.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.*
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.adapter.BookFilterAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter.OriginType
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.viewmodel.PulleyMathBooksViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.GridMarginDecoration
import com.freewheelin.pulley.legacy.views.snackBar.SnackBar
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarViewListener
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PulleyMathBooksActivity : AppCompatActivity(), LifecycleObserver, PlanListenerV2,
    EmailInputDialogListener {

    val binding: ActivityPulleyMathBooksBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_pulley_math_books, null, false)
    }
    private val viewModel: PulleyMathBooksViewModel by viewModels()
    lateinit var planAdapter: PatternStudyMyPlanAdapter
    lateinit var filterAdapter: BookFilterAdapter
    private lateinit var getResult: ActivityResultLauncher<Intent>
    lateinit var userUpdateReceiver: BroadcastReceiver

    companion object {
        const val FOCUS_ON_TOTAL_LABEL = "FOCUS_ON_TOTAL_LABEL"
        const val CHALLENGE_PATTERN_FINISHED = 302
        @JvmStatic
        fun getIntent(context: Context, isFocus: Boolean = false): Intent {
            return Intent(context, PulleyMathBooksActivity::class.java).apply {
                putExtra(FOCUS_ON_TOTAL_LABEL, isFocus)
            }
        }
    }

    var resumeCount = 0
    override fun onResume() {
        super.onResume()
        val filters = viewModel.selectedFilterTypes.toSet()
        viewModel.fetchTotalBooks(filters)
        if (resumeCount > 0) {
            viewModel.collectRecommendList(false) {}
        }
        resumeCount += 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initReceiver()
        val isFocusingOnTotalLabel = intent.getBooleanExtra(FOCUS_ON_TOTAL_LABEL, false)

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@PulleyMathBooksActivity
            planListener = this@PulleyMathBooksActivity

            initAdapter()
            initUI()
            initRecommend()
            initActivityResult()
            btnBack.setOnClickListener {
                finish()
            }
        }

        viewModel.apply {
            checkActionOfStartChallenge {
                val guideDialog = ChallengeGuideManager.getStartGuideMission2()
                supportFragmentManager.let { guideDialog.show(it, "getStartGuideMission2") }
            }

            playTotalLoadingView.observe(this@PulleyMathBooksActivity) {
                if (it) {
                    binding.totalLoadingView.playAnimation()
                } else {
                    binding.totalLoadingView.cancelAnimation()
                }
            }
            books.observe(this@PulleyMathBooksActivity) {
                if (latestFilters == selectedFilterTypes) {
                    planAdapter.submitList(it) {
                        Handler(Looper.getMainLooper()).post {
                            binding.totalRv.invalidateItemDecorations()
                        }
                    }
                    binding.totalRv.scrollToPosition(0)
                    if (it.isEmpty()) {
                        binding.totalEmptyContainer.show(300)
                    } else {
                        val rvAnimController = AnimationUtils.loadLayoutAnimation(
                            this@PulleyMathBooksActivity,
                            R.anim.recyclerview_grid_layout_animation
                        )
                        binding.totalRv.layoutAnimation = rvAnimController
                        binding.totalRv.scheduleLayoutAnimation()
                    }
                    showDummyBottomView.postValue(it.size < 7)
                    showTotalLoadingView.postValue(false)
                    playTotalLoadingView.postValue(false)
                    showTotalPlanCover.postValue(false)
                }
            }
            filterElements.observe(this@PulleyMathBooksActivity) {
                this@PulleyMathBooksActivity.filterAdapter.submitList(it)
            }
            initPositionSettingFlag.observeOnce(this@PulleyMathBooksActivity) {
                if (!isFocusingOnTotalLabel) return@observeOnce
                val outArr = arrayOf(0, 0).toIntArray()
                binding.totalLabelTv.getLocationOnScreen(outArr)
                val yValueOnView = outArr[1] - 100.toPx()
                binding.rootView.smoothScrollTo(0, yValueOnView)
            }
            scrollPositionTop.observe(this@PulleyMathBooksActivity) {
                binding.totalRv.scrollToPosition(0);
                binding.totalRv.layoutManager?.scrollToPosition(0);
            }
            isLoading.observe(this@PulleyMathBooksActivity) { loading ->
                binding.apply {
                    if (loading) {
                        loadingContainer.visibleIf(true)
                        loadingLottie.playAnimation()
                    } else {
                        loadingContainer.hide(300)
                    }
                }
            }
            showTotalLoadingView.observe(this@PulleyMathBooksActivity) { isShow ->
                binding.apply {
                    if (isShow) {
                        totalLoadingContainer.visibleIf(true)
                        totalLoadingView.playAnimation()
                    } else {
                        totalLoadingContainer.hide(300)
                    }
                }
            }
            errorAction.observe(this@PulleyMathBooksActivity) { type ->
                when(type) {
                    HttpException403, GuestException -> showGuestJoinInduceDialog()
                    NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : $type")}
                }
            }
        }
    }

    private fun initReceiver() {
        userUpdateReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    finish()
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(userUpdateReceiver, IntentFilter(UserManager.EVENT_USER_UPDATE))

    }

    private fun showGuestJoinInduceDialog() {
        val dialog = JoinInduceForGuestDialog().apply {
            updateDismissCallback {
                viewModel.errorStatusReset()
            }
        }
        supportFragmentManager.let { dialog.show(it, "joinInduceDialog") }
    }

    fun initUI () {
        binding.apply {
            viewModel.showEmptyContainer.postValue(false)
            viewModel.showRecyclerView.postValue(false)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)
            viewModel.fetchBookFilter()
            totalPlanContainerLl.layoutParams.height = DisplayUtils.getScreenHeight(this@PulleyMathBooksActivity)
        }
    }

    fun initAdapter() {
        binding.apply {
            planAdapter = PatternStudyMyPlanAdapter (this@PulleyMathBooksActivity, listOf(ActionType.pin), OriginType.PulleyMathTotal, viewModel = viewModel)
            val planSpanCount = if(isTablet) 4 else 2
            totalRv.layoutManager = GridLayoutManager(this@PulleyMathBooksActivity, planSpanCount)
            totalRv.adapter = planAdapter

            val columnSpace = resources.getDimension(R.dimen.dp24).toInt()
            totalRv.addItemDecoration(GridMarginDecoration(16.toPx(), columnSpace, planSpanCount))
            totalRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            scrollLogEvent()
                        }
                    }
                }
            })

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
            val filterSpanCount = if(isTablet) 2 else 3
            filterRv.layoutManager = GridLayoutManager(this@PulleyMathBooksActivity, filterSpanCount).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        viewModel.filterElements.value?.let { list ->
                            return when (list[position].type) {
                                BookFilterElement.Type.Item -> 1
                                else -> filterSpanCount
                            }
                        }
                        return 1
                    }
                }
            }
            viewModel.planAdapter = planAdapter
            filterRv.adapter = filterAdapter
            filterRv.minimumHeight = if (isTablet) 650.toPx() else 550.toPx()
            viewModel.filterAdapter = filterAdapter
        }
    }

    fun initRecommend () {
        binding.apply {
            viewModel.collectRecommendList {
                recommendLabel.showIfNeed()
            }

            viewModel.planListener = this@PulleyMathBooksActivity
            viewModel.recommendBookListViews = listOf(firstRecommendList, secondRecommendList, thirdRecommendList, fourthRecommendList)
            viewModel.recommendBookListViews.forEach {
                it.visibility = View.GONE
            }
        }
    }
    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                CHALLENGE_PATTERN_FINISHED -> {
                    viewModel.joinedChallengeList.value?.find { it.isStartChallenge }?.startChallenge?.let { startChallenge ->
                        if (startChallenge.isPulleyBooksCourseFinished) {
                            val turnOnCompletedDialog = {
                                val moveEvent: (ChallengeCourse?) -> Unit = { course ->
                                    ChallengeManager.getMainTabMoveIntent(course).let { intent ->
                                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                                        finish()
                                    }
                                }
                                val completedDialog = ChallengeCompletedDialog.newInstance(
                                    challenge = startChallenge,
                                    ChallengeManager.CourseName.스타트챌린지_유형.id,
                                )
                                completedDialog.moveEvent = moveEvent
//                                completedDialog.useCouponEvent = {
//                                    val pgDialog = PurchaseGuideDialog.newInstance(2)
//                                    supportFragmentManager.let { pgDialog.show(it, "purchaseGuideDialog") }
//                                }
                                supportFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog2") }
                            }

                            val finishGuideDialog = ChallengeGuideManager
                                .getFinishGuideFromMission2(nextEvent = turnOnCompletedDialog)
                            supportFragmentManager.let { finishGuideDialog.show(it, "finishGuideDialog") }
                        }
                    }
                }
            }
        }
    }
    private fun scrollLogEvent() {
        LogUtils.logEvent(this@PulleyMathBooksActivity, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "스크롤", "전체문제집")
    }

    private fun setSnackBar() {
        val snackBar = SnackBar(this, "핀 설정은 최근 문제집에서 확인할 수 있습니다.", "바로가기")
        snackBar.setSnackBarViewListener(object : SnackBarViewListener {
            override fun onXBtnClicked(view: SnackBarView) {
                snackBar.dismiss()
            }

            override fun onActionBtnClicked(view: SnackBarView) {
                snackBar.dismiss()
                setResult(PatternStudyFragment.PLAN_PINNED, intent)
                finish()
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            snackBar.show()
        }
    }

    override fun filterFromTagOnCard(filterType: String) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "유형카드", "태그", filterType)
        val type = LearningFilterType.convertTagAtFilterType(filterType)
        viewModel.updateFilterTypes(type)
        viewModel.syncSelectedFilterType()
        getTotalListWithoutRefresh()
    }
    private fun getTotalListWithoutRefresh() {
        binding.apply {
            viewModel.showTotalPlanCover.postValue(true)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)
            val filters = viewModel.selectedFilterTypes.toSet()

            viewModel.fetchTotalBooks(filters)
        }
    }

    override fun onSentEmail() {
        DaebakToast.show(this, "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    override fun onActionBtnClicked(action: ActionType, book: Book) {
        when (action) {
            ActionType.mail -> {
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
                val dialog = EmailInputDialog(this, listOf(book), user!!, this)
                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.isPinned) "핀해제하기" else "핀설정하기"
                val itemValue = "전체문제집"
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", itemName, itemValue)
                val id = if(book.assignID == null) book.pieceID else book.assignID!!
                viewModel.togglePin(id, !book.isPinned) {
                    setSnackBar()
                    viewModel.collectRecommendList(false) {}
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "최근문제집빼기")
                viewModel.removeFromMyPlan(book)
                viewModel.collectRecommendList(false) {}
            }
        }
    }

    override fun onSolveClicked(book: Book) {
        val intent = SolveActivity.getIntent(this, book)
        intent.putExtra(WHERE_ARE_YOU_FROM, FROM_PULLEYMATH_BOOKS)

        getResult.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userUpdateReceiver)
    }
}