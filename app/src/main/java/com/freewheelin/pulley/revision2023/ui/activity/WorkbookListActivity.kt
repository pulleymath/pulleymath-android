package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityWorkbookListBinding
import com.freewheelin.pulley.dialogs.CustomizeBookDialog
import com.freewheelin.pulley.dialogs.CustomizeBookDialogListener
import com.freewheelin.pulley.dialogs.EmailInputDialog
import com.freewheelin.pulley.dialogs.PulleyPlusPriceDialog
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyTotalPlanAdapter
import com.freewheelin.pulley.revision2023.ui.fragment.PatternStudyFragment
import com.freewheelin.pulley.revision2023.viewmodel.WorkbookListViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.GridMarginDecoration
import com.freewheelin.pulley.views.snackBar.SnackBar
import com.freewheelin.pulley.views.snackBar.SnackBarView
import com.freewheelin.pulley.views.snackBar.SnackBarViewListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class WorkbookListActivity : AppCompatActivity(), LifecycleObserver, PlanListener,
    BookFilterListener,
    CustomizeBookDialogListener {
    val binding: ActivityWorkbookListBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_workbook_list, null, false)
    }
    private val viewModel: WorkbookListViewModel by viewModels()
    private val totalPlanAdapter = PatternStudyTotalPlanAdapter (this, null)

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
            initAdapter()
            initUI()
            btnBack.setOnClickListener {
                finish()
            }
            createWorkbookCl.setOnClickListener {
                if (user!!.hasPulleyPlus) {
                    CustomizeBookDialog(this@WorkbookListActivity, this@WorkbookListActivity).show()
                } else {
                    DialogUtils.confirmHasPulleyPlus(this@WorkbookListActivity) {
                        PulleyPlusPriceDialog(this@WorkbookListActivity).show()
                    }
                }
            }
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
                    totalPlanAdapter.submitList(it)
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
        }
    }
    fun initAdapter() {
        binding.apply {
            totalRv.layoutManager = GridLayoutManager(this@WorkbookListActivity, 3)
            totalRv.adapter = totalPlanAdapter
            totalRv.addItemDecoration(GridMarginDecoration(16.toPx(), 0, 3))
            totalRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when(newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> { scrollLogEvent() }
                    }
                }
            })
            viewModel.adapter = totalPlanAdapter
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


    override fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder) {
        when (action) {
            ActionType.mail -> {
//                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
//                val dialog = EmailInputDialog(this, listOf(book), user!!, this)
//                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.pin) "핀해제하기" else "핀설정하기"
                val itemValue = "전체문제집"
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습-워크북", itemName, itemValue)
                val id = if(book.assignID == null) book.pieceID else book.assignID!!
                viewModel.togglePin(id, !book.pin) {
                    setSnackBar()
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "유형학습-워크북", "나의문제집빼기")
                viewModel.removeFromMyPlan(book)
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
                finish()
                setResult(PatternStudyFragment.PLAN_PINNED, intent)
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
        startActivity(intent)
    }

    override fun onMakeCustomBookClicked(holder: PlanHolder, book: Book) {

    }
    private fun scrollLogEvent() {
        LogUtils.logEvent(this@WorkbookListActivity, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "스크롤", "전체문제집")
    }

    override fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>) {
        fetchCustomBook()
    }

    override fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book) {
        fetchCustomBook()
    }
}