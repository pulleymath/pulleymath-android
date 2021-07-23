package com.freewheelin.pulley.activities.analysis.tabFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.activities.analysis.AnalysisTabDelegate
import com.freewheelin.pulley.activities.analysis.AnanlysisTabActivityInterface
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ContentManager
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.dialogs.WrongManagementDialog
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.Analysis
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.curation.MyCuration
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.SnackBar.SnackBar
import com.freewheelin.pulley.views.SnackBar.SnackBarView
import com.freewheelin.pulley.views.SnackBar.SnackBarViewListener
import com.freewheelin.pulley.views.charts.TriplePenChart
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableAdapter
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableItem
import kotlinx.android.synthetic.main.activity_analysis_tab.*
import kotlinx.android.synthetic.main.dialog_wrong_management.*
import kotlinx.android.synthetic.main.fragment_analysis_unit.*
import kotlinx.android.synthetic.main.item_analysis_unit.view.*
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
        fun newInstance() = AnalysisUnitFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analysis_unit, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        achieveOrderRadio.textSize = 20f
        achieveOrderRadio.labels = listOf("낮은 성취도 TOP3", "높은 성취도 TOP3")
        achieveOrderRadio.listener = this

        initUI()
        configureUI(from, to, DateTimeUtils.getPeriod(from, to))
        unitRv.layoutManager = LinearLayoutManager(context)
        unitRv.isFocusable = false
        activity?.scrollView?.scrollTo(0, scrollPosition)
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        configureAchieveSectionUI()
    }

    override fun onPeriodSelected(from: LocalDate, to: LocalDate, period: Int) {
        super.onPeriodSelected(from, to, period)
        selectedChapter.clear()
        if(from > LocalDate.now() || analysis == null)
            configureEmptyUI(from, to)
        else
            configureUI(from, to,  period)
    }

    override fun onItemChanged(set: ObservableHashSet<ChapterAnalysis>) {
        if(set.isEmpty()) {
            unitSelectGuideTv.text = ""
            learnBtn.toDisableUI()
            reviewBtn.toDisableUI()
        } else {
            val problemCount = set.sumBy { it.problemTotalNumber }
            unitSelectGuideTv.text = "${problemCount}개의 문제를 학습합니다."
            learnBtn.toEnableUI()
            reviewBtn.toEnableUI()
        }
    }

    private fun initUI() {
        unitSelectGuideTv.text = ""
        selectedChapter.listener = this
        selectedChapter.clear()
        learnBtn.setPermissionClickListener {
            if(learnBtn.isEnableUI()) {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "내분석보기", "추가학습하기")
                val dialog = WrongManagementDialog(requireContext(), WrongManagementDialog.Type.scrap)
                dialog.configureUIByChapter(selectedChapter)
                dialog.show()
                dialog.makeBtn.setOnClickListener {
                    LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "내분석보기", "단원 학습지 만들기")
                    dialog.makeBtn.startLoding()
                    val cntPerProblem = dialog.cnt
                    val isSimilar = dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom
                    val level = dialog.level

                    val isIncludeClearProblem = dialog.isClearInclude

                    PieceManager.makeWeakPieceUsingChapters(requireContext(), user!!, selectedChapter.toList(),
                            isSimilar, level, cntPerProblem, isIncludeClearProblem,
                            from.toDate(),
                            to.toDate(),
                            successCB = {
                                dialog.dismiss()

                                if (dialog.checkbox.isChecked) {
                                    val intent = SolveActivity.getIntent(requireContext(), it)
                                    startActivity(intent)
                                } else {
                                    val text: String
                                    if (selectedChapter.size == 1)
                                        text = "'${selectedChapter.first().name}'의 오답관리 문제가 만들어졌습니다."
                                    else
                                        text = "'${selectedChapter.first().name}' 외 ${selectedChapter.size - 1}개의 오답관리 문제가 만들어졌습니다."

                                    showSnackBar(text, "바로가기")
                                }

                                selectedChapter.clear()
                            },
                            failCB = {
                                dialog.dismiss()
                                DaebakToast.showFailedMakePiece(requireContext())
                            }
                    )
                }
            }
        }

        reviewBtn.setPermissionClickListener {
            if(reviewBtn.isEnableUI()) {
                ContentManager.getReview(requireContext(), user!!, selectedChapter.toList(), from.toDate(), to.toDate()) {
                    val intent = SolveActivity.getReviewIntent(requireContext(), it, false)
                    startActivity(intent)
                }
            }
        }
    }

    private fun configureUI(from: LocalDate, to: LocalDate, period: Int) {
        configureSummarySectionUI(period)
        configureAchieveSectionUI()
        configureUnitSectionUI()
    }

    private fun configureAllCheckBoxUI() {
        allCheckBox.setOnCheckedChangeListener(null)
        val chapters = chapterTreeList?.map { it.leaf() }?.flatten()?.map { it.chapter } ?: listOf()
        allCheckBox.isChecked = selectedChapter.containsAll(chapters)
        allCheckBox.setOnCheckedChangeListener { button, isChecked ->
            if(isChecked)
                selectedChapter.addAll(chapters)
            else
                selectedChapter.removeAll(chapters)
            unitRv.adapter?.notifyDataSetChanged()
        }
    }

    private fun configureSummarySectionUI(period: Int) {

        subjectChart.setData(analysis?.getUnitSummaryData() ?: listOf(), true)
        subjectChart.setDetailBtnVisibility(View.GONE)

        val myRating = analysis?.myRating
        if (myRating == 1)
            subjectChart.setSelectedBarLabel("1등급\n평균", "나의\n정답률", null)
        else if (myRating != null)
            subjectChart.setSelectedBarLabel("${myRating}등급\n평균", "나의\n정답률", "${myRating - 1}등급\n평균")

        subjectChart.selectedBar = subjectChart.bars?.first()
        subjectChart.bars?.first()?.isSelectedDetailBtn = true
        subjectChart.requestLayout()

        if (analysis == null)
            summaryTv.text = template.dataNotExistText
        else
            summaryTv.text = template.getUnitSummaryQ(period, analysis?.improvement, analysis!!.summaryAnalysis)
    }

    private fun configureAchieveSectionUI() {
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
        unitRv.visibility = View.VISIBLE
        this.chapterTreeList = treeList

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
        allCheckBox.setOnCheckedChangeListener { button, isChecked ->
            if(isChecked)
                selectedChapter.addAll(chapters)
            else
                selectedChapter.removeAll(chapters)
            adapter.notifyDataSetChanged()
            Tutor.showToolTipIfNeed(allCheckBox, Tutor.TooltipType.additionalStudyInAnalysis)
        }
    }

    fun configureEmptyUI(from: LocalDate, to: LocalDate) {
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
//                    snackBarWindow.dismiss()
                    val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                    intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 1)
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
            holder.checkbox.setOnCheckedChangeListener { button, isCheckecd ->
                if(isCheckecd) {
                    selectedChapter.addAll(chapterTree.leaf().map { it.chapter})
                } else
                    selectedChapter.removeAll(chapterTree.leaf().map { it.chapter})

                notifyDataSetChanged()
                configureAllCheckBoxUI()
                Tutor.showToolTipIfNeed(holder.checkbox, Tutor.TooltipType.additionalStudyInAnalysis)
            }
            holder.itemView.setOnTouchListener { p0, p1 ->
                Tutor.showToolTipIfNeed(holder.checkbox, Tutor.TooltipType.additionalStudyInAnalysis)
                false
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_analysis_unit, parent, false)
            return UnitHolder(view)
        }
    }
}

class UnitHolder(val view: View): RecyclerView.ViewHolder(view) {
    val expandableIndicator = view.expandableIndictor
    val unitTv = view.unitTv
    val problemCntTv= view.problemCntTv
    val correctRateHb = view.correctRateHb
    val correctRateTv = view.correctRateTv
    val checkbox = view.checkbox

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
            itemView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white_ffffff))
        else
            itemView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white_fafafa))
    }

    fun set(analysis: ChapterAnalysis) {
        unitTv.text = analysis.name
        problemCntTv.text = "${analysis.problemTotalNumber}문제"
        correctRateHb.value = analysis.myRate
        correctRateTv.text = TextUtils.percentFormat.format(analysis.myRate)

        if(analysis.myRate < 0.3)
            correctRateHb.progressColor = ContextCompat.getColor(view.context, R.color.red_fe7b67)
        else if(analysis.myRate >= 0.3 && analysis.myRate < 0.7)
            correctRateHb.progressColor = ContextCompat.getColor(view.context, R.color.yellow_ffd545)
        else
            correctRateHb.progressColor = ContextCompat.getColor(view.context, R.color.green_70d000)
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