package com.freewheelin.pulley.activities.analysis.tabFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.analysis.AnalysisTabActivity
import com.freewheelin.pulley.activities.analysis.AnalysisTabDelegate
import com.freewheelin.pulley.activities.analysis.AnanlysisTabActivityInterface
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.curation.MyCuration
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import kotlinx.android.synthetic.main.activity_analysis_tab.*
import kotlinx.android.synthetic.main.fragment_analysis_by_level.*
import org.joda.time.LocalDate

class AnalysisByLevelFragment : Fragment(), DabakTabRadioListener, AnalysisTabDelegate {

    override val tabTitle = "난이도별 분석"
    override var scrollPosition: Int = 0
    override var from: LocalDate = LocalDate.now().minusDays(6)
    override var to: LocalDate = LocalDate.now()

    override val analysis: Analysis?
        get() = (activity as AnanlysisTabActivityInterface).analysis

    val levelAnalysis: List<ChapterAnalysis>
        get() {
            return analysis?.levelAnalysis ?: listOf()
        }

    val levelRatioAnalysis: List<LevelRatioAnalysis>
        get() {
            return analysis?.levelRatioAnalysis ?: listOf()
        }

    val curation: MyCuration
        get() = MyCuration(requireContext())

    override fun getFragment(): Fragment {
        return this
    }

    companion object {
        @JvmStatic
        fun newInstance() = AnalysisByLevelFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analysis_by_level, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        summaryDtr.listener = this
        studyRatioByLevelDtr.listener = this
        summaryLevelQuestionBtn.extensionTouchArea(8.toPx())
        summaryLevelQuestionBtn.setOnClickListener {
            it.showBalloon("배점과 정답률을 고려하여 측정한\n문항의 수준 정보입니다.")
        }
        questionBtn.extensionTouchArea(8.toPx())
        questionBtn.setOnClickListener {
            it.showBalloon("배점과 정답률을 고려하여 측정한\n문항의 수준 정보입니다.")
        }
        subjectChart.setEmptyGuideText("조금 더 학습을 진행하시면,\n각 난이도별로 '나의 정답률'과 '같은 등급 정답률'을 비교해 볼 수 있습니다.")

        myPb.font = Theme.extraBold(requireContext())
        configureUI()
        activity?.scrollView?.scrollTo(0, scrollPosition)
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        when(radio) {
            summaryDtr -> {
                configureSummaryUI(index)
            }
            studyRatioByLevelDtr -> {
                configurePracRatioUI(index)
            }
        }

    }

    override fun onPeriodSelected(from: LocalDate, to: LocalDate, period: Int) {
        super.onPeriodSelected(from, to, period)
        configureUI()
    }

    private fun configureUI() {
        configureSummaryUI(0)
        configurePracRatioUI(0)
    }

    private fun configureSummaryUI(index: Int) {
        summaryDtr.labels = levelAnalysis.map { it.name }
        summaryDtr.selectedIndex = index

        val selectedAnalysis = levelAnalysis.getOrNull(index)
        val data = analysis?.getLevelSummaryData(index) ?: listOf()

        subjectChart.setData(data, true)
        subjectChart.setDetailBtnVisibility(View.GONE)

        val myRating = analysis?.myRating
        if (myRating == 1)
            subjectChart.setSelectedBarLabel("1등급\n평균", "나의\n정답률", null)
        else if (myRating != null)
            subjectChart.setSelectedBarLabel("${myRating}등급\n평균", "나의\n정답률", "${myRating - 1}등급\n평균")

        subjectChart.selectedBar = subjectChart.bars?.first()
        subjectChart.bars?.first()?.isSelectedDetailBtn = true
        subjectChart.requestLayout()


        summaryGuideTv.text = curation.getLevelSummaryQ(selectedAnalysis?.chapters)

        if(levelAnalysis.isEmpty()) {
            summaryDtr.visibility = View.GONE
            summaryLevelQuestionLabel.visibility = View.GONE
            summaryLevelQuestionBtn.visibility = View.GONE
        } else {
            summaryDtr.visibility = View.VISIBLE
            summaryLevelQuestionLabel.visibility = View.VISIBLE
            summaryLevelQuestionBtn.visibility = View.VISIBLE
        }
    }

    private fun configurePracRatioUI(index: Int) {
        val myRating = analysis?.myRating
        val selectedRatioAnalysis = levelRatioAnalysis.getOrNull(index)
        val legends = listOf(
                Pair(highestLegend, highestLegendTv),
                Pair(highLegend, highLegendTv),
                Pair(middleLegend, middleLegendTv),
                Pair(middleLowLegend, middleLowLegendTv),
                Pair(lowLegend, lowLegendTv)
        )

        studyRatioGuideTv.text = curation.getLevelStudyRatioQ(selectedRatioAnalysis)
        if(selectedRatioAnalysis == null || myRating == null) {
            studyRatioByLevelDtr.visibility = View.GONE

            levelQuestionLabel.visibility = View.INVISIBLE
            questionBtn.visibility = View.INVISIBLE

            sameRatingStudyRatioTv.visibility = View.INVISIBLE
            myStudyRatioTv.visibility = View.INVISIBLE
            upperRatingStudyRatioTv.visibility = View.INVISIBLE

            sameRatingPb.visibility = View.INVISIBLE
            upperPb.visibility = View.INVISIBLE
            myPb.visibility = View.INVISIBLE
            levelEmptyGuideTv.visibility = View.VISIBLE
            legends.forEach {
                it.first.visibility = View.INVISIBLE
                it.second.visibility = View.INVISIBLE
            }
        } else {
            studyRatioByLevelDtr.visibility = View.VISIBLE

            levelQuestionLabel.visibility = View.VISIBLE
            questionBtn.visibility = View.VISIBLE

            sameRatingPb.visibility = View.VISIBLE
            upperPb.visibility = View.VISIBLE
            myPb.visibility = View.VISIBLE
            levelEmptyGuideTv.visibility = View.GONE
            legends.forEach {
                it.first.visibility = View.VISIBLE
                it.second.visibility = View.VISIBLE
            }

            studyRatioByLevelDtr.labels = levelRatioAnalysis.map { it.chapterName }
            studyRatioByLevelDtr.selectedIndex = index

            sameRatingPb.setValues(
                    selectedRatioAnalysis.problemLevel_SameGrade_1,
                    selectedRatioAnalysis.problemLevel_SameGrade_2,
                    selectedRatioAnalysis.problemLevel_SameGrade_3,
                    selectedRatioAnalysis.problemLevel_SameGrade_4,
                    selectedRatioAnalysis.problemLevel_SameGrade_5, true
            )

            upperPb.setValues(
                    selectedRatioAnalysis.problemLevel_HigherGrade_1,
                    selectedRatioAnalysis.problemLevel_HigherGrade_2,
                    selectedRatioAnalysis.problemLevel_HigherGrade_3,
                    selectedRatioAnalysis.problemLevel_HigherGrade_4,
                    selectedRatioAnalysis.problemLevel_HigherGrade_5, true
            )

            myPb.setValues(
                    selectedRatioAnalysis.problemLevel_1,
                    selectedRatioAnalysis.problemLevel_2,
                    selectedRatioAnalysis.problemLevel_3,
                    selectedRatioAnalysis.problemLevel_4,
                    selectedRatioAnalysis.problemLevel_5, true
            )

            sameRatingStudyRatioTv.text = "${myRating}등급 평균"
            upperRatingStudyRatioTv.text = "${myRating!! - 1}등급 평균"

            if(myRating == 1) {
                sameRatingStudyRatioTv.visibility = View.VISIBLE
                myStudyRatioTv.visibility = View.VISIBLE
                upperRatingStudyRatioTv.visibility = View.INVISIBLE

                sameRatingPb.visibility = View.VISIBLE
                upperPb.visibility = View.INVISIBLE
                myPb.visibility = View.VISIBLE
            } else {
                sameRatingStudyRatioTv.visibility = View.VISIBLE
                myStudyRatioTv.visibility = View.VISIBLE
                upperRatingStudyRatioTv.visibility = View.VISIBLE

                sameRatingPb.visibility = View.VISIBLE
                upperPb.visibility = View.VISIBLE
                myPb.visibility = View.VISIBLE
            }
        }
    }

}
