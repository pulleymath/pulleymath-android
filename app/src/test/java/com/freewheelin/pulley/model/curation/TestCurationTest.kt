package com.freewheelin.pulley.legacy.model.curation

import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.legacy.model.Problem
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test

class TestCurationTest: ContextTest() {
    val curation = TestCuration(context)

    val correct1 = Problem().apply {
        problemNum = 1
        this.rawResult = 1
    }
    val correct2 = Problem().apply {
        problemNum = 2
        this.rawResult = 1
    }
    val correct3 = Problem().apply {
        problemNum = 3
        this.rawResult = 1
    }
    val correct4 = Problem().apply {
        problemNum = 4
        this.rawResult = 1
    }
    val correct5 = Problem().apply {
        problemNum = 5
        this.rawResult = 1
    }

    val incorrect1 = Problem().apply {
        this.rawResult = -2
        problemNum = 1
    }
    val incorrect2 = Problem().apply {
        this.rawResult = -2
        problemNum = 2
    }
    val incorrect3 = Problem().apply {
        this.rawResult = -2
        problemNum = 3
    }
    val incorrect4 = Problem().apply {
        this.rawResult = -2
        problemNum = 4
    }
    val incorrect5 = Problem().apply {
        this.rawResult = -2
        problemNum = 5
    }

    @Test
    fun `데일리 보고서 큐레이션을 적절하게 리턴해야한다`() {
        val curation0 = curation.getDailyReportGuideQ(39,39, listOf(incorrect1, incorrect2, incorrect3, incorrect4, incorrect5))
        assertEquals("많이 어려웠나요?\n알지만 틀릴 수 있는 문제인 1, 2번 만큼은 꼭 다시 풀어보세요!\n내일은 더 잘 할 수 있도록 난이도를 조금 조정해서 출제할게요 :)", curation0.toString())

        val curation1 = curation.getDailyReportGuideQ(100,100, listOf(correct1, correct2, correct3, correct4, correct5))
        assertEquals("우와! 연속 2회 만점!! 수학의 신인가요?!\n스스로 긴가민가했던 문제를 다시보길 추천해요 :)", curation1.toString())

        val curation2 = curation.getDailyReportGuideQ(88, 88, listOf(incorrect1, incorrect2, incorrect3, incorrect4, incorrect5))
        assertEquals("당황하지 말아요 :)\n나에게 약한 점을 채울 수 있는 아주 좋은 기회이니까요!\n우리 최소한 1, 2번 만큼은 꼭 다시 풀어봅시다!", curation2.toString())

        val curation3 = curation.getDailyReportGuideQ(88, 88, listOf(correct1, correct2, incorrect3, incorrect4, incorrect5))
        assertEquals("약점 채우기 딱 좋은 기회네요!\n정답률이 높았던 3, 4번 부터 꼭 다시 풀어보세요!\n조금 더 고민하면 해결할 수 있는 문제가 있을거에요 :)", curation3.toString())

        val curation4_1 = curation.getDailyReportGuideQ(88, 88, listOf(correct1, correct2, correct3, incorrect4, incorrect5))
        assertEquals("어려운 문제 때문에 힘들진 않았나요?\n다른 친구들도 어려워한 1, 2번은 꼭 다시 풀어보세요!\n나의 약점이 어디인지 알아낼 수 있을거에요 :)", curation4_1.toString())

        val curation4_2 = curation.getDailyReportGuideQ(88, 88, listOf(correct1, correct2, correct3, correct4, incorrect5))
        assertEquals("어려운 문제 때문에 힘들진 않았나요?\n다른 친구들도 어려워한 1, 2번은 꼭 다시 풀어보세요!\n나의 약점이 어디인지 알아낼 수 있을거에요 :)", curation4_2.toString())

        val curation5 = curation.getDailyReportGuideQ(80,80, listOf(correct1, correct2, correct3, correct4, correct5))
        assertEquals("우와! 만점이라니!! 정말 멋져요 :D\n다른 친구들도 어려워한 1, 2번을 복습해볼까요?\n내일도 100점 가능할거에요!", curation5.toString())
    }

    @Test
    fun `위클리 보고서 큐레이션을 적절하게 리턴해야한다`() {
        val curation0 = curation.getWeeklyReportGuideQ(null, listOf(incorrect1, incorrect2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("처음 보는 주간테스트라 당황했나요?\n괜찮아요! 다음 주간테스트는 더 잘 볼 수 있을거에요 :)\n여기서 끝내지 말고 1, 2, 3번은 꼭 다시 풀어봅시다!", curation0.toString())

        val curation1 = curation.getWeeklyReportGuideQ(10, listOf(incorrect1, incorrect2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("당황하지 말아요 :)\n나에게 약한 점을 채울 수 있는 아주 좋은 기회이니까요!\n우리 최소한 1, 2, 3번 만큼은 꼭 다시 풀어봅시다!", curation1.toString())

        val curation2 = curation.getWeeklyReportGuideQ(0, listOf(incorrect1, incorrect2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("헉! 무슨일이죠?\n현태님의 약점이 아직 채워지지 않은걸까요?ㅜㅜ\n다음 주엔 더 잘할 수 있도록 1, 2, 3번 만큼은 꼭 다시 풀어봅시다!", curation2.toString())

        val curation3 = curation.getWeeklyReportGuideQ(null, listOf(correct1, correct2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("헛! 많이 어려웠나요?\n아깝게 틀린 3, 4, 5번 만큼은 꼭 다시 풀어보세요!\n다음엔 꼭 맞힐 수 있을거에요 :)", curation3.toString())

        val curation4 = curation.getWeeklyReportGuideQ(30, listOf(correct1, correct2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("천천히 가는 거북이가 승리한다!\n지난 주간테스트보다 점수가 올랐어요!\n다음 주에도 상승할 수 있도록 3, 4, 5번은 꼭 다시 풀어보세요!", curation4.toString())

        val curation5 = curation.getWeeklyReportGuideQ(70, listOf(correct1, correct2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("헛! 많이 어려웠나요?\n점수 상승을 만들어줄 3, 4, 5번 만큼은 꼭 다시 풀어보세요!\n다음 주에는 더 잘 볼 수 있을거에요 :)", curation5.toString())

        val curation6 = curation.getWeeklyReportGuideQ(40, listOf(correct1, correct2, incorrect3, incorrect4, incorrect5), "현태")
        assertEquals("엇! 이번주에 약점 채우기 공부에 소홀하진 않았나요?\n점수 상승을 만들 수 있도록 3, 4, 5번 먼저 다시 풀어봅시다 :)\n다음 주는 만족하는 결과를 얻을거에요!", curation6.toString())

        val curation7 = curation.getWeeklyReportGuideQ(null, listOf(correct1, correct2, correct3, incorrect4, incorrect5), "현태")
        assertEquals("나의 약점 찾기 딱 좋은 결과네요!\n약점 점검을 위해 1, 2, 3번을 다시 풀어볼까요?\n다음 주는 더 잘할 수 있을거에요 :)", curation7.toString())

        val curation8 = curation.getWeeklyReportGuideQ(40, listOf(correct1, correct2, correct3, incorrect4, incorrect5), "현태")
        assertEquals("수학에선 꾸준함이 생명! 지난 번보다 상승했어요!\n이 상승세를 유지할 수 있도록 1, 2, 3번을 복습해볼까요?\n다음 주는 더 오를거에요 :)", curation8.toString())

        val curation9 = curation.getWeeklyReportGuideQ(90, listOf(correct1, correct2, correct3, incorrect4, incorrect5), "현태")
        assertEquals("아이쿠! 지난주보다 점수가 떨어졌네요!\n약점을 채운다면 충분히 상승할 수 있을거에요 :)\n더 나은 다음주를 위해 1, 2, 3번 복습을 추천합니다!", curation9.toString())

        val curation10 = curation.getWeeklyReportGuideQ(60, listOf(correct1, correct2, correct3, incorrect4, incorrect5), "현태")
        assertEquals("나의 약점 찾기 딱 좋은 결과네요!\n더 잘할 수 있도록 1, 2, 3번을 다시 풀어볼까요?\n다음 주는 점수 상승 만들어보아요 :)", curation10.toString())

        val curation11 = curation.getWeeklyReportGuideQ(null, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, incorrect5), "현태")
        assertEquals("나의 약점 찾기 딱 좋은 결과네요!\n약점 점검을 위해 1, 2, 3번을 다시 풀어볼까요?\n다음 주는 더 잘할 수 있을거에요 :)", curation11.toString())

        val curation12 = curation.getWeeklyReportGuideQ(40, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, incorrect5), "현태")
        assertEquals("수학에선 꾸준함이 생명! 지난 번보다 상승했어요!\n이 상승세를 유지할 수 있도록 1, 2, 3번을 복습해볼까요?\n다음 주는 더 오를거에요 :)", curation12.toString())

        val curation13 = curation.getWeeklyReportGuideQ(90, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, incorrect5), "현태")
        assertEquals("나의 약점 찾기 딱 좋은 결과네요!\n더 잘할 수 있도록 1, 2, 3번을 다시 풀어볼까요?\n다음 주는 점수 상승 만들어보아요 :)", curation13.toString())

        val curation14 = curation.getWeeklyReportGuideQ(null, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, correct5), "현태")
        assertEquals("이 점수 실화인가요?! 100점이에요!!\n빈틈없이 완벽하고 싶다면 1, 2, 3번 복습을 추천합니다 :)\n다음주도 100점 받을 수 있을거에요!", curation14.toString())

        val curation15 = curation.getWeeklyReportGuideQ(90, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, correct5), "현태")
        assertEquals("대박!! 100점이라니!!\n이번 주 열심히 하셨군요?! 지난 주 보다 상승했어요!!\n다음 주도 완벽하고 싶다면 1, 2, 3번 복습을 추천합니다 :)", curation15.toString())

        val curation16 = curation.getWeeklyReportGuideQ(100, listOf(correct1, correct2, correct3, correct4, correct5, correct1, correct2, correct3, correct4, correct5), "현태")
        assertEquals("연속 100점이라니! 대박!!\n약점을 꼼꼼히 채우는 한 주를 보내셨군요!?\n다음 주도 완벽하고 싶다면 1, 2, 3번 복습을 추천합니다 :)", curation16.toString())
    }
}

