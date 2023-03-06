package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityPulleyMathBooksBinding
import com.freewheelin.pulley.dialogs.EmailInputDialog
import com.freewheelin.pulley.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyTotalPlanAdapter
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.revision2023.viewmodel.PulleyMathBooksViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.GridMarginDecoration
import com.freewheelin.pulley.views.snackBar.SnackBar
import com.freewheelin.pulley.views.snackBar.SnackBarView
import com.freewheelin.pulley.views.snackBar.SnackBarViewListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PulleyMathBooksActivity : AppCompatActivity(), LifecycleObserver, PlanListener, PatternStudyListener, BookFilterListener,
    EmailInputDialogListener {

    val binding: ActivityPulleyMathBooksBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_pulley_math_books, null, false)
    }
    private val viewModel: PulleyMathBooksViewModel by viewModels()
    lateinit var totalPlanAdapter: PatternStudyTotalPlanAdapter
    private lateinit var getResult: ActivityResultLauncher<Intent>

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
        val filters = binding.filterView.selectedFilterTypes.toSet()
        viewModel.fetchTotalBooks(filters)
        if (resumeCount > 0) {
            viewModel.collectRecommendList(false) {}
        }
        resumeCount += 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val isFocusingOnTotalLabel = intent.getBooleanExtra(FOCUS_ON_TOTAL_LABEL, false)

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@PulleyMathBooksActivity

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
                if (latestFilters == binding.filterView.selectedFilterTypes) {
                    totalPlanAdapter.submitList(it)
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
            initPositionSettingFlag.observeOnce(this@PulleyMathBooksActivity) {
                if (!isFocusingOnTotalLabel) return@observeOnce
                val outArr = arrayOf(0, 0).toIntArray()
                binding.totalLabelTv.getLocationOnScreen(outArr)
                val yValueOnView = outArr[1] - 100.toPx()
                binding.rootView.smoothScrollTo(0, yValueOnView)
            }
        }
    }

    fun initUI () {
        binding.apply {
            viewModel.showEmptyContainer.postValue(false)
            viewModel.showRecyclerView.postValue(false)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)
            val filters = filterView.selectedFilterTypes.toSet()
            viewModel.fetchTotalBooks(filters)
            totalPlanContainerLl.layoutParams.height = DisplayUtils.getScreenHeight(this@PulleyMathBooksActivity)
            filterView.listener = this@PulleyMathBooksActivity
        }
    }

    fun initAdapter() {
        binding.apply {
            totalPlanAdapter = PatternStudyTotalPlanAdapter (this@PulleyMathBooksActivity, this@PulleyMathBooksActivity, viewModel)
            totalRv.layoutManager = GridLayoutManager(this@PulleyMathBooksActivity, 3)
            totalRv.adapter = totalPlanAdapter
            totalRv.addItemDecoration(GridMarginDecoration(16.toPx(), 0, 3))
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
            viewModel.totalAdapter = totalPlanAdapter
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
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                CHALLENGE_PATTERN_FINISHED -> {
                    viewModel.joinedChallengeList.value?.find { it.isStartChallenge }?.startChallenge?.let { startChallenge ->
                        if (startChallenge.isPulleyBooksCourseFinished) {
                            val turnOnCompletedDialog = {
                                val completedDialog = ChallengeCompletedDialog(startChallenge,
                                    ChallengeManager.CourseName.스타트챌린지_유형.id,
                                ) {
                                    ChallengeManager.getMainTabMoveIntent(it).let {
                                        LocalBroadcastManager.getInstance(this).sendBroadcast(it)
                                        finish()
                                    }
                                }
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

    override fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder) {
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
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "나의문제집빼기")
                viewModel.removeFromMyPlan(book)
                viewModel.collectRecommendList(false) {}
            }
        }

    }

    private fun setSnackBar() {
        val snackBar = SnackBar(this, "핀 설정은 나의 문제집에서 확인할 수 있습니다.", "바로가기")
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

    override fun onReviewBtnClicked(holder: PlanHolder, book: Book) {

        val itemValue = "전체문제집"
        LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "리뷰하기", itemValue)
        val intent = SolveActivity.getReviewIntent(this, book)
        startActivity(intent)
    }

    override fun onSolveClicked(holder: PlanHolder, book: Book) {
        val intent = SolveActivity.getIntent(this, book)
        getResult.launch(intent)
    }

    override fun onMakeCustomBookClicked(holder: PlanHolder, book: Book) {
        println("onMakeCustomBookClicked")
    }

    override fun filterFromTagOnCard(type: FilterType) {
        binding.filterView.selectedFilterTypes.add(type)
        binding.filterView.selectedFilterTypes.removeAll(type.exclusiveSet)
        binding.filterView.adapter?.notifyDataSetChanged()

        getTotalListWithoutRefresh()
    }
    private fun getTotalListWithoutRefresh() {
        binding.apply {
            viewModel.showTotalPlanCover.postValue(true)
            viewModel.showTotalLoadingView.postValue(true)
            viewModel.playTotalLoadingView.postValue(true)
            val filters = filterView.selectedFilterTypes.toSet()

            viewModel.fetchTotalBooks(filters)
        }
    }

    override fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>) {
        viewModel.showEmptyContainer.postValue(false)
        viewModel.showRecyclerView.postValue(false)
        viewModel.showTotalLoadingView.postValue(true)
        viewModel.playTotalLoadingView.postValue(true)
        viewModel.fetchTotalBooks(filters.toSet())
    }

    override fun onSentEmail() {
        DaebakToast.show(this, "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    fun scrollToTotalLabel(subject: String?) {
        subject?.let {
            val targetHashSet = setFilterType(it)
            binding.filterView.selectedFilterTypes = targetHashSet
            binding.filterView.adapter?.notifyDataSetChanged()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.rootView.scrollToView(binding.totalLabelTv)
        }, 1500)
    }
    fun setFilterType(subject: String): HashSet<FilterType> {
        val defaultSet = mutableSetOf(
            FilterType.워크북_미포함,
            FilterType.핀_포함,
            FilterType.계열_전체,
            FilterType.유형_전체,
            FilterType.추천_2_3등급
        )
        when (subject) {
            "수학(상)" -> defaultSet.add(FilterType.과목_수학_상)
            "수학(하)" -> defaultSet.add(FilterType.과목_수학_하)
            "수학1" -> defaultSet.add(FilterType.과목_수학1)
            "수학2" -> defaultSet.add(FilterType.과목_수학2)
            "미적분" -> defaultSet.addAll(listOf(FilterType.과목_미적분, FilterType.과목_수학2))
            "확률과 통계" -> defaultSet.add(FilterType.과목_확통)
            "기하" -> defaultSet.add(FilterType.과목_기하)
            else -> defaultSet.add(FilterType.과목_수학1)
        }
        return defaultSet.toHashSet()
    }
}