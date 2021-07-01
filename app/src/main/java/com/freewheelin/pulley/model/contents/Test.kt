package com.freewheelin.pulley.model.contents

import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.utils.LogUtils
import java.io.Serializable
import java.util.*
import com.freewheelin.pulley.utils.*
import com.google.gson.Gson
import org.json.JSONObject
import java.text.SimpleDateFormat

class Test: Content {
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
    var testInfo: Map<String, Any> = emptyMap<String, Any>()

    val dailyInfo: DailyInfo
        get() = DailyInfo(testInfo)
    val weeklyInfo: WeeklyInfo
        get() = WeeklyInfo(testInfo)
    val wrongInfo: WrongInfo
        get() = WrongInfo(testInfo)


    fun isPossibleToSolve(): Boolean {
        if(getTestType() == TestType.weekly) {
            return weeklyInfo.weeklyProblemCount >= 30
        }
        return true
    }


    fun getTestType(): TestType {
        return TestType.init(pieceSubCategory)
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

class DailyInfo {
    val testLevel: String
    val testRange: String
    val recentSubjectCode : String
    val excludeSubjectCode : String

    constructor(values: Map<String, Any>) {
        this.testLevel = if(values["testLevel"]!=null) values["testLevel"] as String else ""
        this.testRange = if(values["testRange"]!=null) values["testRange"] as String else ""
        this.recentSubjectCode = if(values["recentSubjectCode"]!=null) values["recentSubjectCode"] as String else ""
        this.excludeSubjectCode = if(values["excludeSubjectCode"]!=null) values["excludeSubjectCode"] as String else ""
    }

    fun isNeedSetup(): Boolean {
        return testLevel.isEmpty() || testRange.isEmpty()
    }
}

class WeeklyInfo {
    val weekEndDate: Date
    val weekStartDate: Date
    val testRange: String
    val weeklyProblemCount: Int

    constructor(values: Map<String, Any>) {
        val weeklyInfo = Gson().fromJson(JSONObject(values).toString(), WeeklyInfo::class.java)
        weekEndDate = weeklyInfo.weekEndDate
        weekStartDate = weeklyInfo.weekStartDate
        testRange = weeklyInfo.testRange
        weeklyProblemCount = weeklyInfo.weeklyProblemCount
    }

    fun getDurationText(): String {
        return "${weekStartDate.month()}월 ${weekStartDate.day()}일" +
                " - " +
                "${weekEndDate.month()}월 ${weekEndDate.day()}일"
    }
}

class WrongInfo {
    val headline: String
    val monthAndWeek: String
    val totalProblemCount: Int
    val wrongProblemCount: Int
    val clearedProblemCount: Int

    constructor(values: Map<String, Any>) {
        this.headline = values["headline"] as String
        this.monthAndWeek = values["monthAndWeek"] as String
        this.totalProblemCount = (values["totalProblemCount"] as Double).toInt()
        this.wrongProblemCount = (values["wrongProblemCount"] as Double).toInt()
        this.clearedProblemCount = (values["clearedProblemCount"] as Double).toInt()
    }

    fun isNeedMoreProblem(): Boolean {
        return totalProblemCount == 0 || wrongProblemCount - clearedProblemCount <= 0
    }

}
