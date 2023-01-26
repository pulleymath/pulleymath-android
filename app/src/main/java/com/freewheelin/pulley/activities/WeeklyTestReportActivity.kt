package com.freewheelin.pulley.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityTestReportWeeklyBinding
import com.freewheelin.pulley.databinding.ItemTestReportScoringBinding
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.model.curation.TestCuration
import com.freewheelin.pulley.utils.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class WeeklyTestReportActivity : AppCompatActivity() {
    private val binding: ActivityTestReportWeeklyBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_test_report_weekly,null,false)
    }
    lateinit var test: Test
    val curation: TestCuration
        get() = TestCuration(this)
    var isFromSolve = false

    companion object {
        fun getIntent(context: Context, test: Test, isFromSolve: Boolean = false): Intent {
            val intent = Intent(context, WeeklyTestReportActivity::class.java)
            intent.putExtra(TestManager.ARG_TEST, test)
            intent.putExtra("FROM_SOLVE", isFromSolve)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUI()

        isFromSolve = intent.getBooleanExtra("FROM_SOLVE", false)
        val test = intent.getSerializableExtra(TestManager.ARG_TEST) as Test
        with(binding) {
            TestManager.getTestReport(this@WeeklyTestReportActivity, user!!, test) {
                this@WeeklyTestReportActivity.test = it
                scoreTv.text = "${it.score}"
                scoringRv.adapter = ScoringAdapter()
                scoreGuideTv.text = curation.getWeeklyReportGuideQ(it.getLastTimeScore(), it.problems, user!!.fullName)
                setChartData()

                scoreTv.show()
                scoreLabel.show()
                scoreGuideTv.show()
                reviewBtn.show()
                historyChart.show()
                correctRateGuideTv.show()

                correctRateGuideTv.text = "* 정오 옆의 숫자는 ${it.studentRating}등급 평균 정답률입니다."
                if(it.weakChapterAnalysis == null)
                    lowestNothingGuideTv.show()
                else {
                    lowestSubjectTv.text = SubjectV3.codeToSubject(it.weakChapterAnalysis!!.code).filterText // Subject.init(it.weakChapterAnalysis!!.code).filterText
                    lowestUnitTv.text = it.weakChapterAnalysis?.name
                    lowestPenChart.setValues(it.weakChapterAnalysis!!.myRate, it.weakChapterAnalysis!!.belowRate, withAnim =  true, withRangeColor = true)
                    lowestPenChart.setLabels("내 정답률", "${it.studentRating}등급 평균")
                    lowestSubjectTv.show()
                    lowestUnitTv.show()
                    lowestPenChart.show()
                }

                if(it.strongChapterAnalysis == null)
                    highestNothingGuideTv.show()
                else {
                    lowestSubjectTv.text = SubjectV3.codeToSubject(it.weakChapterAnalysis!!.code).filterText // Subject.init(it.weakChapterAnalysis!!.code).filterText
                    highestUnitTv.text = it.strongChapterAnalysis?.name
                    highestPenChart.setValues(it.strongChapterAnalysis!!.myRate, it.strongChapterAnalysis!!.belowRate, withAnim =  true, withRangeColor = true)
                    highestPenChart.setLabels("내 정답률", "${it.studentRating}등급 평균")
                    highestSubjectTv.show()
                    highestUnitTv.show()
                    highestPenChart.show()
                }
            }
        }
    }

    private fun initUI() {
        with(binding) {
            scoringRv.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dp48) * 4
            scoringRv.layoutManager = GridLayoutManager(this@WeeklyTestReportActivity, 4, GridLayoutManager.HORIZONTAL, false).also {
                it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return 1
                    }
                }
            }

            lowestPenChart.setLabelTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp14))
            lowestPenChart.setValueTextSize(resources.getDimension(R.dimen.sp14))
            lowestPenChart.setLabelWidth(resources.getDimension(R.dimen.dp80).toInt())
            highestPenChart.setLabelTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp14))
            highestPenChart.setValueTextSize(resources.getDimension(R.dimen.sp14))
            highestPenChart.setLabelWidth(resources.getDimension(R.dimen.dp80).toInt())

            initChart(historyChart)
            hideViews()
            xBtn.extensionTouchArea(24.toPx())
            xBtn.setOnClickListener {
                onBackPressed()
            }
            reviewBtn.setOnClickListener {
                onReviewBtnClicked()
            }
        }
    }

    private fun initChart(chart: BarChart) {
        chart.isScaleXEnabled = false
        chart.isScaleYEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawAxisLine(false)
        chart.axisRight.setDrawGridLines(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.setDrawLabels(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.axisLineColor = Color.TRANSPARENT
        chart.xAxis.textSize = resources.getDimension(R.dimen.sp14).pxToSp()
        chart.xAxis.typeface = Theme.bold(this)
        chart.xAxis.textColor = ContextCompat.getColor(this, R.color.grey_9f9f9f)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setValueFormatter { value, axis ->
            val index = value.toInt()
            test.testHistory.reversed().getOrNull(index)?.formattedDate
        }
        chart.renderer = RoundChartRenderer(chart, chart.animator, chart.viewPortHandler, 5f.toPx())
        chart.axisLeft.axisMaximum = 130f
        chart.axisLeft.axisMinimum = 5f
        chart.xAxis.labelCount = 5
        chart.extraBottomOffset = resources.getDimension(R.dimen.dp32)
    }

    fun onReviewBtnClicked() {
        if(isFromSolve) {
            finish()
        } else {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "리뷰하기", test.getTestType().eventItemValue)
            val intent = SolveActivity.getReviewIntent(this, test)
            startActivity(intent)
        }
    }

    private fun setChartData() {
        val entry = arrayListOf<BarEntry>()
        for(x in 0 until test.testHistory.reversed().size) {
            val value = (test.testHistory.reversed()[x].score?.toFloat() ?: 0f) + 10f
            entry.add(BarEntry(x.toFloat(), value))
        }

        val barDataSet = BarDataSet(entry, "점수")
        barDataSet.colors = listOf(
                ContextCompat.getColor(this, R.color.purple_ECEBFF),
                ContextCompat.getColor(this, R.color.purple_ECEBFF),
                ContextCompat.getColor(this, R.color.purple_ECEBFF),
                ContextCompat.getColor(this, R.color.purple_ECEBFF),
                ContextCompat.getColor(this, R.color.purple_ACACFF)
        )

        binding.historyChart.data = BarData(barDataSet).apply {
            barWidth = 0.5f
            isHighlightEnabled = false
            setValueTextSize(resources.getDimension(R.dimen.sp14).pxToSp())
            setValueTextColor(ContextCompat.getColor(this@WeeklyTestReportActivity, R.color.purple_ACACFF))
            setValueTypeface(Theme.bold(this@WeeklyTestReportActivity))
            setValueFormatter{ value, entry, index, handler ->
                val value = test.testHistory.reversed()[entry.x.toInt()].score
                if(value == null)
                    "미응시"
                else
                    value.toInt().toString()
            }
        }
    }

    private fun hideViews() {
        with(binding) {
            scoreTv.visibility = View.INVISIBLE
            scoreLabel.visibility = View.INVISIBLE
            scoreGuideTv.visibility = View.INVISIBLE
            reviewBtn.visibility = View.INVISIBLE
            historyChart.visibility = View.INVISIBLE
            lowestSubjectTv.visibility = View.INVISIBLE
            lowestUnitTv.visibility = View.INVISIBLE
            lowestPenChart.visibility = View.INVISIBLE
            lowestNothingGuideTv.visibility = View.INVISIBLE

            highestSubjectTv.visibility = View.INVISIBLE
            highestUnitTv.visibility = View.INVISIBLE
            highestPenChart.visibility = View.INVISIBLE
            highestNothingGuideTv.visibility = View.INVISIBLE

            correctRateGuideTv.visibility = View.INVISIBLE
        }
    }

    private fun showViews() {
        with(binding) {
            scoreTv.show()
            scoreLabel.show()
            scoreGuideTv.show()
            reviewBtn.show()
            historyChart.show()
            lowestSubjectTv.show()
            lowestUnitTv.show()
            lowestPenChart.show()
            lowestNothingGuideTv.show()

            highestSubjectTv.show()
            highestUnitTv.show()
            highestPenChart.show()
            highestNothingGuideTv.show()

            correctRateGuideTv.show()
            scoringRv.show()
        }
    }

    inner class ScoringAdapter: RecyclerView.Adapter<WeeklyScoringHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyScoringHolder {
            val itemBinding: ItemTestReportScoringBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_test_report_scoring, parent, false)
            return WeeklyScoringHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return test.problems.size
        }

        override fun onBindViewHolder(holder: WeeklyScoringHolder, position: Int) {
            val problem = test.problems[position]
            holder.set(problem)
            if(position > 15)
                holder.endBorder.visibility = View.INVISIBLE
            else
                holder.endBorder.visibility = View.VISIBLE

            if(position % 4 == 3)
                holder.bottomBorder.visibility = View.INVISIBLE
            else
                holder.bottomBorder.visibility = View.VISIBLE
        }
    }
}


class WeeklyScoringHolder(val itemBinding: ItemTestReportScoringBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val endBorder = itemBinding.endBorder
    val bottomBorder= itemBinding.bottomBorder
    val numTv = itemBinding.numTv
    val resultIv = itemBinding.resultIv
    val correctRateTv = itemBinding.correctRateTv

    fun set(problem: Problem) {
        numTv.text = "${problem.problemNum}"
        if(problem.getResultByScoring() == Result.correct)
            resultIv.setImageResource(R.drawable.ic_result_correct)
        else
            resultIv.setImageResource(R.drawable.ic_result_incorrect)

        correctRateTv.text = "${problem.standardCorrectRate}%"

    }
}

