package com.freewheelin.pulley.legacy.model.curation

import android.content.Context
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.legacy.utils.partialFont
import java.util.*

class TestCuration(val context: Context) {

    val extraBold = Theme.extraBold(context)

    val list_daily_before_A = "데일리테스트는\n" +
            "오늘만 풀 수 있어요!\n" +
            "지금 바로 풀어볼까요?"
    val list_daily_befroe_B = "3일째 풀고있는 %s님의 꾸준함!!\n" +
            "작심삼일 성공했으니\n" +
            "오늘도 풀 수 있죠?!"
    val list_daily_before_C = "%s님의 성실함에 감탄중!!\n" +
            "오늘까지 풀이하면\n" +
            "이번주 데일리테스트 ALL Clear!"

    val list_daily_finish_A = "다음 데일리 테스트는\n내일 오전 6시에 공개됩니다 :)"
    val list_daily_finish_B = "이번주도 수고하셨어요!\n내일 오전 6시에는 주간테스트가 공개됩니다 :)"

    val report_daily_guide_A = "많이 어려웠나요?\n" +
            "알지만 틀릴 수 있는 문제인 %s 만큼은 꼭 다시 풀어보세요!\n" +
            "내일은 더 잘 할 수 있도록 난이도를 조금 조정해서 출제할게요 :)"
    val report_daily_guide_F = "우와! 연속 2회 만점!! 수학의 신인가요?!\n" +
            "스스로 긴가민가했던 문제를 다시보길 추천해요 :)"
    val report_daily_guide_G = "당황하지 말아요 :)\n" +
            "나에게 약한 점을 채울 수 있는 아주 좋은 기회이니까요!\n" +
            "우리 최소한 %s 만큼은 꼭 다시 풀어봅시다!"
    val report_daily_guide_H = "약점 채우기 딱 좋은 기회네요!\n" +
            "정답률이 높았던 %s 부터 꼭 다시 풀어보세요!\n" +
            "조금 더 고민하면 해결할 수 있는 문제가 있을거에요 :)"
    val report_daily_guide_I = "어려운 문제 때문에 힘들진 않았나요?\n" +
            "다른 친구들도 어려워한 %s은 꼭 다시 풀어보세요!\n" +
            "나의 약점이 어디인지 알아낼 수 있을거에요 :)"
    val report_daily_guide_J = "우와! 만점이라니!! 정말 멋져요 :D\n" +
            "다른 친구들도 어려워한 %s을 복습해볼까요?\n" +
            "내일도 100점 가능할거에요!"


    val list_weekly_before_A = "주간테스트는 20문제가 출제되며,\n" +
            "주말에만 풀 수 있어요!\n" +
            "지금 바로 시작해볼까요?"
    val list_weekly_before_B = "B. 주간테스트 3주(3회)/월 연속 응시내역이 있는 경우 \n" +
            "이번주까지 보면 연속 3주!\n" +
            "수능 1등급도 가능한 성실함!!\n" +
            "지금 바로 시작해봅시다 : )"
    val list_weekly_before_C = "앗! 중간점검기회를\n" +
            "놓치지 마세요!"

    val report_weekly_guide_A = "처음 보는 주간테스트라 당황했나요?\n" +
            "괜찮아요! 다음 주간테스트는 더 잘 볼 수 있을거에요 :)\n" +
            "여기서 끝내지 말고 %s은 꼭 다시 풀어봅시다!"
    val report_weekly_guide_C = "당황하지 말아요 :)\n" +
            "나에게 약한 점을 채울 수 있는 아주 좋은 기회이니까요!\n" +
            "우리 최소한 %s 만큼은 꼭 다시 풀어봅시다!"
    val report_weekly_guide_D = "헉! 무슨일이죠?\n" +
            "%s님의 약점이 아직 채워지지 않은걸까요?ㅜㅜ\n" +
            "다음 주엔 더 잘할 수 있도록 %s 만큼은 꼭 다시 풀어봅시다!"
    val report_weekly_guide_E = "헛! 많이 어려웠나요?\n" +
            "아깝게 틀린 %s 만큼은 꼭 다시 풀어보세요!\n" +
            "다음엔 꼭 맞힐 수 있을거에요 :)"
    val report_weekly_guide_F = "천천히 가는 거북이가 승리한다!\n" +
            "지난 주간테스트보다 점수가 올랐어요!\n" +
            "다음 주에도 상승할 수 있도록 %s은 꼭 다시 풀어보세요!"
    val report_weekly_guide_G = "헛! 많이 어려웠나요?\n" +
            "점수 상승을 만들어줄 %s 만큼은 꼭 다시 풀어보세요!\n" +
            "다음 주에는 더 잘 볼 수 있을거에요 :)"
    val report_weekly_guide_H = "엇! 이번주에 약점 채우기 공부에 소홀하진 않았나요?\n" +
            "점수 상승을 만들 수 있도록 %s 먼저 다시 풀어봅시다 :)\n" +
            "다음 주는 만족하는 결과를 얻을거에요!"
    val report_weekly_guide_I = "나의 약점 찾기 딱 좋은 결과네요!\n" +
            "약점 점검을 위해 %s을 다시 풀어볼까요?\n" +
            "다음 주는 더 잘할 수 있을거에요 :)"
    val report_weekly_guide_J = "수학에선 꾸준함이 생명! 지난 번보다 상승했어요!\n" +
            "이 상승세를 유지할 수 있도록 %s을 복습해볼까요?\n" +
            "다음 주는 더 오를거에요 :)"
    val report_weekly_guide_K = "아이쿠! 지난주보다 점수가 떨어졌네요!\n" +
            "약점을 채운다면 충분히 상승할 수 있을거에요 :)\n" +
            "더 나은 다음주를 위해 %s 복습을 추천합니다!"
    val report_weekly_guide_L = "나의 약점 찾기 딱 좋은 결과네요!\n" +
            "더 잘할 수 있도록 %s을 다시 풀어볼까요?\n" +
            "다음 주는 점수 상승 만들어보아요 :)"
    val report_weekly_guide_M = "이 점수 실화인가요?! 100점이에요!!\n" +
            "빈틈없이 완벽하고 싶다면 %s 복습을 추천합니다 :)\n" +
            "다음주도 100점 받을 수 있을거에요!"
    val report_weekly_guide_N = "대박!! 100점이라니!!\n" +
            "이번 주 열심히 하셨군요?! 지난 주 보다 상승했어요!!\n" +
            "다음 주도 완벽하고 싶다면 %s 복습을 추천합니다 :)"
    val report_weekly_guide_P = "연속 100점이라니! 대박!!\n" +
            "약점을 꼼꼼히 채우는 한 주를 보내셨군요!?\n" +
            "다음 주도 완벽하고 싶다면 %s 복습을 추천합니다 :)"

    fun getDailyListFinishQ(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        return if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            list_daily_finish_B
        else list_daily_finish_A
    }

    fun getDailyReportGuideQ(beforeScore: Int, beforeScore2: Int, problems: List<Problem>): CharSequence {
        val sortedProblems = problems.sortedBy { it.correctRate }
        val correctProblems = problems.filter { it.getResultByScoring() == Result.correct }
        val incorrectProblems = problems.filter { it.getResultByScoring() != Result.correct }.sortedByDescending { it.correctRate }

        val score: Int = ((correctProblems.size / problems.size.toFloat()) * 100).toInt()

        val recommendText = if(score <= 40) {
            val list = listOf(incorrectProblems[0], incorrectProblems[1]).sortedBy { it.problemNum }
            "${list[0].problemNum}, ${list[1].problemNum}번"
        } else {
            val list = listOf(sortedProblems[0], sortedProblems[1]).sortedBy { it.problemNum }
            "${list[0].problemNum}, ${list[1].problemNum}번"
        }

        return if(beforeScore <= 40 && beforeScore2 <= 40 && score <= 40) {
            val text = report_daily_guide_A.format(recommendText)
            text.partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
        } else if(beforeScore == 100 && beforeScore2 == 100 && score == 100) {
            report_daily_guide_F.partialFont(extraBold, "연속 2회 만점!!")
        } else {
            if(score == 0) {
                val text = report_daily_guide_G.format(recommendText)
                text.partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
            } else if (score <= 40) {
                val text = report_daily_guide_H.format(recommendText)
                text.partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
            } else if(score in 41..80) {
                val text = report_daily_guide_I.format(recommendText)
                text.partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
            } else {
                val text = report_daily_guide_J.format(recommendText)
                text.partialFont(Theme.extraBold(context), text.indexOf(recommendText), text.indexOf(recommendText) + recommendText.length)
            }
        }
    }

    fun getWeeklyReportGuideQ(prevScore: Int?, problems: List<Problem>, userName: String): CharSequence {
        val sortedProblems = problems.sortedBy { it.correctRate }
        val correctProblems = problems.filter { it.getResultByScoring() == Result.correct }
        val incorrectProblems = problems.filter { it.getResultByScoring() != Result.correct }.sortedByDescending { it.correctRate }

        val score: Int = ((correctProblems.size / problems.size.toFloat()) * 100).toInt()

        val recommendText = if(score < 60) {
            val list = listOf(incorrectProblems[0], incorrectProblems[1], incorrectProblems[2]).sortedBy { it.problemNum }
            "${list[0].problemNum}, ${list[1].problemNum}, ${list[2].problemNum}번"
        } else {
            val list = listOf(sortedProblems[0], sortedProblems[1], sortedProblems[2]).sortedBy { it.problemNum }
            "${list[0].problemNum}, ${list[1].problemNum}, ${list[2].problemNum}번"
        }

        val curation: String =  if(score == 0) {
            when {
                prevScore == null -> report_weekly_guide_A.format(recommendText)
                prevScore > score -> report_weekly_guide_C.format(recommendText)
                prevScore == score -> report_weekly_guide_D.format(userName, recommendText)
                else -> report_weekly_guide_A.format(recommendText)
            }
        } else if (score < 60) {
            when {
                prevScore == null -> report_weekly_guide_E.format(recommendText)
                prevScore < score -> report_weekly_guide_F.format(recommendText)
                prevScore > score -> report_weekly_guide_G.format(recommendText)
                else -> report_weekly_guide_H.format(recommendText)
            }

        } else if(score in 60..99) {
            when {
                prevScore == null -> report_weekly_guide_I.format(recommendText)
                prevScore < score -> report_weekly_guide_J.format(recommendText)
                prevScore > score-> report_weekly_guide_K.format(recommendText)
                else -> report_weekly_guide_L.format(recommendText)
            }
        } else {
            when {
                prevScore == null -> report_weekly_guide_M.format(recommendText)
                prevScore < score -> report_weekly_guide_N.format(recommendText)
                else -> report_weekly_guide_P.format(recommendText)
            }
        }

        return curation.partialFont(Theme.extraBold(context), recommendText)
    }
}