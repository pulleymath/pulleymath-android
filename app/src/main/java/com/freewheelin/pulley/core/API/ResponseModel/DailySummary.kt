package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.model.contents.Content
import java.util.*

class DailyStudy {
    val todaySummary = DaySummary()
    val yesterdaySummary = DaySummary()
    val weekStudyData = listOf<WeekStudyData>()
    val mainCuration: String? = ""

    val todayProblemCount: Int
        get() = todaySummary.studyDataCount

    val todayProblemCountDiff: Int
        get() = todaySummary.studyDataCount - yesterdaySummary.studyDataCount

    val todayPercentage: Int
        get() = todaySummary.studyDataPercent

    val todayPercentageDiff: Int
        get() = todaySummary.studyDataPercent - yesterdaySummary.studyDataPercent

    val onlyStudyTime: Int
        get() = todaySummary.studyTime?.onlyStudyTime ?: 0

    val totalStudyTime: Int
        get() = todaySummary.studyTime?.totalStudyTime ?: 0
}

class DailyRecommend {
    val weakChapter = WeakChapterResult()
    val compareNormalAndNote = NormalNoteRatio()
    val curation = Curation()
}

/*
deprecated
* */
class DailySummary {
    val todaySummary = DaySummary()
    val yesterdaySummary = DaySummary()
    val weakChapter = WeakChapterResult()
    val compareNormalAndNote = NormalNoteRatio()
    val userStudyPieceList: List<Content> = emptyList()
    val weekStudyData = listOf<WeekStudyData>()

    val curation = Curation()

    val mainCuration: String = ""

    val todayProblemCount: Int
    get() = todaySummary.studyDataCount

    val todayProblemCountDiff: Int
    get() = todaySummary.studyDataCount - yesterdaySummary.studyDataCount

    val todayPercentage: Int
    get() = todaySummary.studyDataPercent

    val todayPercentageDiff: Int
    get() = todaySummary.studyDataPercent - yesterdaySummary.studyDataPercent

    val onlyStudyTime: Int
        get() = todaySummary.studyTime?.onlyStudyTime ?: 0

    val totalStudyTime: Int
        get() = todaySummary.studyTime?.totalStudyTime ?: 0

    val isNeedToStudyUI: Boolean
        get() = todayProblemCount <= 0
}


class DaySummary {
    val studyDataCount: Int = 0
    val studyDataPercent: Int = 0
    val studyTime: StudyTime? = null
}


class WeakChapterResult {
    val studentPercent: Int = 0
    val sameGradePercent: Int = 0
    val smallChapterName: String = ""
    val bigChapterName: String = ""
    val studentRating: String = ""
}

class NormalNoteRatio {
    var normalRatio: Int = 0
    var noteRatio: Int = 0
}

class PieceList {
    var date = Date()
    var pieceCategoryTag: String = ""
    var pieceSubCategory = ""
    var subject: String = ""
    var chapter = ""
    var markedNumber: Int = 0
    var similarProblemNumber: Int = 0
    var score: Int = 0
    var markingState: String = ""

    fun isCompleted(): Boolean {
        return markingState == "COMPLETED"
    }
}
class Curation {
    val guide: Map<String, String> = hashMapOf()
    val ratio: Map<String, String> = hashMapOf()
}


class StudyTime {
    val onlyStudyTime: Int = 0
    val totalStudyTime: Int = 0
}

class WeekStudyData {
    val dayOfWeek:Int = 0
    val now:String = "0000-00-00"
    val solvedCount:Int = 0
}