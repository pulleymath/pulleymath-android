package com.freewheelin.pulley.activities

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.ActivityMockReportBinding
import com.freewheelin.pulley.databinding.ActivityTestReportDailyBinding
import com.freewheelin.pulley.databinding.ItemMockReportProblemListBinding
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.model.curation.MockReportCuration
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import com.freewheelin.pulley.views.charts.MockReportBarChartView
import java.lang.Exception
import java.util.*

class MockReportActivity : AppCompatActivity(), ArduousSpinnerListener {
    private val binding: ActivityMockReportBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this),R.layout.activity_mock_report,null,false)
    }
    lateinit var examAnalysis: MockExamAnalysis
    lateinit var adapter: MockReportProblemAdapter
    lateinit var mockExam: MockExam

    val filteredProblemList: MutableList<MockExamProblem> = mutableListOf()
    var subjectTreeSet = HashSet<String>()
    var scoreTreeSet = HashSet<Int>()
    var killerTreeSet = HashSet<Boolean>()
    var scoreResultTreeSet = HashSet<Boolean>()

    val template: MockReportCuration
        get() = MockReportCuration(this)

    val subjectTabIds = mutableListOf<Int>()
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
                configureUI()
            },
            failCB = {
                binding.loadingContainer.visibility = View.GONE
            }
        )

        binding.backBtn.extensionTouchArea(40)
        binding.backBtn.setOnClickListener { finish() }

        showSuccessToastIfNeed()
    }

    private fun showSuccessToastIfNeed() {
        Handler(Looper.getMainLooper()).postDelayed({
            val scoredInfo = intent.getSerializableExtra(MockExamManager.ARG_SCORED_INFO) as? ScoredStudentGoalInfo
            if (scoredInfo?.isNeedToShowCompletedToast() == true) {
                SuccessToast.show(this, "목표달성 ${scoredInfo.continuousGoalCount}일째","하루 ${scoredInfo.goalProblemCount}문제 풀기 성공")
            }
        }, 2000)
    }

    fun onReviewBtnClicked(problem: Problem) {
        val intent = SolveActivity.getReviewIntent(this, mockExam, problem.id)
        startActivity(intent)
    }

    fun configureUI() {
        binding.loadingContainer.visibility = View.GONE
        configureSummaryUI()
        setSubjectUI()
        setScoreUI()
        setProblemUI()
        setFilters()
    }

    private fun setFilters() {
        with(binding) {
            subjectFilter.listener = this@MockReportActivity
            scoreFilter.listener = this@MockReportActivity
            killerFilter.listener = this@MockReportActivity
            scoreResultFilter.listener = this@MockReportActivity

            val subjectFilterList = mutableListOf<String>()
            subjectFilterList.add(0, "과목 전체")
            subjectTreeSet = examAnalysis.problemAnalysis?.problemList?.map { it.subject }?.toHashSet()?: hashSetOf()
            subjectFilterList.addAll(subjectTreeSet)
            subjectFilter.items = subjectFilterList

            scoreTreeSet = hashSetOf(2,3,4)
            scoreFilter.items = listOf("배점 전체", "2점", "3점", "4점")
            killerTreeSet = hashSetOf(true, false)
            killerFilter.items = listOf("킬러 전체", "킬러 있음", "킬러 없음")
            scoreResultTreeSet = hashSetOf(true, false)
            scoreResultFilter.items = listOf("채점결과 전체", "정답", "오답")
        }

    }

    private fun configureSummaryUI() {
        Log.d("모의고사보고서", "showAllSummary=${examAnalysis.showAllSummary}")
        if(examAnalysis.showAllSummary) {
            setAllSummaryHeader()
        } else {
            setOneSummaryHeader()
        }
    }

    private fun setOneSummaryHeader() {
        with(binding) {
            oneSummaryContainerCl.visibility = View.VISIBLE
            summaryContainerCl.visibility = View.INVISIBLE

            val correctRate = examAnalysis.summaryAnalysis?.correctRate ?: 0
            val totalCount = examAnalysis.summaryAnalysis?.totalNumber ?: 0
            val correctCount = examAnalysis.summaryAnalysis?.correctCount ?: 0

            titleTv.text = mockExam.getMockTitle()
            twinsSupportTv.visibility = if (mockExam.examType?.isTwins == true) View.VISIBLE else View.GONE
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

    private fun setAllSummaryHeader() {
        with(binding) {
            oneSummaryContainerCl.visibility = View.INVISIBLE
            summaryContainerCl.visibility = View.VISIBLE

            examAnalysis.summaryAnalysis?.let{
                scorePercentTv.text = "${it.correctRate}%"
                scoreCountTv.text = "${it.correctCount}/${it.totalNumber}"

                titleTv.text = mockExam.getMockTitle()
                twinsSupportTv.visibility = if (mockExam.examType?.isTwins == true) View.VISIBLE else View.GONE

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
        }
    }

    fun setSubjectUI() {

        setSubjectCuration(examAnalysis.subjectAnalysis?.goodCuration, examAnalysis.subjectAnalysis?.badCuration)

        examAnalysis.subjectAnalysis?.report?.firstOrNull { it.subjectCode == "NONE"}?.let { none ->
            examAnalysis.subjectAnalysis?.report?.remove(none)
            examAnalysis.subjectAnalysis?.report?.add(none)
        }

        var tabTitles = examAnalysis.subjectAnalysis?.report?.map { it.subjectName }?.toMutableList()?: mutableListOf()

        setTabs(tabTitles, binding.subjectTab, subjectTabIds)
        binding.subjectTab.setOnCheckedChangeListener { group, checkedId ->
            for ((idx, tabId) in subjectTabIds.withIndex()) {
                if (tabId == checkedId) {
                    examAnalysis.subjectAnalysis?.report?.get(idx)?.let { subjectReport ->
                        setRingChart(idx)
                        setSubjectBarChart(idx)
                    }
                }
            }
        }

        subjectTabIds.firstOrNull()?.let {
            findViewById<RadioButton>(it).isChecked = true
        }
    }

    fun setSubjectCuration(good: CurationTitle?, bad:CurationTitle?) {
        try {
            if (good != null) {
                val text = makeCurationText(good.template, good.values)
                binding.subjectAnalysisGuideTv.text = text
            } else {
                binding.subjectAnalysisGuideTv2.setPaddingTop(resources.getDimensionPixelSize(R.dimen.dp40))
                binding.subjectAnalysisGuideTv.visibility = View.GONE
            }

            if (bad != null) {
                val text = makeCurationText(bad.template, bad.values)
                binding.subjectAnalysisGuideTv2.text = text
            } else binding.subjectAnalysisGuideTv2.visibility = View.GONE
        } catch ( e:Exception) {
            Log.e("MockReportActivity", "error=${e.localizedMessage}")
        }
    }

    fun setScoreUI() {

        setScoreCuration(examAnalysis.scoreAnalysis?.goodCuration, examAnalysis.scoreAnalysis?.badCuration)

        examAnalysis.scoreAnalysis?.report?.firstOrNull { it.subject == "교육과정 외"}?.let { none ->
            examAnalysis.scoreAnalysis?.report?.remove(none)
            examAnalysis.scoreAnalysis?.report?.add(none)
        }

        var tabTitles = examAnalysis.scoreAnalysis?.report?.map { it.subject }?.toMutableList()?: mutableListOf()

        setTabs(tabTitles, binding.scoreTab, scoreTabIds)
        binding.scoreTab.setOnCheckedChangeListener { group, checkedId ->
            for((idx, tabId) in scoreTabIds.withIndex()) {
                if(tabId == checkedId) {
                    examAnalysis.scoreAnalysis?.report?.get(idx)?.let { scoreReport ->
                        setScoreBarChart(idx)
                    }
                }
            }
        }

        setScoreBarChart(0)

        scoreTabIds.firstOrNull()?.let {
            findViewById<RadioButton>(it).isChecked = true
        }
    }

    fun setScoreCuration(good: CurationTitle?, bad:CurationTitle?) {
        try {
            if (good != null) {
                val text = makeCurationText(good.template, good.values)
                binding.scoreAnalysisGuideTv.text = text
            } else {
                binding.scoreAnalysisGuideTv2.setPaddingTop(resources.getDimensionPixelSize(R.dimen.dp40))
                binding.scoreAnalysisGuideTv.visibility = View.GONE
            }

            if (bad != null) {
                val text = makeCurationText(bad.template, bad.values)
                binding.scoreAnalysisGuideTv2.text = text
            } else binding.scoreAnalysisGuideTv2.visibility = View.GONE
        } catch ( e:Exception) {
            Log.e("MockReportActivity", "error=${e.localizedMessage}")
        }
    }

    fun setProblemUI() {
        setProblemCuration(examAnalysis.problemAnalysis?.curation)
        filteredProblemList.clear()
        filteredProblemList.addAll(examAnalysis.problemAnalysis?.problemList?: listOf())
        adapter = MockReportProblemAdapter(filteredProblemList, binding.problemRv)
        adapter.notifyDataSetChanged()
    }

    fun setProblemCuration(curation: CurationTitle?) {
        try {
            if (curation != null) {
                val text = makeCurationText(curation.template, curation.values)
                binding.problemAnalysisGuideTv.text = text
            } else binding.scoreAnalysisGuideTv.visibility = View.GONE

        } catch ( e:Exception) {
            Log.e("MockReportActivity", "error=${e.localizedMessage}")
        }
    }


    fun setTabs(tabTitles:List<String>, container: LinearLayout, idList:MutableList<Int>) {
        for((idx,title) in tabTitles.withIndex()) {
            val viewId = ViewUtils.generateViewId()
            idList.add(idx, viewId)
            addTab(title, container, viewId)
        }
    }

    fun addTab(tabTitle: String, container: LinearLayout, id:Int) {
        val tab = LayoutInflater.from(this).inflate(R.layout.item_mock_report_subject_tab_bg, container, false) as RadioButton
        tab.id = id
        tab.text = tabTitle
        container.addView(tab)
    }

    fun setRingChart(idx:Int) {
        examAnalysis.subjectAnalysis?.report?.get(idx)?.let { report ->
            val correct = report.totalCorrectRate
            val wrong = 100 - report.totalCorrectRate
            binding.ringChart.visibility = View.VISIBLE
            binding.ringChart.setData(wrong.toFloat(), correct.toFloat())
            binding.tvCorrectRate.text = "${correct}%"
        }
    }

    fun setSubjectBarChart(idx:Int) {
        binding.subjectBarCharContainer.removeAllViews()
        examAnalysis.subjectAnalysis?.report?.get(idx)?.let { subjectReport ->
            for((idx, chapter) in subjectReport.chapterList.withIndex()) {
                val bar = MockReportBarChartView(this, binding.subjectBarCharContainer, examAnalysis.showAllSummary)

                val leftTitle = chapter.chapterName
                val leftSub = "${chapter.totalNumber}문항"
                val rightTitle = "내 정답률 ${chapter.myCorrectRate}%"
                val rightSub = "${chapter.myRating}등급 평균 ${chapter.sameRatingCorrectRate}%"

                bar.setTitles(leftTitle, leftSub, rightTitle, rightSub)
                bar.setMainColorWithPercent(chapter.myCorrectRate)
                bar.setSubPercent(chapter.sameRatingCorrectRate)

                if(idx > 0) bar.binding.root.setPaddingTop(resources.getDimensionPixelSize(R.dimen.dp32))
                bar.show()
            }
        }
        binding.subjectBarCharContainer.postInvalidate()
    }

    fun setScoreBarChart(idx:Int) {
        binding.scoreBarCharContainer.removeAllViews()
        examAnalysis.scoreAnalysis?.report?.get(idx)?.let { scoreReport ->
            for((idx, chapter) in scoreReport.pointProblemList.withIndex()) {
                val bar = MockReportBarChartView(this, binding.scoreBarCharContainer, examAnalysis.showAllSummary)

                val leftTitle = "${chapter.point}점 문항"
                val leftSub = "${chapter.totalNumber}문항"
                val rightTitle = "내 정답률 ${chapter.myCorrectRate}%"
                val rightSub = "${chapter.myRating}등급 평균 ${chapter.sameRatingCorrectRate}%"

                bar.setTitles(leftTitle, leftSub, rightTitle, rightSub)
                bar.setMainColorWithPercent(chapter.myCorrectRate)
                bar.setSubPercent(chapter.sameRatingCorrectRate)

                if(idx > 0) bar.binding.root.setPaddingTop(32.toPx())
                bar.show()
            }
        }
        binding.scoreBarCharContainer.postInvalidate()
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
        val itemName = when(view) {
            binding.subjectFilter -> "필터-과목"
            binding.scoreFilter -> "필터-배점"
            binding.killerFilter -> "필터-킬러"
            else -> "필터-채점결과"
        }

        val itemValue = view.items[position]
        LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "모의고사보고서", itemName, itemValue)

        var filtered = testFilter(examAnalysis.problemAnalysis?.problemList ?: listOf(), binding.subjectFilter)

        filtered = testFilter(ArrayList(filtered), binding.scoreFilter)
        filtered = testFilter(ArrayList(filtered), binding.killerFilter)
        filtered = testFilter(ArrayList(filtered), binding.scoreResultFilter)

        filteredProblemList.clear()
        filteredProblemList.addAll(filtered)

        adapter.notifyDataSetChanged()
    }

    private fun testFilter(exams: List<MockExamProblem>, filter: ArduousSpinner): List<MockExamProblem> {
        val position = filter.position
        if (position != null && position > 0) {
            if (filter === binding.subjectFilter) {
                return exams.filter {
                    it.subject == ArrayList(subjectTreeSet).get(position - 1)
                }
            } else if (filter === binding.scoreFilter) {
                return exams.filter {
                    it.point == ArrayList(scoreTreeSet).get(position - 1)
                }
            } else if (filter === binding.killerFilter) {
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

class MockReportProblemAdapter(val problems: List<MockExamProblem>, val parent:LinearLayout) {
    fun notifyDataSetChanged() {
        parent.removeAllViews()
        for((idx, problem) in problems.withIndex()) {
            val view = getView(problem, idx)
            parent.addView(view)
        }
    }

    private fun getView(problem:MockExamProblem, idx:Int): View {
        val itemBinding: ItemMockReportProblemListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mock_report_problem_list, parent, false)
        set(itemBinding, problem)
        itemBinding.root.setBackgroundColor(if(idx%2 == 0) ContextCompat.getColor(parent.context, R.color.white_fafafa) else ContextCompat.getColor(parent.context, R.color.white_ffffff))
        return itemBinding.root
    }

    fun set(itemBinding: ItemMockReportProblemListBinding, problem: MockExamProblem) {
        itemBinding.tvNum.text = "${problem.problemNum}"
        itemBinding.tvSubject.text = problem.subject
        itemBinding.tvBigUnit.text = problem.bigChapter
        itemBinding.tvType.text = problem.unit
        itemBinding.tvPoint.text = "${problem.point}"

        itemBinding.ivKiller.visibility = if(problem.isKiller) View.VISIBLE else View.INVISIBLE

        if(problem.isCorrect){
            itemBinding.ivResult.setImageResource(R.drawable.ic_mock_report_result_o)
        } else {
            itemBinding.ivResult.setImageResource(R.drawable.ic_mock_report_result_x)
        }
    }
}



