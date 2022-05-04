package com.freewheelin.pulley.revision2021.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.model.curation.MockReportCuration
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import com.freewheelin.pulley.views.charts.MockReportBarChartView
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*

class MockReportActivity : AppCompatActivity(), ArduousSpinnerListener {
    private val binding: ActivityMockReportBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_mock_report,null,false)
    }
    lateinit var examAnalysis: MockExamAnalysis
    lateinit var mockExam: MockExam

    val filteredProblemList: MutableList<MockExamProblem> = mutableListOf()
    var subjectTreeSet = HashSet<String>()
    var scoreTreeSet = HashSet<Int>()
    var killerTreeSet = HashSet<Boolean>()
    var scoreResultTreeSet = HashSet<Boolean>()

    val template: MockReportCuration
        get() = MockReportCuration(this)

    val scoreTabIds = mutableListOf<Int>()

    companion object {
        fun getIntent(context: Context, mockExam: MockExam, scoredStudentGoalInfo: ScoredStudentGoalInfo? = null): Intent {
            val intent = Intent(context, MockReportActivity::class.java)
            intent.putExtra(MockExamManager.ARG_MOCK_EXAM, mockExam)
            intent.putExtra(MockExamManager.ARG_SCORED_INFO, scoredStudentGoalInfo)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()

    }
    private fun init() {
        mockExam = intent.getSerializableExtra(MockExamManager.ARG_MOCK_EXAM) as MockExam

        MockExamManager.getMockExamReport(this, mockExam, user!!,
            successCB = {
                examAnalysis = it
                setUI()
            },
            failCB = {
                binding.loadingContainer.visibility = View.GONE
            }
        )

        binding.backBtn.extensionTouchArea(40)
        binding.backBtn.setOnClickListener { finish() }

        showSuccessToastIfNeed()
    }
    private fun setUI() {
        filteredProblemList.clear()
        filteredProblemList.addAll(examAnalysis.problemAnalysis?.problemList?: listOf())

        binding.apply {
            loadingContainer.visibility = View.GONE
            recyclerView.adapter = ReportAdapter(examAnalysis)
            recyclerView.layoutManager = LinearLayoutManager(this@MockReportActivity)

        }
    }

    private fun showSuccessToastIfNeed() {
        CoroutineScope(Dispatchers.IO).launch {
            val scoredInfo = intent.getSerializableExtra(MockExamManager.ARG_SCORED_INFO) as? ScoredStudentGoalInfo
            delay(2000)

            withContext(Dispatchers.Main) {
                if (scoredInfo?.isNeedToShowCompletedToast() == true) {
                    SuccessToast.show(this@MockReportActivity, "목표달성 ${scoredInfo.continuousGoalCount}일째","하루 ${scoredInfo.goalProblemCount}문제 풀기 성공")
                }
            }
        }
    }

    fun onReviewBtnClicked(problem: Problem) {
        val intent = SolveActivity.getReviewIntent(this, mockExam, problem.id)
        startActivity(intent)
    }

    var summeryBinding: ItemMockReportSummaryBinding? = null
    var subjectBinding: ItemMockReportSubjectBinding? = null
    var scoreBinding: ItemMockReportScoreBinding? = null
    var problemTopBinding: ItemMockReportProblemTopBinding? = null
    var problemMiddleBinding: ItemMockReportProblemMiddleBinding? = null

    inner class ReportAdapter(private val mockExamAnalysis: MockExamAnalysis): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val typeSummary = 0
        private val typeSubject = 1
        private val typeScore = 2
        private val typeProblemTop = 3
        private val typeProblemMiddle = 4
        private val typeProblemBottom = 5
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeSummary -> {
                    ReportSummaryHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_summary, parent, false))
                }
                typeSubject -> {
                    ReportSubjectHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_subject, parent, false))
                }
                typeScore -> {
                    ReportScoreHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_score, parent, false))
                }
                typeProblemTop -> {
                    ReportProblemTopHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_problem_top, parent, false))
                }
                typeProblemMiddle -> {
                    ReportProblemMiddleHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_problem_middle, parent, false))
                }
                else -> { // typeProblemBottom
                    ReportProblemBottomHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_problem_bottom, parent, false))
                }
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val totalListSize = filteredProblemList.size + 5
            val problemBottomIdx = totalListSize - 1

            when (position) {
                typeSummary -> { (holder as ReportSummaryHolder).bind(mockExamAnalysis) }
                typeSubject -> { (holder as ReportSubjectHolder).bind(mockExamAnalysis) }
                typeScore -> { (holder as ReportScoreHolder).bind(mockExamAnalysis) }
                typeProblemTop -> { (holder as ReportProblemTopHolder).bind(mockExamAnalysis) }
                problemBottomIdx -> { (holder as ReportProblemBottomHolder).bind(mockExamAnalysis) }
                else -> { (holder as ReportProblemMiddleHolder).bind(position - 4) }
            }
        }
        override fun getItemViewType(position: Int): Int {
            val totalListSize = filteredProblemList.size + 5
            val problemBottomIdx = totalListSize - 1

            return when(position) {
                0 -> typeSummary
                1 -> typeSubject
                2 -> typeScore
                3 -> typeProblemTop
                problemBottomIdx -> typeProblemBottom
                else -> typeProblemMiddle
            }
        }

        override fun getItemCount(): Int {
            val totalListSize = filteredProblemList.size + 5
            return totalListSize
        }
    }

    inner class ReportSummaryHolder(private val itemBinding: ItemMockReportSummaryBinding): RecyclerView.ViewHolder(itemBinding.root) {
        init {
            summeryBinding = itemBinding
        }
        fun bind(analysis: MockExamAnalysis) {
            val summaryAnalysis = analysis.summaryAnalysis
            itemBinding.apply {
                oneSummaryContainerCl.visibility = if (examAnalysis.showAllSummary) View.INVISIBLE else View.VISIBLE
                summaryContainerCl.visibility = if (examAnalysis.showAllSummary) View.VISIBLE else View.INVISIBLE


                if(examAnalysis.showAllSummary) {
                    summaryAnalysis?.let{
                        scorePercentTv.text = "${it.correctRate}%"
                        scoreCountTv.text = "${it.correctCount}/${it.totalNumber}"

                        binding.titleTv.text = mockExam.getMockTitle()
                        binding.twinsSupportTv?.visibility = if (mockExam.examType?.isTwins == true) View.VISIBLE else View.GONE

                        scoreTv.text = it.score.toString() + "점"
                        percentageTv.text = "${it.percent}%"
                        ratingTv.text = it.rating.toString() + "등급"
                        timeTv.text = "걸린시간 : " + examAnalysis.myTimeStr

                        ratingGuideTv.text = template.getSummaryQ(it.rating, it.score, it.higherRatingScore)

                        myScoreArrowTv.text = "내 위치\n${it.score}점"
                        myScoreGuide.layoutParams = (myScoreGuide.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                            guidePercent = it.score * 0.01f
                        }

                        belowScoreArrowTv.text = "${it.rating}등급\n${it.sameRatingScore}점"
                        belowScoreGuide.layoutParams = (belowScoreGuide.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                            guidePercent = it.sameRatingScore * 0.01f
                        }

                        if(it.rating - 1 > 0) {
                            upperScoreArrowTv.text = "${it.rating- 1}등급\n${it.higherRatingScore}점"
                            upperScoreGuide.layoutParams = (upperScoreGuide.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                                guidePercent = it.higherRatingScore * 0.01f
                            }
                        }

                        val animationListener = object: Animator.AnimatorListener {
                            override fun onAnimationRepeat(p0: Animator?) {}
                            override fun onAnimationEnd(p0: Animator?) {
                                belowScoreArrowTv.show()
                                belowScoreArrowIv.show()
                                belowScoreBorder.show()
                                if(it.rating - 1 > 0) {
                                    upperArrowIv.show()
                                    upperScoreBorder.show()
                                    upperScoreArrowTv.show()
                                }
                                myScoreArrowIv.show()
                                myScoreArrowTv.show()
                            }
                            override fun onAnimationStart(p0: Animator?) {}
                            override fun onAnimationCancel(p0: Animator?) {}
                        }

                        belowScoreArrowIv.visibility = View.GONE
                        belowScoreArrowTv.visibility = View.GONE
                        belowScoreBorder.visibility = View.GONE
                        upperArrowIv.visibility = View.GONE
                        upperScoreArrowTv.visibility = View.GONE
                        upperScoreBorder.visibility = View. GONE

                        myScoreArrowIv.visibility = View.GONE
                        myScoreArrowTv.visibility = View.GONE
                        scoreBarView.set(it.score.toFloat() * 0.01f, true, listener = animationListener, delay = 100)
                    }

                } else {
                    val correctRate = summaryAnalysis?.correctRate ?: 0
                    val totalCount = summaryAnalysis?.totalNumber ?: 0
                    val correctCount = summaryAnalysis?.correctCount ?: 0

                    binding.titleTv.text = mockExam.getMockTitle()
                    binding.twinsSupportTv?.visibility = if (mockExam.examType?.isTwins == true) View.VISIBLE else View.GONE
                    ratingGuideTv2.text = template.getSummaryP(correctRate)

                    scorePercentTv2.text = "${correctRate}%"
                    scoreCountTv2.text = "${correctCount}/${totalCount}"

                    timeTv2.text = "걸린시간 : " + examAnalysis.myTimeStr


                    myScoreArrowTv2.text = "정답률\n${correctRate}%"
                    myScoreGuide2.layoutParams = (myScoreGuide2.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                        guidePercent = correctRate * 0.01f
                    }

                    val animationListener = object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {
                            myScoreArrowIv2.show()
                            myScoreArrowTv2.show()
                        }
                        override fun onAnimationStart(p0: Animator?) {}
                        override fun onAnimationCancel(p0: Animator?) {}
                    }

                    Log.d("모의고사보고서", "correctRate=${correctRate}")

                    scoreBarView2.set(correctRate.toFloat() * 0.01f, true, listener = animationListener, delay = 100)
                }

            }
        }
    }
    inner class ReportSubjectHolder(private val itemBinding: ItemMockReportSubjectBinding): RecyclerView.ViewHolder(itemBinding.root) {
        val subjectTabIds = mutableListOf<Int>()

        private var subjectAnalysis: SubjectAnalysis? = null
        init {
            subjectBinding = itemBinding
        }
        fun bind(analysis: MockExamAnalysis) {
            subjectAnalysis = analysis.subjectAnalysis
            itemBinding.apply {

                setSubjectCuration(subjectAnalysis?.goodCuration, subjectAnalysis?.badCuration)

                subjectAnalysis?.report?.firstOrNull { it.subjectCode == "NONE"}?.let { none ->
                    if (subjectAnalysis?.report?.contains(none) == false) {
                        subjectAnalysis?.report?.add(none)
                    }
                }

                var tabTitles = subjectAnalysis?.report?.map { it.subjectName }?.toMutableList()?: mutableListOf()

                setTabs(tabTitles, subjectTab, subjectTabIds)
                subjectTab.setOnCheckedChangeListener { group, checkedId ->

                    for ((idx, tabId) in subjectTabIds.withIndex()) {
                        if (tabId == checkedId) {
                            subjectAnalysis?.report?.get(idx)?.let { subjectReport ->
                                setRingChart(idx)
                                setSubjectBarChart(idx)
                            }
                        }
                    }
                }
                defaultSelectFirstIdx(itemBinding.subjectTab.children.iterator())
            }
        }

        private fun setSubjectCuration(good: CurationTitle?, bad:CurationTitle?) {
            try {
                if (good != null) {
                    val text = makeCurationText(good.template, good.values)
                    itemBinding.subjectAnalysisGuideTv.text = text
                } else {
                    itemBinding.subjectAnalysisGuideTv2.setPaddingTop(resources.getDimensionPixelSize(R.dimen.dp40))
                    itemBinding.subjectAnalysisGuideTv.visibility = View.GONE
                }

                if (bad != null) {
                    val text = makeCurationText(bad.template, bad.values)
                    itemBinding.subjectAnalysisGuideTv2.text = text
                } else itemBinding.subjectAnalysisGuideTv2.visibility = View.GONE
            } catch ( e:Exception) {
                Log.e("MockReportActivity", "error=${e.localizedMessage}")
            }
        }
        private fun setTabs(tabTitles:List<String>, container: LinearLayout, idList:MutableList<Int>) {
            if (container.childCount > 0) return
            for((idx,title) in tabTitles.withIndex()) {
                val viewId = ViewUtils.generateViewId()
                idList.add(idx, viewId)
                addTab(title, container, viewId)
            }
        }
        private fun addTab(tabTitle: String, container: LinearLayout, id:Int) {
            val tab = LayoutInflater.from(itemBinding.root.context).inflate(R.layout.item_mock_report_subject_tab_bg, container, false) as RadioButton
            tab.id = id
            tab.text = tabTitle
            container.addView(tab)
        }

        private fun setRingChart(idx:Int) {
            subjectAnalysis?.report?.get(idx)?.let { report ->
                val correct = report.totalCorrectRate
                val wrong = 100 - report.totalCorrectRate
                itemBinding.ringChart.visibility = View.VISIBLE
                itemBinding.ringChart.setData(wrong.toFloat(), correct.toFloat())
                itemBinding.tvCorrectRate.text = "${correct}%"
            }
        }

        fun setSubjectBarChart(idx: Int) {
            examAnalysis.subjectAnalysis?.report?.get(idx)?.let { subjectReport ->
                val barChartList: List<MockReportBarChartView> = listOf(itemBinding.subjectBarChartView1, itemBinding.subjectBarChartView2, itemBinding.subjectBarChartView3)

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        barChartList.forEachIndexed { index, chartView ->
                            chartView.visibility = if (index >= subjectReport.chapterList.size) View.INVISIBLE else View.VISIBLE
                        }
                        subjectReport.chapterList.forEachIndexed { idx, chapter ->
                            val barChart = barChartList[idx]
                            val leftTitle = chapter.chapterName
                            val leftSub = "${chapter.totalNumber}문항"
                            val rightTitle = "내 정답률 ${chapter.myCorrectRate}%"
                            val rightSub =
                                "${chapter.myRating}등급 평균 ${chapter.sameRatingCorrectRate}%"
                            barChart.setTitles(leftTitle, leftSub, rightTitle, rightSub)
                            barChart.setMainColorWithPercent(chapter.myCorrectRate)
                            barChart.setSubPercent(chapter.sameRatingCorrectRate)
                            barChart.show(examAnalysis.showAllSummary)
                        }
                    }
                }
            }
        }
    }
    inner class ReportScoreHolder(private val itemBinding: ItemMockReportScoreBinding): RecyclerView.ViewHolder(itemBinding.root) {
        init {
            scoreBinding = itemBinding
        }
        fun bind(analysis: MockExamAnalysis) {
            val scoreAnalysis = analysis.scoreAnalysis
            itemBinding.apply {
                setScoreCuration(examAnalysis.scoreAnalysis?.goodCuration, examAnalysis.scoreAnalysis?.badCuration)

                scoreAnalysis?.report?.firstOrNull { it.subject == "교육과정 외"}?.let { none ->
                    scoreAnalysis.report.remove(none)
                    scoreAnalysis.report.add(none)
                }

                var tabTitles = scoreAnalysis?.report?.map { it.subject }?.toMutableList()?: mutableListOf()

                setTabs(tabTitles, scoreTab, scoreTabIds)
                scoreTab.setOnCheckedChangeListener { group, checkedId ->
                    for((idx, tabId) in scoreTabIds.withIndex()) {
                        if(tabId == checkedId) {
                            scoreAnalysis?.report?.get(idx)?.let { scoreReport ->
                                setScoreBarChart(idx)
                            }
                        }
                    }
                }

                setScoreBarChart(0)

                defaultSelectFirstIdx(itemBinding.scoreTab.children.iterator())
            }
        }

        private fun setScoreCuration(good: CurationTitle?, bad:CurationTitle?) {
            try {
                if (good != null) {
                    val text = makeCurationText(good.template, good.values)
                    itemBinding.scoreAnalysisGuideTv.text = text
                } else {
                    itemBinding.scoreAnalysisGuideTv2.setPaddingTop(resources.getDimensionPixelSize(R.dimen.dp40))
                    itemBinding.scoreAnalysisGuideTv.visibility = View.GONE
                }

                if (bad != null) {
                    val text = makeCurationText(bad.template, bad.values)
                    itemBinding.scoreAnalysisGuideTv2.text = text
                } else itemBinding.scoreAnalysisGuideTv2.visibility = View.GONE
            } catch ( e:Exception) {
                Log.e("MockReportActivity", "error=${e.localizedMessage}")
            }
        }
        private fun setTabs(tabTitles:List<String>, container: LinearLayout, idList:MutableList<Int>) {
            if (container.childCount > 0) return
            for((idx,title) in tabTitles.withIndex()) {
                val viewId = ViewUtils.generateViewId()
                idList.add(idx, viewId)
                addTab(title, container, viewId)
            }
        }
        private fun setScoreBarChart(idx:Int) {
            examAnalysis.scoreAnalysis?.report?.get(idx)?.let { scoreReport ->

                val barChartList: List<MockReportBarChartView> = listOf(itemBinding.scoreBarChartView1, itemBinding.scoreBarChartView2, itemBinding.scoreBarChartView3)

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        println("tpehf , scoreReport.pointProblemList : ${scoreReport.pointProblemList.size}")
                        barChartList.forEachIndexed { index, chartView ->
                            chartView.visibility = if (index >= scoreReport.pointProblemList.size) View.INVISIBLE else View.VISIBLE
                        }
                        scoreReport.pointProblemList.forEachIndexed { idx, problem ->
                            val barChart = barChartList[idx]

                            val leftTitle = "${problem.point}점 문항"
                            val leftSub = "${problem.totalNumber}문항"
                            val rightTitle = "내 정답률 ${problem.myCorrectRate}%"
                            val rightSub = "${problem.myRating}등급 평균 ${problem.sameRatingCorrectRate}%"

                            barChart.setTitles(leftTitle, leftSub, rightTitle, rightSub)
                            barChart.setMainColorWithPercent(problem.myCorrectRate)
                            barChart.setSubPercent(problem.sameRatingCorrectRate)
                            barChart.show(examAnalysis.showAllSummary)
                        }
                    }
                }
            }
        }
    }
    inner class ReportProblemTopHolder(private val itemBinding: ItemMockReportProblemTopBinding): RecyclerView.ViewHolder(itemBinding.root) {
        init {
            problemTopBinding = itemBinding
        }
        fun bind(analysis: MockExamAnalysis) {
            val problemAnalysis = analysis.problemAnalysis
            itemBinding.apply {
                setProblemCuration(examAnalysis.problemAnalysis?.curation)
                setFilters()

            }
        }
        fun setProblemCuration(curation: CurationTitle?) {
            try {
                if (curation != null) {
                    val text = makeCurationText(curation.template, curation.values)
                    itemBinding.problemAnalysisGuideTv.text = text
                } else {
                    scoreBinding?.scoreAnalysisGuideTv?.visibility = View.GONE
                }

            } catch ( e:Exception) {
                Log.e("MockReportActivity", "error=${e.localizedMessage}")
            }
        }
        private fun setFilters() {
            with(itemBinding) {
                subjectFilter.listener = this@MockReportActivity
                scoreFilter.listener = this@MockReportActivity
                killerFilter.listener = this@MockReportActivity
                scoreResultFilter.listener = this@MockReportActivity

                val subjectFilterList = mutableListOf<String>()
                subjectFilterList.add(0, "과목 전체")
                examAnalysis.problemAnalysis?.problemList?.map { it.subject }?.toHashSet()?.let {
                    subjectTreeSet = it
                }

                subjectFilterList.addAll(subjectTreeSet)
                subjectFilter.items = subjectFilterList

                scoreTreeSet = hashSetOf(2, 3, 4)
                scoreFilter.items = listOf("배점 전체", "2점", "3점", "4점")
                killerTreeSet = hashSetOf(true, false)
                killerFilter.items = listOf("킬러 전체", "킬러 있음", "킬러 없음")
                scoreResultTreeSet = hashSetOf(true, false)
                scoreResultFilter.items = listOf("채점결과 전체", "정답", "오답")
            }
        }
    }
    inner class ReportProblemMiddleHolder(private val itemBinding: ItemMockReportProblemMiddleBinding): RecyclerView.ViewHolder(itemBinding.root) {
        init {
            problemMiddleBinding = itemBinding
        }
        fun bind(idx: Int) {
            val problem = filteredProblemList[idx]
            itemBinding.apply {
                val bgDrawable = if(idx % 2 == 0) R.drawable.bg_gray_300_stroke_top_gray else R.drawable.bg_gray_300_stroke_top
                problemMiddleLl.setBackgroundResource(bgDrawable)

                tvNum.text = "${problem.problemNum}"
                tvSubject.text = problem.subject
                tvBigUnit.text = problem.bigChapter
                tvType.text = problem.unit
                tvPoint.text = "${problem.point}"

                ivKiller.visibility = if(problem.isKiller) View.VISIBLE else View.INVISIBLE

                if(problem.isCorrect){
                    ivResult.setImageResource(R.drawable.ic_mock_report_result_o)
                } else {
                    ivResult.setImageResource(R.drawable.ic_mock_report_result_x)
                }
            }
        }
    }
    inner class ReportProblemBottomHolder(private val itemBinding: ItemMockReportProblemBottomBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(analysis: MockExamAnalysis) {

        }

    }
    private fun defaultSelectFirstIdx (tabs: Iterator<View>) {
        var count = 0
        for (child in tabs) {
            if (count == 0) {
                (child as AppCompatRadioButton).isChecked = true
            }
            count += 1
        }
    }
    fun addTab(tabTitle: String, container: LinearLayout, id:Int) {
        val tab = LayoutInflater.from(this).inflate(R.layout.item_mock_report_subject_tab_bg, container, false) as RadioButton
        tab.id = id
        tab.text = tabTitle
        container.addView(tab)
    }

    private fun makeCurationText(text:String, values:List<String>?) : CharSequence {
        return if(values != null && values.isNotEmpty()) {
            var result = text
            // 텍스트 교체
            for(value in values) {
                result = result.replaceFirst("%s", value)
            }
            var spannable:CharSequence = result
            for(value in values) {
                spannable = spannable.partialFont(Theme.extraBold(this), value)
            }
            spannable
        } else {
            text
        }
    }

    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        problemTopBinding?.apply {
             val itemName = when(view) {
                subjectFilter -> "필터-과목"
                scoreFilter -> "필터-배점"
                killerFilter -> "필터-킬러"
                else -> "필터-채점결과"
            }

            val itemValue = view.items[position]
            LogUtils.logEvent(this@MockReportActivity, user!!, PulleyEvent.BUTTON_CLICK, "모의고사보고서", itemName, itemValue)

            var filtered = testFilter(examAnalysis.problemAnalysis?.problemList ?: listOf(), subjectFilter)

            filtered = testFilter(ArrayList(filtered), scoreFilter)
            filtered = testFilter(ArrayList(filtered), killerFilter)
            filtered = testFilter(ArrayList(filtered), scoreResultFilter)

            filteredProblemList.clear()
            filteredProblemList.addAll(filtered)
            binding.recyclerView.adapter?.notifyDataSetChanged()
    //        adapter.notifyDataSetChanged()
        }

    }

    private fun testFilter(exams: List<MockExamProblem>, filter: ArduousSpinner): List<MockExamProblem> {
        val position = filter.position
        if (position != null && position > 0) {
            if (filter === problemTopBinding?.subjectFilter) {
                return exams.filter {
                    it.subject == ArrayList(subjectTreeSet).get(position - 1)
                }
            } else if (filter === problemTopBinding?.scoreFilter) {
                return exams.filter {
                    it.point == ArrayList(scoreTreeSet).get(position - 1)
                }
            } else if (filter === problemTopBinding?.killerFilter) {
                return exams.filter {
                    it.isKiller == ArrayList(killerTreeSet.reversed()).get(position - 1)
                }
            } else {
                return exams.filter {
                    it.isCorrect == ArrayList(scoreResultTreeSet.reversed()).get(position - 1)
                }
            }
        } else {
            return exams
        }
    }
}


