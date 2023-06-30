package com.freewheelin.pulley.legacy.activities.analysis.tabFragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.legacy.activities.analysis.AnalysisTabDelegate
import com.freewheelin.pulley.legacy.activities.analysis.AnanlysisTabActivityInterface
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.usertest.analysis.UserAnalysisAllActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.databinding.FragmentAnalysisStudyAmountBinding
import com.freewheelin.pulley.legacy.model.Analysis
import com.freewheelin.pulley.legacy.model.NumberAnalysis
import com.freewheelin.pulley.legacy.model.curation.MyCuration
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.textViews.UpDownTextView
import com.freewheelin.pulley.legacy.views.charts.OneBarChart
import org.joda.time.LocalDate


class AnalysisStudyAmountFragment : Fragment(), AnalysisTabDelegate {

    override val tabTitle = "학습량 분석"
    override var scrollPosition: Int = 0
    override var from: LocalDate = LocalDate.now().minusDays(6)
    override var to: LocalDate = LocalDate.now()

    override val analysis: com.freewheelin.pulley.legacy.model.Analysis?
        get() = (activity as AnanlysisTabActivityInterface).analysis

    val numberAnalysis: com.freewheelin.pulley.legacy.model.NumberAnalysis?
        get() {
            return analysis?.numberAnalysis
        }

    val notExistDataText: String
        get() {
            return (activity as AnanlysisTabActivityInterface).notExistDataText
        }

    val curation: MyCuration
        get() = MyCuration(requireContext())

    override fun getFragment(): Fragment {
        return this
    }

    companion object {
        @JvmStatic
        fun newInstance() = AnalysisStudyAmountFragment()
    }
    lateinit var binding: FragmentAnalysisStudyAmountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis_study_amount, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        configUI(from, to)

        (activity as? AnalysisTabActivity)?.binding?.scrollView?.scrollTo(0, scrollPosition)
        (activity as? UserAnalysisAllActivity)?.binding?.scrollView?.scrollTo(0, scrollPosition)

        with(binding) {
            unitChart.post {
                val itemCnt = unitChart.adapter?.itemCount
                if(itemCnt != null)
                    (unitChart.layoutManager as? LinearLayoutManager)?.scrollToPosition(itemCnt - 1)
            }
            dailyChart.post {
                val itemCnt = dailyChart.adapter?.itemCount
                if(itemCnt != null)
                    (dailyChart.layoutManager as? LinearLayoutManager)?.scrollToPosition(itemCnt - 1)
            }
        }
    }

    private fun initUI() {
        with(binding) {
            (0 until 6).forEach { getLegendViewComp(it).first.background.setTint(ringChart.legendColors[it]) }
            unitChart.isHighlightMaxAndMin = true
            dailyChart.isHighlightMaxAndMin = true
            mockCntContainer.visibleIf(schoolType.isHigh)
        }
    }

    private fun configUI(from: LocalDate, to: LocalDate) {
        configSummaryUI(from, to)
        configUnitAmountUI()
        configDailyAmountUI(from, to)
    }

    private fun configSummaryUI(from: LocalDate, to: LocalDate) {
        with(binding) {
            val period = DateTimeUtils.getPeriod(from, to)
            periodLabel.text = "지난 ${period}일 학습량"
            lastPeriodLabel.text = "지난 ${period}일 대비"

            if(numberAnalysis == null) {
                ringChart.visibility = View.INVISIBLE
                legendContainerLl.visibility = View.INVISIBLE
                summaryEmptyGuideTv.visibility = View.VISIBLE
                subjectGuideTv.text = notExistDataText
                testCntTv.text = "0개"
                unitCntTv.text = "0개"
                mockCntTv.text = "0개"
                wrongCntTv.text = "0개"

                periodTotalCntTv.text = "0문제"
                lastPeriodCompCntUtv.change = UpDownTextView.Change.noChange
                lastPeriodCompCntUtv.text = "0문제"
                return
            }

            periodTotalCntTv.text = "${numberAnalysis!!.problemTotalCount}문제"
            lastPeriodCompCntUtv.change = if(numberAnalysis!!.changeAmount >= 0) UpDownTextView.Change.increase else UpDownTextView.Change.decrease
            if(numberAnalysis!!.changeAmount < 0)
                lastPeriodCompCntUtv.text = "${numberAnalysis!!.changeAmount * -1}문제"
            else
                lastPeriodCompCntUtv.text = "${numberAnalysis!!.changeAmount}문제"

            val categoryAnalysis = numberAnalysis?.categoryAnalysis
            if(categoryAnalysis != null) {
                testCntTv.text = "${categoryAnalysis.testProblemCount}개"
                unitCntTv.text = "${categoryAnalysis.bookProblemCount}개"
                mockCntTv.text = "${categoryAnalysis.moProblemCount}개"
                wrongCntTv.text = "${categoryAnalysis.weakProblemCount}개"
            } else {
                testCntTv.text = "0개"
                unitCntTv.text = "0개"
                mockCntTv.text = "0개"
                wrongCntTv.text = "0개"
            }

            (0 until 6).forEach { hideLegend(it) }
            val subjectAnalysis = numberAnalysis!!.getArrangedSubjectAnalysis()

            for(i in 0 until subjectAnalysis.size) {
                showLegend(i)
                val legend = getLegendViewComp(i)
                legend.second.text = subjectAnalysis[i].chapterName
                legend.third.text = "${subjectAnalysis[i].problemTotalNumber}개"
            }

            val guideAnalysis = subjectAnalysis.getOrNull(0)
            subjectGuideTv.text = curation.getStudyAmountSummaryQ(subjectAnalysis)

            if(guideAnalysis != null) {
                legendContainerLl.visibility = View.VISIBLE
                ringChart.setData(subjectAnalysis.map { it.problemTotalNumber })
                ringChart.visibility = View.VISIBLE
                summaryEmptyGuideTv.visibility = View.INVISIBLE
            } else {
                legendContainerLl.visibility = View.INVISIBLE
                ringChart.visibility = View.INVISIBLE
                summaryEmptyGuideTv.visibility = View.VISIBLE
            }
        }
    }

    private fun configUnitAmountUI() {
        with(binding) {
            if (numberAnalysis == null) {
                unitGuideTv.text = notExistDataText
                unitChart.visibility = View.INVISIBLE
                bigUnitEmptyGuideTv.visibility = View.VISIBLE
                return
            }
            val chapterBigAnalysis = numberAnalysis!!.chatperBigAnalysis

            unitGuideTv.text = curation.getStudyAmountCompareByUnitQ(chapterBigAnalysis)
            val data = chapterBigAnalysis.map { OneBarChart.BarData(it.chapterName, it.problemTotalNumber) }

            if(data.isNotEmpty()) {
                bigUnitEmptyGuideTv.visibility = View.GONE
                unitChart.visibility = View.VISIBLE
                unitChart.setData(data)
            } else {
                bigUnitEmptyGuideTv.visibility = View.VISIBLE
                unitChart.visibility = View.INVISIBLE
            }
        }
    }

    private fun configDailyAmountUI(from: LocalDate, to: LocalDate) {

        binding.dailyChart.setData(getDailyAnalysis(from, to))
    }

    private fun getDailyAnalysis(from: LocalDate, to: LocalDate): List<OneBarChart.BarData> {
        val period = DateTimeUtils.getPeriod(from, to)
        return (0 until period).map {
            val date = from.plusDays(it).toDate()
            val dateKey = DateTimeUtils.yyyyMMdd.format(date).toInt()
            val data = numberAnalysis?.problemCountByEachDailyAnalysis?.filter { it.chapterCode == dateKey }?.firstOrNull()
            OneBarChart.BarData(
                    DateTimeUtils.mMddFormat.format(date), data?.problemTotalNumber ?: 0
            )
        }
    }

    private fun hideLegend(position: Int) {
        val legend = getLegendViewComp(position)
        legend.first.visibility = View.INVISIBLE
        legend.second.visibility = View.INVISIBLE
        legend.third.visibility = View.INVISIBLE
    }

    private fun showLegend(position: Int) {
        val legend = getLegendViewComp(position)
        legend.first.visibility = View.VISIBLE
        legend.second.visibility = View.VISIBLE
        legend.third.visibility = View.VISIBLE
    }

    private fun getLegendViewComp(position: Int): Triple<View, TextView, TextView> {
        with(binding) {
            val legends = listOf(
                Triple(firstLegend, firstLegendTv, firstLegendCntTv),
                Triple(secondLegend, secondLegendTv, secondLegendCntTv),
                Triple(thirdLegend, thirdLegendTv, thirdLegendCntTv),
                Triple(fourthLegend, fourthLegendTv, fourthLegendCntTv),
                Triple(fifthLegend, fifthLegendTv, fifthLegendCntTv),
                Triple(sixthLegend, sixthLegendTv, sixthLegendCntTv)

            )
            return legends[position]
        }
    }

    override fun onPeriodSelected(from: LocalDate, to: LocalDate, period: Int) {
        super.onPeriodSelected(from, to, period)
        configUI(from, to)
    }
}

