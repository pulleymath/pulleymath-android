package com.freewheelin.pulley.model.curation

import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.model.ChapterAmountAnalysis
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.model.LevelRatioAnalysis
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

class MyCurationTest: ContextTest() {
    val curation = MyCuration(context)


    val analysis: List<ChapterAnalysis>
        get() {
            val analyisMock0 = Mockito.mock(ChapterAnalysis::class.java)
            val analyisMock1 = Mockito.mock(ChapterAnalysis::class.java)
            val analyisMock2 = Mockito.mock(ChapterAnalysis::class.java)
            Mockito.`when`(analyisMock0.name).thenReturn("첫번째아날")
            Mockito.`when`(analyisMock1.name).thenReturn("두번째아날")
            Mockito.`when`(analyisMock2.name).thenReturn("세번째아날")



            return listOf(
                    analyisMock0,
                    analyisMock1,
                    analyisMock2
            )
        }
    @Test
    fun `단원분석 - 요약 큐레이션이 적절하게 나와야한다`() {

        val chapterAnalysis = analysis

        /**
         * 기존 점수 기록 없음.
         */
        Mockito.`when`(chapterAnalysis[0].myCorrectRate).thenReturn(20)
        assertEquals("첫번째아날 과목의 성취도가 가장 높아요.", curation.getUnitSummaryQ(10, null, chapterAnalysis).toString())

        Mockito.`when`(chapterAnalysis[1].averageCorrectRateSameGrade).thenReturn(50)
        Mockito.`when`(chapterAnalysis[1].myCorrectRate).thenReturn(0)
        assertEquals("두번째아날 과목의 성취도가 가장 낮아요.", curation.getUnitSummaryQ(10, null, chapterAnalysis).toString())

        /**
         * 기존 점수 기록
         */
        // 향상
        assertEquals("지난 10일에 비해서 성적이 10% 향상되었어요!", curation.getUnitSummaryQ(10, 10, chapterAnalysis).toString())
        // 유지
        assertEquals("지난 10일에 비해서 성적이 유지되었어요.", curation.getUnitSummaryQ(10, 0, chapterAnalysis).toString())
        // 하락
        assertEquals("지난 10일에 비해서 성적이 10% 떨어졌어요.", curation.getUnitSummaryQ(10, -10, chapterAnalysis).toString())
    }


    @Test
    fun `단원분석 - 성취도 순위 큐레이션이 적절하게 나와야한다`() {
        val chapterAnalysis = analysis
        assertEquals("첫번째아날 단원의 성취도가 가장 낮아요.", curation.getUnitAchieveQ(chapterAnalysis[0], true).toString())

        assertEquals("두번째아날 단원의 성취도가 가장 높아요.", curation.getUnitAchieveQ(chapterAnalysis[1], false).toString())
    }

    @Test
    fun `단원분석 - 단원별 분석 큐레이션이 적절하게 나와야한다`() {
        val chapterAnalysis = analysis
        Mockito.`when`(chapterAnalysis[1].myCorrectRate).thenReturn(60)
        Mockito.`when`(chapterAnalysis[2].myCorrectRate).thenReturn(80)

        assertEquals("첫번째아날 단원의 보완이 필요해요.", curation.getUnitChapterQ(chapterAnalysis).toString())
    }

    @Test
    fun `난이도별 분석 - 요약 큐레이션이 적절하게 나와야한다`() {
        val chapterAnalysis = analysis
        Mockito.`when`(chapterAnalysis[1].myCorrectRate).thenReturn(50)
        assertEquals("두번째아날 난이도 정답률이 같은 등급 친구들 대비 가장 높아요.", curation.getLevelSummaryQ(chapterAnalysis).toString())


        Mockito.`when`(chapterAnalysis[0].averageCorrectRateSameGrade).thenReturn(90)
        assertEquals("첫번째아날 난이도 정답률이 같은 등급 친구들 대비 가장 낮아요.", curation.getLevelSummaryQ(chapterAnalysis).toString())

    }

    @Test
    fun `난이도별 분석 - 난이도별 연습 비율 분석 큐레이션이 적절하게 나와야한다`() {
        val levelRatioAnalysis0 = Mockito.mock(LevelRatioAnalysis::class.java)

        Mockito.`when`(levelRatioAnalysis0.getMaxValOnComparing()).thenReturn(Pair("상", false))
        assertEquals("같은 등급 친구들 대비 상 난이도의 연습량이 가장 부족해요!", curation.getLevelStudyRatioQ(levelRatioAnalysis0).toString())

        Mockito.`when`(levelRatioAnalysis0.getMaxValOnComparing()).thenReturn(Pair("상", true))
        assertEquals("같은 등급 친구들 대비 상 난이도의 연습량이 가장 많아요!", curation.getLevelStudyRatioQ(levelRatioAnalysis0).toString())
    }


    @Test
    fun `학습량 분석 - 요약 큐레이션이 적절하게 나와야한다`() {
        val chapterAmountAnalysis1 = ChapterAmountAnalysis().apply {
            chapterName = "첫번째"
            problemTotalNumber = 340
        }
        val chapterAmountAnalysis2 = ChapterAmountAnalysis().apply {
            chapterName = "두번째"
            problemTotalNumber = 310
        }
        val chapterAmountAnalysis3 = ChapterAmountAnalysis().apply {
            chapterName = "세째"
            problemTotalNumber = 300
        }

        assertEquals("첫번째 과목을 집중해서 공부했네요.", curation.getStudyAmountSummaryQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        chapterAmountAnalysis2.problemTotalNumber = 340
        assertEquals("첫번째, 두번째 과목을 집중해서 공부했네요.", curation.getStudyAmountSummaryQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        chapterAmountAnalysis3.problemTotalNumber = 340
        assertEquals("첫번째, 두번째, 세째 과목을 집중해서 공부했네요.", curation.getStudyAmountSummaryQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        assertEquals("분석을 위한 학습 내역이 부족해요.", curation.getStudyAmountSummaryQ(listOf()).toString())
    }

    @Test
    fun `학습량 분석 - 대단원별 학습량 비교 그래프 큐레이션이 적절하게 나와야한다`() {
        val chapterAmountAnalysis1 = ChapterAmountAnalysis().apply {
            chapterName = "첫번째"
            problemTotalNumber = 340
        }
        val chapterAmountAnalysis2 = ChapterAmountAnalysis().apply {
            chapterName = "두번째"
            problemTotalNumber = 310
        }
        val chapterAmountAnalysis3 = ChapterAmountAnalysis().apply {
            chapterName = "세째"
            problemTotalNumber = 300
        }

        assertEquals("첫번째 학습량이 많았어요.", curation.getStudyAmountCompareByUnitQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        chapterAmountAnalysis2.problemTotalNumber = 340
        assertEquals("첫번째, 두번째 학습량이 많았어요.", curation.getStudyAmountCompareByUnitQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        chapterAmountAnalysis3.problemTotalNumber = 340
        assertEquals("첫번째, 두번째, 세째 학습량이 많았어요.", curation.getStudyAmountCompareByUnitQ(listOf(chapterAmountAnalysis1, chapterAmountAnalysis2, chapterAmountAnalysis3)).toString())

        assertEquals("분석을 위한 학습 내역이 부족해요.", curation.getStudyAmountCompareByUnitQ(listOf()).toString())
    }
}