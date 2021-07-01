package com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component

enum class FilterType {
    과목_전체,
    수학_상,
    수학_하,
    math1,
    math2,
    probabilityAndStatistics,
    calculus,
    geometry,

    allLevel,
    low,
    middleLow,
    middle,
    high,
    highest,

    allCategory,
    test,
    unitStudy,
    mockText,

    allViewType,
    exceptClear,
    includeClear,
    correctProblem,
    incorrectProblem,
    notSolvedProblem;

    val text: String
        get() {
            when (this) {
                과목_전체 -> return "전체"
                수학_상 -> return "수학(상)"
                수학_하 -> return "수학(하)"
                math1 -> return "수학1"
                math2 -> return "수학2"
                probabilityAndStatistics -> return "확률과 통계"
                calculus -> return "미적분"
                geometry -> return "기하"

                allLevel -> return "전체"
                low -> return "하"
                middleLow -> return "중하"
                middle -> return "중"
                high -> return "상"
                highest -> return "최상"

                allCategory -> return "전체"
                test -> return "테스트"
                unitStudy -> return "유형학습"
                mockText -> return "모의고사"

                allViewType -> return "전체"
                exceptClear -> return "클리어 미포함"
                includeClear -> return "클리어 포함"
                correctProblem -> return "맞은 문제"
                incorrectProblem -> return "틀린 문제"
                notSolvedProblem -> return "안 푼 문제"
            }
        }

    val exclusiveSet: Set<FilterType>
        get() {
            when(this) {
                과목_전체 -> return setOf(수학_상, 수학_하, math1, math2, probabilityAndStatistics, calculus, geometry)
                수학_상 -> return setOf(과목_전체)
                수학_하 -> return setOf(과목_전체)
                math1 -> return setOf(과목_전체)
                math2 -> return setOf(과목_전체)
                probabilityAndStatistics -> return setOf(과목_전체)
                calculus -> return setOf(과목_전체)
                geometry -> return setOf(과목_전체)

                allLevel -> return setOf(low, middleLow, middle, high, highest)
                low -> return setOf(allLevel)
                middleLow -> return setOf(allLevel)
                middle -> return setOf(allLevel)
                high -> return setOf(allLevel)
                highest -> return setOf(allLevel)

                allCategory -> return setOf(test, unitStudy, mockText)
                test -> return setOf(allCategory)
                unitStudy -> return setOf(allCategory)
                mockText -> return setOf(allCategory)

                allViewType -> return setOf(exceptClear, includeClear, correctProblem, incorrectProblem, notSolvedProblem)
                exceptClear -> return setOf(allViewType, includeClear)
                includeClear -> return setOf(allViewType, exceptClear)
                correctProblem -> return setOf(allViewType)
                incorrectProblem -> return setOf(allViewType)
                notSolvedProblem -> return setOf(allViewType)
            }
        }

    val commonSet: Set<FilterType>
        get() {
            return setOf(과목_전체,
                    수학_상,
                    수학_하,
                    math1,
                    math2,
                    probabilityAndStatistics,
                    calculus,
                    geometry,

                    allLevel,
                    low,
                    middleLow,
                    middle,
                    high,
                    highest,

                    allCategory,
                    test,
                    unitStudy,
                    mockText)
        }
}