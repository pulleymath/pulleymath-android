package com.freewheelin.pulley.activities.analysis.tabFragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.activities.analysis.AnalysisTabDelegate
import com.freewheelin.pulley.activities.analysis.AnanlysisTabActivityInterface
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.AnalysisFragment.Companion.IS_SAMPLE
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.AnalysisFragment.Companion.TAB_SCROLL_EVENT
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.AnalysisFragment.Companion.TAB_SCROLL_EVENT_TARGET
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisAllActivity
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ContentManager
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.FragmentAnalysisUnitBinding
import com.freewheelin.pulley.databinding.ItemAnalysisUnitBinding
import com.freewheelin.pulley.dialogs.WrongManagementDialog
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.Analysis
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.curation.MyCuration
import com.freewheelin.pulley.revision2023.model.request.AnalysisAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.viewmodel.AnalysisTabActViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.snackBar.SnackBar
import com.freewheelin.pulley.views.snackBar.SnackBarView
import com.freewheelin.pulley.views.snackBar.SnackBarViewListener
import com.freewheelin.pulley.views.charts.TriplePenChart
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableAdapter
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableItem

import org.joda.time.LocalDate

class AnalysisUnitFragment : Fragment(), DabakTabRadioListener, AnalysisTabDelegate, ObservableHashSetListener<ChapterAnalysis> {

    override val tabTitle = "단원 분석"
    override var scrollPosition: Int = 0
    override var from: LocalDate = LocalDate.now().minusDays(6)
    override var to: LocalDate = LocalDate.now()

    val template: MyCuration
        get() = MyCuration(requireContext())

    var selectedChapter: ObservableHashSet<ChapterAnalysis> = ObservableHashSet()

    var chapterTreeList: List<ChapterTreeList>? = null

    var snackBar: SnackBar? = null

    override val analysis: Analysis?
        get() {
            return (activity as AnanlysisTabActivityInterface).analysis
        }

    val chapterAnalysis: List<ChapterAnalysis>
        get() {
            return this.analysis?.chapterAnalysis ?: listOf()
        }

    val notExistDataText: String
        get() {
            return (activity as AnanlysisTabActivityInterface).notExistDataText
        }

    override fun getFragment(): Fragment {
        return this
    }


    companion object {
        @JvmStatic
        fun newInstance(isSample: Boolean): AnalysisUnitFragment {
            val fragment = AnalysisUnitFragment()
            val bundle = Bundle()
            bundle.putBoolean(IS_SAMPLE, isSample)
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var binding: FragmentAnalysisUnitBinding
    val viewModel: AnalysisTabActViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis_unit, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            achieveOrderRadio.textSize = 20f
            achieveOrderRadio.labels = listOf("낮은 성취도 TOP3", "높은 성취도 TOP3")
            achieveOrderRadio.listener = this@AnalysisUnitFragment

            initUI()
            configureUI(DateTimeUtils.getPeriod(from, to))
            unitRv.layoutManager = LinearLayoutManager(context)
            unitRv.isFocusable = false

            (activity as? AnalysisTabActivity)?.binding?.scrollView?.scrollTo(0, scrollPosition)
            (activity as? UserAnalysisAllActivity)?.binding?.scrollView?.scrollTo(0, scrollPosition)
        }
        viewModel.errorAction.observe(viewLifecycleOwner) {
            dialog?.dismiss()
            DaebakToast.showFailedMakePiece(requireContext())
        }
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        configureAchieveSectionUI()
    }

    override fun onPeriodSelected(from: LocalDate, to: LocalDate, period: Int) {
        super.onPeriodSelected(from, to, period)
        selectedChapter.clear()
        if(from > LocalDate.now() || analysis == null)
            configureEmptyUI()
        else
            configureUI(period)
    }

    override fun onItemChanged(set: ObservableHashSet<ChapterAnalysis>) {
        with(binding) {
            if(set.isEmpty()) {
                unitSelectGuideTv.text = ""
                learnBtn.toDisableUI()
                reviewBtn.toDisableUI()
            } else {
                val problemCount = set.sumOf { it.problemTotalNumber }
                unitSelectGuideTv.text = "${problemCount}개의 문제를 학습합니다."
                learnBtn.toEnableUI()
                reviewBtn.toEnableUI()
            }
        }
    }

    var dialog: Dialog? = null
    private fun initUI() {
        selectedChapter.listener = this
        selectedChapter.clear()
        val isSample = arguments?.getBoolean(IS_SAMPLE) ?: false
        with(binding) {
            unitSelectGuideTv.text = ""
            learnBtn.setOnClickListener {
                if (isSample) {
                    if (learnBtn.isEnableUI()) {
                        DaebakToast.show(requireContext(), "유사한 문제를 만들어 풀어볼 수 있어요!")
                        return@setOnClickListener
                    } else {
                        DaebakToast.show(requireContext(), "보완할 단원을 선택해보세요!")
                        return@setOnClickListener
                    }
                }
                if(learnBtn.isEnableUI()) {
                    LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "내분석보기", "추가학습하기")
                    val dialog = WrongManagementDialog(requireContext(), WrongManagementDialog.Type.scrap)
                    this@AnalysisUnitFragment.dialog = dialog
                    dialog.configureUIByChapter(selectedChapter)
                    dialog.show()
                    dialog.binding.makeBtn.setOnClickListener {
                        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "내분석보기", "단원 학습지 만들기")
                        dialog.binding.makeBtn.startLoading()

                        val similar = if (dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom) "SIMILAR" else "SAME"
                        val chapterLittles = selectedChapter.toList().map { it.code }
                        val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
                        val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
                        val difficulty = dialog.level?.text ?: ""
                        val requestProblemNumber = dialog.cnt
                        val isIncludeClearProblem = dialog.isClearInclude

                        val req = AnalysisAdvancedLearningRequest(
                            sameOrSimilar = similar,
                            studentID = user?.studentID!!,
                            requestProblemNumber = requestProblemNumber,
                            difficulty = difficulty,
                            noteType = null,
                            includeClearProblem = isIncludeClearProblem,
                            chapterLittles = chapterLittles,
                            startDate = startDate,
                            endDate = endDate
                        )
                        viewModel.setAdvancedLearning(req) {
                            dialog.dismiss()
                            if (dialog.binding.checkbox.isChecked) {
                                val intent = SolveActivity.getIntent(requireContext(), it)
                                startActivity(intent)
                            } else {
                                val text = if (selectedChapter.size == 1)
                                    "'${selectedChapter.first().name}'의 오답관리 문제가 만들어졌습니다."
                                else
                                    "'${selectedChapter.first().name}' 외 ${selectedChapter.size - 1}개의 오답관리 문제가 만들어졌습니다."
                                showSnackBar(text, "바로가기")
                            }

                            selectedChapter.clear()
                        }
                    }
                }
            }

            reviewBtn.setOnClickListener {
                if (isSample) {
                    if (reviewBtn.isEnableUI()) {
                        DaebakToast.show(requireContext(), "푼 문제를 다시 확인해 볼 수 있어요!")
                        return@setOnClickListener
                    } else {
                        DaebakToast.show(requireContext(), "보완할 단원을 선택해보세요!")
                        return@setOnClickListener
                    }
                }

                if(reviewBtn.isEnableUI()) {
                    ContentManager.getReview(requireContext(), user!!, selectedChapter.toList(), from.toDate(), to.toDate()) {
                        val intent = SolveActivity.getReviewIntent(requireContext(), it, false)
                        startActivity(intent)
                    }
                }
            }
        }
    }

//    private fun configureUI(from: LocalDate, to: LocalDate, period: Int) {
    private fun configureUI(period: Int) {
        configureSummarySectionUI(period)
        configureAchieveSectionUI()
        configureUnitSectionUI()
    }

    private fun configureAllCheckBoxUI() {
        with(binding) {
            allCheckBox.setOnCheckedChangeListener(null)
            val chapters = chapterTreeList?.map { it.leaf() }?.flatten()?.map { it.chapter } ?: listOf()
            allCheckBox.isChecked = selectedChapter.containsAll(chapters)
            allCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    selectedChapter.addAll(chapters)
                else
                    selectedChapter.removeAll(chapters)
                unitRv.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun configureSummarySectionUI(period: Int) {
        with(binding) {
            subjectChart.setData(analysis?.getUnitSummaryData() ?: listOf(), true)
            subjectChart.setDetailBtnVisibility(View.GONE)

            val myRating = analysis?.myRating
            val upperRatingByMe = analysis?.getUpperRatingByMe()

            if (myRating == "1" || myRating == "S") {
                subjectChart.setSelectedBarLabel("${myRating}등급\n평균", "나의\n정답률", null)

            } else if (myRating != null) {
                subjectChart.setSelectedBarLabel("${myRating}등급\n평균", "나의\n정답률", "${upperRatingByMe}등급\n평균")
            }
            subjectChart.selectedBar = subjectChart.bars?.first()
            subjectChart.bars?.first()?.isSelectedDetailBtn = true
            subjectChart.requestLayout()

            if (analysis == null)
                summaryTv.text = template.dataNotExistText
            else
                summaryTv.text = template.getUnitSummaryQ(period, analysis?.improvement, analysis!!.summaryAnalysis)
        }
    }

    private fun configureAchieveSectionUI() {
        with(binding) {
            val achieveViews = listOf(
                AchieveView(firstSubjectTv, firstUnitTv, firstRatingBorder, firstProblemCntTv, firstTPC, firstAchieveEmptyGuideTv),
                AchieveView(secondSubjectTv, secondUnitTv, secondRatingBorder, secondProblemCntTv, secondTPC, secondAchieveEmptyGuideTv),
                AchieveView(thirdSubjectTv, thirdUnitTv, thirdRatingBorder, thirdProblemCntTv, thirdTPC, thirdAchieveEmptyGuideTv)
            )

            val achieves = analysis?.getUnitAchieveData(achieveOrderRadio.selectedIndex == 0) ?: listOf()

            for(i in 0 until 3) {
                val achieve = achieves.getOrNull(i)
                if(achieve == null)
                    achieveViews[i].showEmptyGuide()
                else {
                    val chapterAnalysis = achieve.second
                    achieveViews[i].set(achieve.first, chapterAnalysis.name, chapterAnalysis.problemTotalNumber, chapterAnalysis.myRate, chapterAnalysis.belowRate)
                    achieveViews[i].tpc.setLabels("내 정답률", "${analysis!!.myRating}등급 평균")
                }
            }

            achieveGuideTv.text = template.getUnitAchieveQ(achieves.getOrNull(0)?.second, achieveOrderRadio.selectedIndex == 0)
        }
    }

    private fun configureUnitSectionUI() {

        val treeList = chapterAnalysis.map {
            val children = it.chapters.map {
                val children = it.chapters.map {

                    ChapterTreeList(it, listOf())
                }
                ChapterTreeList(it, children)
            }

            ChapterTreeList(it, children)
        }
        with(binding) {
            unitRv.visibility = View.VISIBLE
            chapterTreeList = treeList

            val adapter = UnitAdapter()
            if(treeList.isNotEmpty()) {
                adapter.setItems(chapterTreeList!!)
                unitRv.adapter = adapter
                unitRv.visibility = View.VISIBLE
                guideView.visibility = View.GONE
            } else {
                unitRv.visibility = View.GONE
                guideView.visibility = View.VISIBLE
            }

            unitGuideTv.text = template.getUnitChapterQ(analysis?.chapterAnalysis)
            val chapters = chapterTreeList?.map { it.leaf() }?.flatten()?.map { it.chapter } ?: listOf()
            allCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    selectedChapter.addAll(chapters)
                else
                    selectedChapter.removeAll(chapters)
                adapter.notifyDataSetChanged()
                Tutor.showToolTipIfNeed(allCheckBox, Tutor.TooltipType.additionalStudyInAnalysis)
            }
        }
    }

    fun configureEmptyUI() {
        with(binding) {
            guideView.visibility = View.VISIBLE
            val guideEmptyText = notExistDataText
            unitRv.visibility = View.INVISIBLE
            subjectChart.setData(listOf(), false)
            summaryTv.text = guideEmptyText
            achieveGuideTv.text = guideEmptyText
            unitGuideTv.text = guideEmptyText

            firstSubjectTv.visibility = View.GONE
            secondSubjectTv.visibility = View.GONE
            thirdSubjectTv.visibility = View.GONE

            firstUnitTv.visibility = View.GONE
            secondUnitTv.visibility = View.GONE
            thirdUnitTv.visibility = View.GONE

            firstProblemCntTv.visibility = View.GONE
            secondProblemCntTv.visibility = View.GONE
            thirdProblemCntTv.visibility = View.GONE

            firstTPC.visibility = View.GONE
            secondTPC.visibility = View.GONE

            thirdTPC.visibility = View.GONE

            firstRatingBorder.visibility = View.GONE
            secondRatingBorder.visibility = View.GONE
            thirdRatingBorder.visibility = View.GONE

            firstAchieveEmptyGuideTv.visibility = View.VISIBLE
            secondAchieveEmptyGuideTv.visibility = View.VISIBLE
            thirdAchieveEmptyGuideTv.visibility = View.VISIBLE
        }
    }

    private fun showSnackBar(text: String, buttonText: String) {
        if (snackBar?.isShowing == true) {
            snackBar?.dismiss()
        }

        if (snackBar == null) {
            val snackBarWindow = SnackBar(requireContext(), text, buttonText)
            snackBarWindow.setSnackBarViewListener(object : SnackBarViewListener {
                override fun onXBtnClicked(view: SnackBarView) {
                    snackBarWindow.dismiss()
                }

                override fun onActionBtnClicked(view: SnackBarView) {
                    activity?.finish()
                    val intent = Intent(TAB_SCROLL_EVENT)
                    intent.putExtra(TAB_SCROLL_EVENT_TARGET, "todayStudyView")
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                }
            })
            this.snackBar = snackBarWindow
        } else {
            snackBar!!.contentText = text
            snackBar!!.actionText = buttonText
        }

        snackBar!!.show()
    }
    inner class UnitAdapter: ExpandableAdapter<UnitHolder>() {
        override fun onBindViewHolder(holder: UnitHolder, position: Int, depth: Int) {
            val chapterTree = getItem(position) as ChapterTreeList
            val expanded = if (chapterTree.children.isEmpty())
                null
            else expanded(mItems[position])
            val chapterAnalysis = chapterTree.chapter
            holder.setDepth(depth)
            holder.setExpanded(expanded)
            holder.set(chapterAnalysis)

            holder.checkbox.setOnCheckedChangeListener(null)

            holder.checkbox.isChecked = selectedChapter.containsAll(chapterTree.leaf().map { it.chapter})
            holder.checkbox.setOnCheckedChangeListener { _, isCheckecd ->
                if(isCheckecd) {
                    selectedChapter.addAll(chapterTree.leaf().map { it.chapter})
                } else
                    selectedChapter.removeAll(chapterTree.leaf().map { it.chapter})

                notifyDataSetChanged()
                configureAllCheckBoxUI()
                Tutor.showToolTipIfNeed(holder.checkbox, Tutor.TooltipType.additionalStudyInAnalysis)
            }
            holder.itemView.setOnTouchListener { _, _ ->
                Tutor.showToolTipIfNeed(holder.checkbox, Tutor.TooltipType.additionalStudyInAnalysis)
                false
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitHolder {
            val itemBinding: ItemAnalysisUnitBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_analysis_unit, parent, false)
            return UnitHolder(itemBinding)
        }
    }
}

class UnitHolder(val itemBinding: ItemAnalysisUnitBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val expandableIndicator = itemBinding.expandableIndictor
    val unitTv = itemBinding.intentionTv
    val problemCntTv= itemBinding.problemCntTv
    val correctRateHb = itemBinding.correctRateHb
    val correctRateTv = itemBinding.correctRateTv
    val checkbox = itemBinding.checkbox
    val viewContext = itemBinding.root.context

    init {
        checkbox.extensionTouchArea(12.toPx())
    }

    fun setExpanded(expanded: Boolean?) {
        expandableIndicator.visibility = View.VISIBLE
        when (expanded) {
            true -> expandableIndicator.rotation = 90f
            false -> expandableIndicator.rotation = 0f
            else -> expandableIndicator.visibility = View.INVISIBLE
        }
    }

    fun setDepth(depth: Int) {
        val layoutParams = expandableIndicator.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.leftMargin = (23 + 14 * depth).toPx()

        if(depth == 0)
            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.white))
        else
            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.gray_100))
    }

    fun set(analysis: ChapterAnalysis) {
        unitTv.text = analysis.name
        problemCntTv.text = "${analysis.problemTotalNumber}문제"
        correctRateHb.value = analysis.myRate
        correctRateTv.text = TextUtils.percentFormat.format(analysis.myRate)

        if(analysis.myRate < 0.3)
            correctRateHb.progressColor = ContextCompat.getColor(viewContext, R.color.red_300)
        else if(analysis.myRate >= 0.3 && analysis.myRate < 0.7)
            correctRateHb.progressColor = ContextCompat.getColor(viewContext, R.color.yellow_200)
        else
            correctRateHb.progressColor = ContextCompat.getColor(viewContext, R.color.green_300)
    }
}

class ChapterTreeList(
        val chapter: ChapterAnalysis,
        override var children: List<ChapterTreeList> = listOf()
): ExpandableItem {
    fun leaf(): List<ChapterTreeList> {
        if(children.isEmpty())
            return listOf(this)
        else
            return children.map{ it.leaf() }.flatten()
    }
}

data class AchieveView(
        val subjectTv: TextView,
        val unitTv: TextView,
        val border: View,
        val problemCntTv: TextView,
        val tpc: TriplePenChart,
        val emptyGuideTv: TextView

) {
    fun showEmptyGuide() {
        subjectTv.visibility = View.GONE
        unitTv.visibility = View.GONE
        border.visibility = View.GONE
        problemCntTv.visibility = View.GONE
        tpc.visibility = View.GONE
        emptyGuideTv.visibility = View.VISIBLE
    }

    fun set(subject: String, unit: String, problemCnt: Int, myRate: Float, gradeRate: Float) {
        subjectTv.visibility = View.VISIBLE
        unitTv.visibility = View.VISIBLE
        border.visibility = View.VISIBLE
        problemCntTv.visibility = View.VISIBLE
        tpc.visibility = View.VISIBLE
        emptyGuideTv.visibility = View.GONE

        subjectTv.text = subject
        unitTv.text = unit
        problemCntTv.text = "${problemCnt}개"
        tpc.setValues(myRate, gradeRate, withAnim = true, withRangeColor = true)
    }
}