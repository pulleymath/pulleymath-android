package com.freewheelin.pulley.model.curation

import android.content.Context
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.ScoreAnalysis
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.NumberUtils
import com.freewheelin.pulley.utils.partialFont
import com.freewheelin.pulley.utils.partialFontAndColored
import java.lang.Math.abs

class MockReportCuration(val context: Context) {
    val mockNotExistDataText: String = "해당 정보가 없습니다"

    val unitPerfect1 = "대박! 출제자인가요?! 100점이라니!! 완벽해요!!"
    val unitPerfect2 = "이 점수가 수능 점수가 될 거에요!"

    val unitImperfectLow = "%s 과목이 많이 어렵나요? 문제리뷰를 하면 더 잘할 수 있을거에요!"
    val unitImperfectHigh = "%s 과목을 잘하고 있어요! 난이도를 조금 더 높여도 좋겠어요!"

    val extraBold = Theme.extraBold(context)
    val positiveColor = ContextCompat.getColor(context, R.color.blue_30a4ff)
    val negativeColor = ContextCompat.getColor(context, R.color.red_fe7b67)
    val summary_A = "만점까지 %s 더 필요해요. 100점까지 거의 다 왔어요!"
    val summary_B = "대박!! 100점이에요!! 넘볼 수 없는 완벽함!!"
    val summary_C = "%d등급까지 %s 더 필요해요."
    val summary_D = "정답률 %s까지 %s가 더 필요해요."
    val summary_E = "대박!! 정답률 100%에요!! 넘볼 수 없는 완벽함!!"

    val subject_guide_A = "%s 과목은 모두 정답이에요!"
    val subject_guide_B = "%s에서 딱 1문제 틀렸어요!"
    val subject_guide_C = "%s에서 %s 만큼은 꼼꼼하게 리뷰해보아요!"
    val subject_guide_D = "%s에서 %s은 더 집중해서 리뷰해보아요!"

    val unit_A = "모든 단원을 골고루 잘하고 있네요! 1등급도 가능하겠어요!"
    val unit_B = "모든 단원을 같은 등급 친구들보다 잘하고 있네요!"
    val unit_C = "%s 단원이 강점이 될 수 있도록 복습해보기를 추천해요! :)"
    val unit_D = "%s 단원을 같은 등급 친구들보다 잘하고 있어요!"
    val unit_E = "%s 단원이 같은 등급 친구들 대비 가장 낮아요 ㅜㅜ"

    val score_AB = "같은 등급 친구들 보다 %s %s"
    val score_C = "모든 배점에서 잘하고 있어요! %s"
    val score_D = "유독 %s 문항이 어렵나요? %s"


    fun getSummaryQ(rating: Int, score: Int, targetScore: Int?): CharSequence {
        return if(rating == 1) {
            if(score == 100) {
                val extraBoldText = "100점이에요!!"
                summary_B.partialFont(Theme.extraBold(context), summary_B.indexOf(extraBoldText), summary_B.indexOf(extraBoldText) + extraBoldText.length)
            } else {
                val remainPointText = "${100 - score}점"
                val text = summary_A.format(remainPointText)
                text.partialFont(Theme.extraBold(context), 0, 5 + remainPointText.length)
            }
        } else {
            val remainPointText = "${targetScore!! - score}점"
            summary_C.format(rating -1, remainPointText).partialFont(Theme.extraBold(context), 0, 8)
        }
    }

    fun getSummaryP(percent: Int, suffix: String="거의 다 왔어요!"): CharSequence {

        return if(percent == 100) {
            val extraBoldText = "정답률 100%에요!!"
            summary_E.partialFont(Theme.extraBold(context), summary_E.indexOf(extraBoldText), summary_E.indexOf(extraBoldText) + extraBoldText.length)
        } else {
            val targetPointText = "100%"
            val remainPoint = 100 - percent
            val remainPointText = "${remainPoint}%"
            val text = summary_D.format(targetPointText, remainPointText)
            if(remainPoint in 80..99) {
                text.plus(" $suffix")
            }
            text.partialFont(Theme.extraBold(context), 4, 7 + targetPointText.length + remainPointText.length)
        }
    }

    fun getSubjectQ(subjectAnalysis: List<ChapterAnalysis>, score: Int): CharSequence {
        if(score == 100) {
            val perfectText = NumberUtils.rand(0,2).let {
                if(it == 0) unitPerfect1 else unitPerfect2
            }

            return perfectText
        }

        val guideSubject = subjectAnalysis.maxByOrNull {
            abs(it.averageCorrectRateSameGrade - it.myCorrectRate)
        }

        if(guideSubject == null) {
            LogUtils.assert(false, "모의고사 보고서 chapterAnalysis정보가 없음")
            return mockNotExistDataText
        } else {
            val isHigh = guideSubject.myCorrectRate > guideSubject.averageCorrectRateSameGrade

            val curation = if(isHigh) unitImperfectHigh else unitImperfectLow
            return curation.format(guideSubject.name).partialFont(Theme.extraBold(context), 0, guideSubject.name.length)
        }
    }

    fun getSubjectGuideQ(chapterName: String, problems: List<Problem>): CharSequence {
        val incorrectProblems = problems.filter { it.getResultByScoring() == Result.incorrect }.sortedBy { it.correctRate }

        if(incorrectProblems.isEmpty())
            return subject_guide_A.format(chapterName).partialFont(Theme.extraBold(context), 0, chapterName.length)
        else if(incorrectProblems.size == 1)
            return subject_guide_B.format(chapterName).partialFont(Theme.extraBold(context), 0, chapterName.length)
        else if(incorrectProblems.size <= 3) {
            val recommendText = "${incorrectProblems[0].problemNum}번"
            val text = subject_guide_C.format(chapterName, recommendText)
            return text
                    .partialFont(Theme.extraBold(context), 0, chapterName.length)
                    .partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
        }
        else {
            val list = listOf(incorrectProblems[0], incorrectProblems[1]).sortedBy { it.problemNum }
            val recommendText = "${list[0].problemNum}번, ${list[1].problemNum}번"
            val text =  subject_guide_D.format(chapterName, recommendText)
            return text
                    .partialFont(Theme.extraBold(context), 0, chapterName.length)
                    .partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
        }
    }

    fun getUnitGuideQ(rating: Int, chapters: List<ChapterAnalysis>): CharSequence {
        val higherChapters = chapters.filter { it.myRate > it.belowRate }
        val lowerChapters = chapters.filter { it.myRate <= it.belowRate }

        return if(higherChapters.size == chapters.size) {
            if(rating in 2..4) {
                unit_A.partialFont(Theme.extraBold(context), "1등급")
            } else {
                unit_B.partialFont(Theme.extraBold(context), 0, 5)
            }
        } else if(lowerChapters.size == chapters.size) {
            val highChapter = chapters.sortedByDescending { it.myRate }.first()
            unit_C.format(highChapter.name).partialFont(Theme.extraBold(context), 0, highChapter.name.length)
        } else {
            val targetChapter = chapters.sortedByDescending { abs(it.myRate - it.belowRate) }.first()

            if(targetChapter.myRate - targetChapter.belowRate > 0) {
                val text = unit_D.format(targetChapter.name)
                text.partialFont(Theme.extraBold(context), targetChapter.name)
                        .partialFontAndColored(Theme.extraBold(context), positiveColor, "잘하고 있어요!")
            } else {
                val text = unit_E.format(targetChapter.name)
                text.partialFont(Theme.extraBold(context), targetChapter.name)
                text.partialFont(Theme.extraBold(context), targetChapter.name)
                        .partialFontAndColored(Theme.extraBold(context), negativeColor, "가장 낮아요 ㅜㅜ")
            }
        }
    }

    fun getScoreGuideQ(rating: Int, scoreAnalysis: List<ScoreAnalysis>): CharSequence {
//        val highScores = scoreAnalysis.filter { it.myRate > it.sameRate}
//        val lowerScores = scoreAnalysis.filter { it.myRate <= it.sameRate }
//
//        return if(scoreAnalysis.size == highScores.size) {
//            if(rating == 1)
//                score_C.format("수능 만점도 가능하겠어요!").partialFont(Theme.extraBold(context), "모든 배점에서 잘하고 있어요!")
//            else
//                score_C.format("${rating - 1}등급 도전해도 좋겠어요!").partialFont(Theme.extraBold(context), "모든 배점에서 잘하고 있어요!")
//
//        } else if(scoreAnalysis.size == lowerScores.size) {
//            val targetChapter = scoreAnalysis.sortedBy { it.myRate - it.sameRate }.first()
//            val endText: String = if(targetChapter.title == "2점")
//                "실수는 없는지 꼭 오답정리해보아요!"
//            else if(targetChapter.title == "3점")
//                "반복연습하면 정답률 높일 수 있어요!"
//            else if(targetChapter.title == "4점")
//                "스스로 도전하는것이 제일 중요해요!"
//            else
//                "문제의도를 정확히 파악하는데 집중해보아요!"
//            score_D.format(targetChapter.title, endText)
//                    .partialFont(extraBold, targetChapter.title)
//                    .partialFontAndColored(extraBold, negativeColor, endText)
//
//        } else {
//            val targetChapter = scoreAnalysis.sortedByDescending { abs(it.myRate - it.sameRate) }.first()
//
//            val (endText, color) = if(targetChapter.myRate > targetChapter.sameRate) {
//                Pair("문제를 잘 풀고 있어요!", positiveColor)
//            } else {
//                Pair("문제가 약해요 ㅜㅜ", negativeColor)
//            }
//            score_AB.format(targetChapter.title, endText).partialFont(extraBold, targetChapter.title).partialFontAndColored(extraBold, color, endText)
//        }
        return ""
    }

}