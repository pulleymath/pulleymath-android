package com.freewheelin.pulley.legacy.model.contents

import com.freewheelin.pulley.legacy.model.ChapterAnalysis
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.day
import com.freewheelin.pulley.legacy.utils.month
import java.io.Serializable
import java.util.Date

class Test: Content {
    enum class TestLevel(val title: String) {
        HIGH("고난도 위주로"),
        LIKE_ME("내 성적에 맞게"),
        EASY("쉬운 것부터");
        companion object {
            fun titleOfNonNull(title: String): TestLevel =
                values()
                    .firstOrNull { it.title == title } ?: LIKE_ME

            fun ordinalOfNonNull(ordinal: Int): TestLevel =
                values()
                    .firstOrNull { it.ordinal == ordinal } ?: LIKE_ME

        }
    }
    enum class TestRange(val title: String) {
        RECENT_RANGE("최근 공부한 범위에 맞추어서"),
        ALL_RANGE("수능 전범위에 맞추어서"),
        SUBJECT_BY_GRADE("학년별 과목에 맞추어서");
        companion object {
            fun titleOfNonNull(title: String): TestRange =
                values()
                    .firstOrNull { it.title == title } ?: SUBJECT_BY_GRADE
            fun ordinalOfNonNull(ordinal: Int): TestRange =
                values()
                    .firstOrNull { it.ordinal == ordinal } ?: SUBJECT_BY_GRADE

        }
    }
    enum class TestType(val rawText: String) {
        initial("INIT"),
        init_V2("INIT_V2"),
        daily("DAILY"),
        weekly("WEEKLY"),
        theme("THEME"),
        wrong("WRONG");

        val eventItemValue: String
        get() {
            return when(this) {
                daily -> "데일리테스트"
                weekly -> "주간테스트"
                wrong -> "오답테스트"
                else -> rawText
            }
        }

        companion object {
            fun init(rawString: String): TestType {
                return when(rawString) {
                    initial.rawText -> initial
                    daily.rawText -> daily
                    weekly.rawText -> weekly
                    theme.rawText -> theme
                    wrong.rawText -> wrong
                    init_V2.rawText -> init_V2
                    else -> {
                        LogUtils.assert(false, "예상치 못한 케이스 ${rawString}")
                        wrong
                    }
                }
            }
        }
    }

    var description: String = ""

    var startDate: Date = Date()
    var endDate: Date = Date()

    var studentRating: Int? = null
    var testHistory: List<TestHistory> = listOf()

    var weakChapterAnalysis: ChapterAnalysis? = null
    var strongChapterAnalysis: ChapterAnalysis? = null

    var continuousDayCount: Int = 0
    var scoreLastTime: Int = -1
    var scoreBeforeLastTime: Int = -1

    var isReStudy: Boolean = false


    // V2
    var scoringTestPieceCount: Int = 0
    var canSolveTestPiece: Boolean = true

    var dailyInfo: DailyInfo = DailyInfo()
    val weeklyInfo: WeeklyInfo = WeeklyInfo()
    val wrongInfo: WrongInfo = WrongInfo()


    fun isPossibleToSolve(): Boolean {
        if(getTestType() == TestType.weekly) {
            return weeklyInfo.weeklyProblemCount >= 30
        }
        return true
    }


    fun getTestType(): TestType {
        return TestType.init(pieceSubCategory!!)
    }


    fun isPossibleTest(): Boolean {
        val possibleSet = setOf(TestType.initial, TestType.daily, TestType.weekly, TestType.wrong)
        return possibleSet.contains(getTestType())
    }

    fun getLastTimeScore(): Int? {
        return if(getTestType() == TestType.daily)
            scoreLastTime
        else if(getTestType() == TestType.weekly) {
            testHistory.getOrNull(testHistory.size - 2)?.score
        } else
            null
    }

    constructor()
    constructor(content: Content) : super(content) {
        this.pieceSubCategory = content.pieceSubCategory
    }

    override fun isCompleted(): Boolean {
        return !canSolveTestPiece
    }
}

class TestHistory:Serializable {
    val formattedDate: String = ""
    val score: Int? = null
}

class DailyInfo: Serializable {
    val testLevel: Int = -1 // nullable int, null 이면 니드셋업이 true
    val testRange: Int = -1 //nullable int, null 이면 니드셋업이 true
    val subjectCode : String = ""

    fun isNeedSetup(): Boolean {
        return testLevel == -1 || testRange == -1
    }
}

class WeeklyInfo : Serializable{
    val weekEndDate: Date = Date()
    val weekStartDate: Date = Date()
    val testRange: String = ""
    val weeklyProblemCount: Int = -1

    fun getDurationText(): String {
        return "${weekStartDate.month()}월 ${weekStartDate.day()}일" +
                " - " +
                "${weekEndDate.month()}월 ${weekEndDate.day()}일"
    }
}

class WrongInfo: Serializable {
    val headline: String = ""
    val monthAndWeek: String = ""
    val totalProblemCount: Int = -1
    val wrongProblemCount: Int = -1
    val clearedProblemCount: Int = -1

    fun isNeedMoreProblem(): Boolean {
        return totalProblemCount == 0 || wrongProblemCount - clearedProblemCount <= 0
    }

}
