package com.freewheelin.pulley.model.curation

import android.content.Context
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.model.ChapterAmountAnalysis
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.LevelRatioAnalysis
import com.freewheelin.pulley.utils.partialFont
import com.freewheelin.pulley.utils.partialFontAndColored
import java.lang.Math.abs

class MyCuration(val context: Context) {
    val dataNotExistText = "분석을 위한 학습 내역이 부족해요."
    val unitSummaryNoImprovementHigh = "%s 과목의 성취도가 가장 높아요."
    val unitSummaryNoImprovementLow = "%s 과목의 성취도가 가장 낮아요."
    private val unitSummaryIncreaseQ = "지난 %d일에 비해서 성적이 %s 향상되었어요!"
    private val unitSummaryNoChangeQ = "지난 %d일에 비해서 성적이 유지되었어요."
    private val unitSummaryDecreaseQ = "지난 %d일에 비해서 성적이 %s 떨어졌어요."

    val unitChpaterQ = "%s 단원의 보완이 필요해요."

    private val unitHighestAcheiveQ = "%s 단원의 성취도가 가장 높아요."
    private val unitLowestAcheiveQ = "%s 단원의 성취도가 가장 낮아요."

    val levelSummaryHighestQ = "%s 난이도 정답률이 같은 등급 친구들 대비 가장 높아요."
    val levelSummaryLowestQ = "%s 난이도 정답률이 같은 등급 친구들 대비 가장 낮아요."

    val studyAmountSummaryQ = "%s 과목을 집중해서 공부했네요."
    val studyAmountCompareByUnitQ = "%s 학습량이 많았어요."

    val level_ratio_A = "같은 등급 친구들 대비 %s 난이도의 연습량이 가장 부족해요!"
    val level_ratio_B = "같은 등급 친구들 대비 %s 난이도의 연습량이 가장 많아요!"

    val positiveColor: Int
        get() = ContextCompat.getColor(context, R.color.blue_30a4ff)

    val negativeColor: Int
        get() = ContextCompat.getColor(context, R.color.red_fe7b67)

    fun getUnitSummaryQ(period: Int, improvement: Int?, summaryAnalysis: List<ChapterAnalysis>): CharSequence {
        if(improvement == null) {
            val guideAnalysis = summaryAnalysis.maxByOrNull { abs(it.averageCorrectRateSameGrade - it.myCorrectRate) }
            if(guideAnalysis == null)
                return dataNotExistText
            else {
                val curation = (if(guideAnalysis.myCorrectRate > guideAnalysis.averageCorrectRateSameGrade)
                    unitSummaryNoImprovementHigh
                else
                    unitSummaryNoImprovementLow
                )
                    .format(guideAnalysis.name)
                    .partialFont(Theme.extraBold(context), 0, guideAnalysis.name.length)

                val color = if(guideAnalysis.myCorrectRate > guideAnalysis.averageCorrectRateSameGrade) positiveColor else negativeColor

                return curation.partialFontAndColored(
                        Theme.extraBold(context),
                        color,
                        curation.length - 4,
                        curation.length
                )
            }

        }

        return if(improvement == 0)
            unitSummaryNoChangeQ.format(period).partialFont(Theme.extraBold(context), 0, 5)
        else {
            val curation = if(improvement > 0) unitSummaryIncreaseQ else unitSummaryDecreaseQ
            val color = if(improvement > 0) positiveColor else negativeColor

            return curation.format(period,"${abs(improvement)}%")
                    .partialFont(Theme.extraBold(context), 0, 4 + period.toString().length)
                    .partialFontAndColored(Theme.extraBold(context),
                            color,
                            14 + period.toString().length,
                            14 + period.toString().length + "${abs(improvement)}%".length
                    )
        }
    }

    fun getUnitAchieveQ(chapter: ChapterAnalysis?, isAscending: Boolean): CharSequence {
        if(chapter == null)
            return dataNotExistText


        val quration = if(isAscending) unitLowestAcheiveQ else unitHighestAcheiveQ
        val unitName = chapter.name

        return quration.format(unitName).partialFont(Theme.extraBold(context), 0, unitName.length)
    }

    fun getUnitChapterQ(chapterAnalysis: List<ChapterAnalysis>?): CharSequence {
        val minChapter = chapterAnalysis?.minByOrNull { it.myRate } ?: return dataNotExistText

        return unitChpaterQ.format(minChapter.name).partialFont(Theme.extraBold(context), 0, minChapter.name.length)
    }

    fun getLevelSummaryQ(chapters: List<ChapterAnalysis>?): CharSequence {

        val guideAnalysis = chapters?.maxByOrNull {
            abs(it.averageCorrectRateSameGrade - it.myCorrectRate)
        }

        if(guideAnalysis == null)
            return dataNotExistText
        else {
            val levelText = guideAnalysis.name
            val isHigh = guideAnalysis.myCorrectRate > guideAnalysis.averageCorrectRateSameGrade

            val quration = if(isHigh) levelSummaryHighestQ else levelSummaryLowestQ
            return quration.format(levelText).partialFont(Theme.extraBold(context), 0, levelText.length + 4)
        }
    }

    fun getLevelStudyRatioQ(ratioAnalysis: LevelRatioAnalysis?): CharSequence {
        return if(ratioAnalysis == null)
            dataNotExistText
        else {
            val (levelStr, isHigh) = ratioAnalysis.getMaxValOnComparing()
            if(isHigh)
                level_ratio_B.format(levelStr).partialFont(Theme.extraBold(context), "$levelStr 난이도")
            else
                level_ratio_A.format(levelStr).partialFont(Theme.extraBold(context), "$levelStr 난이도")

        }
    }


    fun getStudyAmountSummaryQ(amountAnalysis: List<ChapterAmountAnalysis>?): CharSequence {
        if(amountAnalysis == null || amountAnalysis.isEmpty())
            return dataNotExistText
        else {
            val value = amountAnalysis.maxByOrNull { it.problemTotalNumber }?.problemTotalNumber
            val list = amountAnalysis.filter { it.problemTotalNumber == value }
            val text = list.joinToString { it.chapterName }
            return studyAmountSummaryQ.format(text).partialFont(Theme.extraBold(context), text)
        }
    }

    fun getStudyAmountCompareByUnitQ(amountAnalysis: List<ChapterAmountAnalysis>?): CharSequence {
        if(amountAnalysis == null || amountAnalysis.isEmpty())
            return dataNotExistText
        else {
            val value = amountAnalysis.maxByOrNull { it.problemTotalNumber }?.problemTotalNumber
            val list = amountAnalysis.filter { it.problemTotalNumber == value }
            val text = list.joinToString { it.chapterName }
            return studyAmountCompareByUnitQ.format(text).partialFont(Theme.extraBold(context), text)
        }
    }
}
