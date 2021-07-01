package com.freewheelin.pulley.model

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.NumberUtils
import com.freewheelin.pulley.utils.partialFont
import com.freewheelin.pulley.utils.partialFontAndColored
import com.freewheelin.pulley.views.charts.OneBarChart
import com.freewheelin.pulley.views.charts.VerticalBarView
import java.text.DecimalFormat
import org.joda.time.LocalDate
import java.lang.Math.abs

class Analysis {

    var myScore: Int? = null
    var myPercent: Int = 0
    var improvement: Int? = null
    var myRating: Int? = null
    var problemTotalCount: Int? = 0

    var summaryAnalysis: List<ChapterAnalysis> = listOf()
    var chapterAnalysis: List<ChapterAnalysis> = listOf()
    var levelAnalysis: List<ChapterAnalysis> = listOf()
    var levelRatioAnalysis: List<LevelRatioAnalysis> = listOf()
    var numberAnalysis: NumberAnalysis? = null

    fun getUnitSummaryData(): List<VerticalBarView.BarData> {
        val subjectData = summaryAnalysis.map {
            val data = if(myRating == 1) {
                Pair(it.belowRate, it.myRate)
            } else {
                Triple(it.belowRate, it.myRate, it.upperRate)
            }

            VerticalBarView.BarData(it.name, data)
        }.let {
            if(it.size > 6)
                it.subList(0, 6)
            else
                it
        }

        return subjectData
    }

    fun getUnitAchieveData(isAscending: Boolean): List<Pair<String, ChapterAnalysis>> {
        val data = if(isAscending) {
            summaryAnalysis.flatMap { it.chapters.map { chapter -> Pair(it.name, chapter) } }.sortedBy {
                it.second.myCorrectRate
            }
        } else {
            summaryAnalysis.flatMap { it.chapters.map { chapter -> Pair(it.name, chapter) } }.sortedByDescending {
                it.second.myCorrectRate
            }
        }
        return data
    }

    fun getLevelSummaryData(index: Int): List<VerticalBarView.BarData>? {
        val selectedAnalysis = levelAnalysis.getOrNull(index)
        val data = selectedAnalysis?.let {
            it.chapters.map {
                val data = if(myRating == 1) Pair(it.belowRate, it.myRate)
                else
                    Triple(it.belowRate, it.myRate, it.upperRate)

                VerticalBarView.BarData(it.name, data)
            }
        }

        return data
    }
}


class NumberAnalysis {
    var problemTotalCount: Int = 0
    var problemTotalCountPast: Int = 0
    var subjectAnalysis: List<ChapterAmountAnalysis> = listOf()
    var chatperBigAnalysis: List<ChapterAmountAnalysis> = listOf()
    var problemCountByEachDailyAnalysis: List<ChapterAmountAnalysis> = listOf()
    var categoryAnalysis: CategoryAnalysis = CategoryAnalysis()

    val changeAmount: Int
        get() = problemTotalCount - problemTotalCountPast

    fun getArrangedSubjectAnalysis(): List<ChapterAmountAnalysis> {
        if(subjectAnalysis.size <= 6)
            return subjectAnalysis
        else {
            val arrangedList = ArrayList((0 until 5).map { subjectAnalysis[it] })

            val etcAnalysis = ChapterAmountAnalysis()
            etcAnalysis.chapterName = "기타"
            var totalCnt = 0
            (5 until subjectAnalysis.size).forEach {
                totalCnt += subjectAnalysis[it].problemTotalNumber
            }
            etcAnalysis.problemTotalNumber = totalCnt
            arrangedList.add(etcAnalysis)

            return arrangedList
        }

    }
}

class CategoryAnalysis {
    var moProblemCount: Int = 0
    var testProblemCount: Int = 0
    var bookProblemCount: Int = 0
    var weakProblemCount: Int = 0
}

class ChapterAmountAnalysis {
    var chapterCode: Int = 0
    var chapterName: String = ""
    var problemTotalNumber: Int = 0

}

class LevelRatioAnalysis {
    val chapterCode: Int = 0
    val chapterName: String = ""
    val sameGrade: Int = 0


    val problemLevel_1: Int = 0
    val problemLevel_2: Int = 0
    val problemLevel_3: Int = 0
    val problemLevel_4: Int = 0
    val problemLevel_5: Int = 0
    val problemLevel_SameGrade_1: Int = 0
    val problemLevel_SameGrade_2: Int = 0
    val problemLevel_SameGrade_3: Int = 0
    val problemLevel_SameGrade_4: Int = 0
    val problemLevel_SameGrade_5: Int = 0
    val problemLevel_HigherGrade_1: Int = 0
    val problemLevel_HigherGrade_2: Int = 0
    val problemLevel_HigherGrade_3: Int = 0
    val problemLevel_HigherGrade_4: Int = 0
    val problemLevel_HigherGrade_5: Int = 0


    fun getMaxValOnComparing(): Pair<String, Boolean> {

        val sameCompareValues = listOf(
                problemLevel_1 - problemLevel_SameGrade_1,
                problemLevel_2 - problemLevel_SameGrade_2,
                problemLevel_3 - problemLevel_SameGrade_3,
                problemLevel_4 - problemLevel_SameGrade_4,
                problemLevel_5 - problemLevel_SameGrade_5
        )

        val sameMaxCompareValue = sameCompareValues.maxBy { abs(it) }!!
        var sameIndex = 0
        for (value in sameCompareValues) {
            if(value == sameMaxCompareValue || value == sameMaxCompareValue * -1)
                break
            sameIndex++
        }


        if(sameCompareValues[sameIndex] > 0)
            return Pair(getLevelStrBy(sameIndex), true)
        else
            return Pair(getLevelStrBy(sameIndex), false)
    }


    fun getLevelStrBy(value: Int): String {

        return when(value) {
            0 -> "하"
            1 -> "중하"
            2 -> "중"
            3 -> "상"
            else -> "최상"
        }
    }
}


