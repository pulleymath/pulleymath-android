package com.freewheelin.pulley.model.curation

import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.MockExamAnalysis
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ScoreAnalysis
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class MockReportCurationTest: ContextTest()  {
    val curation = MockReportCuration(context)

    @Test
    fun `요약 큐레이션을 적절하게 리턴해야한다`() {
        assertEquals("2등급까지 27점 더 필요해요.",curation.getSummaryQ(3, 60, 87).toString())
        assertEquals("2등급까지 -3점 더 필요해요.",curation.getSummaryQ(3, 60, 57).toString())

        assertEquals("만점까지 40점 더 필요해요. 100점까지 거의 다 왔어요!",curation.getSummaryQ(1, 60, 57).toString())
        assertEquals("대박!! 100점이에요!! 넘볼 수 없는 완벽함!!",curation.getSummaryQ(1, 100, 57).toString())
    }

    @Test
    fun `단원 분석 과목 큐레이션을 적절하게 리턴해야 한다`() {
        val chapterAnalysis0 = Mockito.mock(ChapterAnalysis::class.java)
        val chapterAnalysis1 = Mockito.mock(ChapterAnalysis::class.java)
        val chapterAnalysis2 = Mockito.mock(ChapterAnalysis::class.java)

        Mockito.`when`(chapterAnalysis0.name).thenReturn("첫번째")
        Mockito.`when`(chapterAnalysis1.name).thenReturn("두번째")
        Mockito.`when`(chapterAnalysis2.name).thenReturn("세번째")

        val curation0 = curation.getSubjectQ(listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2), 100)
        assertTrue("대박! 출제자인가요?! 100점이라니!! 완벽해요!!" == curation0.toString() || "이 점수가 수능점수가 될거에요!" == curation0.toString())

        Mockito.`when`(chapterAnalysis0.myCorrectRate).thenReturn(80)
        assertEquals("첫번째 과목을 잘하고 있어요! 난이도를 조금 더 높여도 좋겠어요!", curation.getSubjectQ(listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2), 90).toString())

        Mockito.`when`(chapterAnalysis0.myCorrectRate).thenReturn(30)
        Mockito.`when`(chapterAnalysis1.averageCorrectRateSameGrade).thenReturn(40)
        assertEquals("두번째 과목이 많이 어렵나요? 문제리뷰를 하면 더 잘할 수 있을거에요!", curation.getSubjectQ(listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2), 90).toString())
    }

    @Test
    fun `과목 문항 분석 큐레이션을 적절하게 리턴해야한다`() {
        val problem0 = Problem().apply {
            problemNum = 1
            correctTimes = 10
            totalTimes = 100
            rawResult = 1
        }

        val problem1 = Problem().apply {
            problemNum = 2
            correctTimes = 5
            totalTimes = 100
            rawResult = 1
        }

        val problem2 = Problem().apply {
            problemNum = 3
            correctTimes = 21
            totalTimes = 100
            rawResult = 1
        }

        val problem3 = Problem().apply {
            problemNum = 4
            correctTimes = 17
            totalTimes = 100
            rawResult = 1
        }

        assertEquals("확률과 통계 과목은 모두 정답이에요!", curation.getSubjectGuideQ("확률과 통계", listOf(problem0, problem1, problem2, problem3)).toString())
        problem0.rawResult = -2
        assertEquals("확률과 통계에서 딱 1문제 틀렸어요!", curation.getSubjectGuideQ("확률과 통계", listOf(problem0, problem1, problem2, problem3)).toString())
        problem2.rawResult = -2
        assertEquals("확률과 통계에서 1번 만큼은 꼼꼼하게 리뷰해보아요!", curation.getSubjectGuideQ("확률과 통계", listOf(problem0, problem1, problem2, problem3)).toString())
        problem1.rawResult = -2
        assertEquals("확률과 통계에서 2번 만큼은 꼼꼼하게 리뷰해보아요!", curation.getSubjectGuideQ("확률과 통계", listOf(problem0, problem1, problem2, problem3)).toString())
        problem3.rawResult = -2
        assertEquals("확률과 통계에서 1번, 2번은 더 집중해서 리뷰해보아요!", curation.getSubjectGuideQ("확률과 통계", listOf(problem0, problem1, problem2, problem3)).toString())
    }

    @Test
    fun `단원 분석 큐레이션을 적절하게 리턴해야한다`() {
        val chapterAnalysis0 = Mockito.mock(ChapterAnalysis::class.java)
        val chapterAnalysis1 = Mockito.mock(ChapterAnalysis::class.java)
        val chapterAnalysis2 = Mockito.mock(ChapterAnalysis::class.java)

        Mockito.`when`(chapterAnalysis0.name).thenReturn("첫번째")
        Mockito.`when`(chapterAnalysis1.name).thenReturn("두번째")
        Mockito.`when`(chapterAnalysis2.name).thenReturn("세번째")

        Mockito.`when`(chapterAnalysis0.belowRate).thenReturn(0f)
        Mockito.`when`(chapterAnalysis1.belowRate).thenReturn(0f)
        Mockito.`when`(chapterAnalysis2.belowRate).thenReturn(0f)

        Mockito.`when`(chapterAnalysis0.myRate).thenReturn(0.1f)
        Mockito.`when`(chapterAnalysis1.myRate).thenReturn(0.1f)
        Mockito.`when`(chapterAnalysis2.myRate).thenReturn(0.1f)

        /**
         *  1 또는 5등급. 모든 단원 점수가 학생 평균 이상
         */
        assertEquals("모든 단원을 같은 등급 친구들보다 잘하고 있네요!", curation.getUnitGuideQ(1, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())
        assertEquals("모든 단원을 같은 등급 친구들보다 잘하고 있네요!", curation.getUnitGuideQ(5, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())

        /**
         *  2 ~ 4등급. 모든 단원 점수가 학생 평균 이상
         */
        assertEquals("모든 단원을 골고루 잘하고 있네요! 1등급도 가능하겠어요!", curation.getUnitGuideQ(2, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())
        assertEquals("모든 단원을 골고루 잘하고 있네요! 1등급도 가능하겠어요!", curation.getUnitGuideQ(3, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())
        assertEquals("모든 단원을 골고루 잘하고 있네요! 1등급도 가능하겠어요!", curation.getUnitGuideQ(4, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())

        /**
         *  모든 대단원이 같은 등급 평균보다 낮을때
         */
        Mockito.`when`(chapterAnalysis0.belowRate).thenReturn(0.2f)
        Mockito.`when`(chapterAnalysis1.belowRate).thenReturn(0.2f)
        Mockito.`when`(chapterAnalysis2.belowRate).thenReturn(0.2f)
        Mockito.`when`(chapterAnalysis2.myRate).thenReturn(0.18f)
        assertEquals("세번째 단원이 강점이 될 수 있도록 복습해보기를 추천해요! :)", curation.getUnitGuideQ(2, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())

        /**
         * 대단원 평균보다 높고 낮은것이 모두 나왔을때
         */
        Mockito.`when`(chapterAnalysis2.myRate).thenReturn(0.40f)
        Mockito.`when`(chapterAnalysis0.myRate).thenReturn(0.02f)
        assertEquals("세번째 단원을 같은 등급 친구들보다 잘하고 있어요!", curation.getUnitGuideQ(2, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())

        Mockito.`when`(chapterAnalysis2.myRate).thenReturn(0.30f)
        assertEquals("첫번째 단원이 같은 등급 친구들보다 가장 낮아요 ㅜㅜ", curation.getUnitGuideQ(2, listOf(chapterAnalysis0, chapterAnalysis1, chapterAnalysis2)).toString())
    }

    @Test
    fun `배점대별 큐레이션을 적절하게 리턴해야 한다`() {
        val score2 = Mockito.mock(ScoreAnalysis::class.java)
        val score3 = Mockito.mock(ScoreAnalysis::class.java)
        val score4 = Mockito.mock(ScoreAnalysis::class.java)
        val scoreKiller = Mockito.mock(ScoreAnalysis::class.java)

        Mockito.`when`(score2.title).thenReturn("2점")
        Mockito.`when`(score3.title).thenReturn("3점")
        Mockito.`when`(score4.title).thenReturn("4점")
        Mockito.`when`(scoreKiller.title).thenReturn("킬러")

        Mockito.`when`(score2.myRate).thenReturn(0.3f)
        Mockito.`when`(score3.myRate).thenReturn(0.3f)
        Mockito.`when`(score4.myRate).thenReturn(0.3f)
        Mockito.`when`(scoreKiller.myRate).thenReturn(0.3f)

        /***
         * 동등급 대비 모두 + 인 경우 , 1등급
         */
        Mockito.`when`(score2.myRate).thenReturn(0.3f)
        Mockito.`when`(score3.myRate).thenReturn(0.3f)
        Mockito.`when`(score4.myRate).thenReturn(0.3f)
        Mockito.`when`(scoreKiller.myRate).thenReturn(0.3f)

        assertEquals("모든 배점에서 잘하고 있어요! 수능 만점도 가능하겠어요!",curation.getScoreGuideQ(1, listOf(score2,score3,score4,scoreKiller)).toString())
        assertEquals("모든 배점에서 잘하고 있어요! 1등급 도전해도 좋겠어요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
        assertEquals("모든 배점에서 잘하고 있어요! 2등급 도전해도 좋겠어요!",curation.getScoreGuideQ(3, listOf(score2,score3,score4,scoreKiller)).toString())
        assertEquals("모든 배점에서 잘하고 있어요! 3등급 도전해도 좋겠어요!",curation.getScoreGuideQ(4, listOf(score2,score3,score4,scoreKiller)).toString())

        /**
         * 동등급 대비 모두 - 인 경우
         */
        Mockito.`when`(score2.sameRate).thenReturn(0.35f)
        Mockito.`when`(score3.sameRate).thenReturn(0.35f)
        Mockito.`when`(score4.sameRate).thenReturn(0.35f)
        Mockito.`when`(scoreKiller.sameRate).thenReturn(0.35f)

        Mockito.`when`(score2.sameRate).thenReturn(0.4f)
        assertEquals("유독 2점 문항이 어렵나요? 실수는 없는지 꼭 오답정리해보아요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
        Mockito.`when`(score3.sameRate).thenReturn(0.45f)
        assertEquals("유독 3점 문항이 어렵나요? 반복연습하면 정답률 높일 수 있어요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
        Mockito.`when`(score4.sameRate).thenReturn(0.50f)
        assertEquals("유독 4점 문항이 어렵나요? 스스로 도전하는것이 제일 중요해요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
        Mockito.`when`(scoreKiller.sameRate).thenReturn(0.55f)
        assertEquals("유독 킬러 문항이 어렵나요? 문제의도를 정확히 파악하는데 집중해보아요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())


        /**
         * 동등급 대비 + , - 가 모두 있는 경우
         */
        Mockito.`when`(score2.sameRate).thenReturn(0.35f)
        Mockito.`when`(score3.sameRate).thenReturn(0.35f)
        Mockito.`when`(score4.sameRate).thenReturn(0.35f)
        Mockito.`when`(scoreKiller.sameRate).thenReturn(0.35f)

        Mockito.`when`(scoreKiller.myRate).thenReturn(0.1f)
        Mockito.`when`(score4.myRate).thenReturn(0.9f)
        assertEquals("같은 등급 친구들 보다 4점 문제를 잘 풀고 있어요!",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
        Mockito.`when`(score4.myRate).thenReturn(0.4f)
        assertEquals("같은 등급 친구들 보다 킬러 문제가 약해요 ㅜㅜ",curation.getScoreGuideQ(2, listOf(score2,score3,score4,scoreKiller)).toString())
    }
}