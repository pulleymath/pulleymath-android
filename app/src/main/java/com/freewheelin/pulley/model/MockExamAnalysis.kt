package com.freewheelin.pulley.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.DecimalFormat

class MockExamAnalysis {

    var showAllSummary: Boolean = false
    var title: String = ""
    val myTimeStr: String = ""

    val summaryAnalysis:SummaryAnalysis? =null
    val subjectAnalysis:SubjectAnalysis? = null
    val scoreAnalysis:ScoreAnalysis? = null
    val problemAnalysis:ProblemAnalysis? = null

}

class SummaryAnalysis: Serializable {
    var correctRate: Int = 0
    var totalNumber: Int = 0
    var correctCount: Int = 0
    var score: Int = 0
    var percent: Int = 0
    var rating: Int = 0
    var higherRatingScore: Int = 0
    var sameRatingScore: Int = 0
}

class SubjectAnalysis: Serializable {
    var goodCuration:CurationTitle? = null
    var badCuration:CurationTitle? = null
    var report:MutableList<SubjectReport> = mutableListOf()
}

class ScoreAnalysis: Serializable {
    var goodCuration:CurationTitle? = null
    var badCuration:CurationTitle? = null
    var report:MutableList<ScoreReport> = mutableListOf()
}

class ProblemAnalysis: Serializable {
    var curation: CurationTitle? = null
    var problemList:List<MockExamProblem> = listOf()
}

class CurationTitle : Serializable {
    var template: String = ""
    var values: List<String> = listOf()
}

class SubjectReport : Serializable {
    var subjectName: String = ""
    var subjectCode: String = ""
    var totalCorrectRate: Int = 0
    var chapterList: List<SubjectReportChapter> = listOf()
}

class SubjectReportChapter: Serializable {
    var chapterName: String = ""
    var totalNumber: Int = 0
    var myCorrectRate: Int = 0
    var sameRatingCorrectRate: Int = 0
    var myRating: Int = 0
}

class ScoreReport: Serializable {
    var subject: String = ""
    var pointProblemList:List<ScoreProblem> = listOf()
}

class ScoreProblem: Serializable {
    var point: Int = 0
    var totalNumber: Int = 0
    var myCorrectRate: Int = 0
    var sameRatingCorrectRate: Int = 0
    var myRating: Int = 0
}

class MockExamProblem: Serializable {
    var problemNum: Int = 0
    var subject: String = ""
    var bigChapter: String = ""
    var unit: String = ""
    var point: Int = 0
    var isKiller = false
    var isCorrect = false
}

class ChapterAnalysis: Serializable {
    @Expose @SerializedName("chapterCode")
    val code: Int = 0
    @Expose @SerializedName("chapterName")
    val name: String = ""

    val myCorrectRate: Int = 0
    val myPercent: Int = 0
    val averageCorrectRateSameGrade = 0
    val averageCorrectRateHigherGrade = 0

    val problemTotalNumber: Int = 0
    val problems: List<Problem> = listOf()
    val chapters: List<ChapterAnalysis> = listOf()

    var improvement: Int? = 0


    fun getCorrectRateGuideText():String {
        val firstChapter = chapters[0]
        val format = DecimalFormat("##%")
        if(firstChapter.myRate < firstChapter.belowRate)
            return "${firstChapter.name} 대단원 정답률이 같은 등급 평균보다 ${format.format(firstChapter.belowRate - firstChapter.myRate)} 낮아요."
        else if(myRate > belowRate) {
            return "${firstChapter.name} 대단원 정답률이 같은 등급 평균보다 ${format.format(firstChapter.myRate - firstChapter.belowRate)} 높아요."
        } else {
            return "${firstChapter.name} 대단원 정답률이 동급 평균과 같아요."
        }
    }

    val myRate: Float
        get() = myCorrectRate * 0.01f
    val belowRate:Float
        get() = averageCorrectRateSameGrade * 0.01f
    val upperRate: Float
        get() = averageCorrectRateHigherGrade * 0.01f

}




