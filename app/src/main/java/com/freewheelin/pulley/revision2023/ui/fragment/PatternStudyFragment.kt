package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentPatternStudyBinding
import com.freewheelin.pulley.databinding.TooltipAnalysisBinding
import com.freewheelin.pulley.dialogs.*
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.revision2023.ui.activity.PulleyMathBooksActivity
import com.freewheelin.pulley.revision2023.ui.activity.WorkbookListActivity
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.viewmodel.PatternStudyViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.MarginDecoration
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PatternStudyFragment : LearningTabFragment(),
    PlanListener,
    CustomizeBookDialogListener,
    EmailInputDialogListener {
    private lateinit var binding: FragmentPatternStudyBinding
    private val viewModel: PatternStudyViewModel by viewModels()

    override var screenName = "유형"

    private val myPlanAdapter = PatternStudyMyPlanAdapter (this)
    private lateinit var getResult: ActivityResultLauncher<Intent>

    companion object {
        val PLAN_PINNED = 401

        @JvmStatic
        fun newInstance() =
            PatternStudyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pattern_study, container, false)
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == PLAN_PINNED) {
                binding.apply {
                    scrollRootView.smoothScrollTo(0, myPlanCl.top)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel

                initAdapter()
                initGuide()

//                guideView.setViewModel(viewModel)

                pulleyMathBookCv.setOnClickListener { goPulleyMathBooks() }
                commercialBookCv.setOnClickListener { goPdfList() }
                workBookCv.setOnClickListener { goWorkbooks() }
            }
            viewModel.apply {
                myPlans.observe(viewLifecycleOwner) {
                    myPlanAdapter.submitList(it.myPieceStorageList)
                    val planCount = "총 ${it.myPieceStorageList.size}개 "
                    val pinText = "핀 설정 ${it.pinBookPlanCount}개 "
                    myPlanCount.postValue(planCount)
                    pinCount.postValue(pinText)
                    showMyPlanEmptyView.postValue(it.myPieceStorageList.isEmpty())
                    showMyPlan.postValue(it.myPieceStorageList.isNotEmpty())
                }
            }
        }
    }

    private fun initAdapter () {
        binding.apply {
            myPlanRv.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = myPlanAdapter
                val itemSpace = resources.getDimension(R.dimen.dp16).toInt()
                addItemDecoration(MarginDecoration(itemSpace))
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                dragLogEvent()
                            }
                        }
                    }
                })
            }
            viewModel.myPlanAdapter = myPlanAdapter
            viewModel.initMyPlanAdapterItem()
        }
    }

    private fun initGuide() {
        binding.apply {
            myPlanDeleteGuideTv.extensionTouchArea(12.toPx())
            myPlanDeleteGuideTv.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "삭제기준보기")
                val window =
                    BalloonWindow(requireContext(), it, BalloonWindow.Position.below, 16.toPx())
                window.balloonColor = ContextCompat.getColor(requireContext(), R.color.purple_ACACFF)
                window.offset = if (context?.is10InchUI == true) -240 else -190
                window.setPadding(if (context?.is10InchUI == true) 32.toPx() else 24.toPx())
                val tooltipBinding: TooltipAnalysisBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tooltip_analysis, null, false)
                tooltipBinding.chartTopTv.text = "삭제 기준"
                tooltipBinding.chartContentTv.text = viewModel.tooltipText
                window.show(tooltipBinding.root)
            }
        }
    }
    override fun initUI() {

    }

    override fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder) {
        when (action) {
            ActionType.mail -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
                val dialog = EmailInputDialog(requireContext(), listOf(book), user!!, this)
                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.pin) "핀해제하기" else "핀설정하기"
                val itemValue = if(holder is MyPlanHolder) "나의문제집" else if(holder is RecommendPlanHolder) "추천문제집" else "전체문제집"
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", itemName, itemValue)

                val id = if(book.assignID == null) book.pieceID else book.assignID!!
                viewModel.togglePin(id, !book.pin) {
                    viewModel.initMyPlanAdapterItem()
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "나의문제집빼기")

                viewModel.removeFromMyPlan(book) {
                    viewModel.initMyPlanAdapterItem()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.initMyPlanAdapterItem()
    }

    fun goPulleyMathBooks() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "전체문제집")
        PulleyMathBooksActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }
    fun goPdfList() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "출판사문제집")
        Intent(requireContext(), PdfListActivity::class.java).let {
            startActivity(it)
        }
    }
    fun goWorkbooks() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "전체-워크북만들기")
        WorkbookListActivity.getIntent(requireContext()).let {
            getResult.launch(it)
        }
    }
    override fun onReviewBtnClicked(holder: PlanHolder, book: Book) {
        val itemValue = if(holder is MyPlanHolder) "나의문제집" else if(holder is RecommendPlanHolder) "추천문제집" else "전체문제집"
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "리뷰하기", itemValue)
        val intent = SolveActivity.getReviewIntent(requireContext(), book)
        startActivity(intent)

    }

    override fun onSolveClicked(holder: PlanHolder, book: Book) {
        val intent = SolveActivity.getIntent(requireContext(), book)
        startActivity(intent)
    }

    override fun onMakeCustomBookClicked(holder: PlanHolder, book: Book) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "문제집선택버튼")
        CustomizeBookDialog(requireContext(), this, book).show()
    }

    override fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book) {

    }

    override fun onSentEmail() {
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }
    private fun dragLogEvent() {
        LogUtils.logEvent(
            requireContext(),
            user!!,
            PulleyEvent.BUTTON_CLICK,
            "유형학습",
            "스와이프",
            "나의문제집"
        )
    }
}