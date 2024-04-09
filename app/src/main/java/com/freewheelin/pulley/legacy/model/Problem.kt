package com.freewheelin.pulley.legacy.model

import android.content.Context
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.PieceCategory
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2023.model.StudyCategoryEnum
import com.freewheelin.pulley.revision2023.model.response.NoteReviewProblem
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.joda.time.LocalDateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*
import kotlin.collections.HashSet

enum class ProblemType {
    single,
    multi,
    short;

    companion object {
        fun init(rawString: String): ProblemType {
            return when(rawString) {
                "객관식" -> single
                "선다형" -> multi
                "주관식" -> short
                else -> {
                    LogUtils.assert(false, "Unexpected rawProblemType : ${rawString}")
                    short
                }
            }
        }
    }
}

enum class Result(val rawValue: Int) {
    correct(1),
    incorrect(-2),
    yet(0),
    temp(-999);

    val isYet: Boolean
        get() {
            return this == yet
        }

}

enum class ProblemErrorStatus {
    NONE,
    REPORT,
    ERROR
}

open class Problem: Serializable {

    @Expose @SerializedName("problemID")
    var id: Int = 0

    var studyID: Int = 0
    var unitCode: Int = 0
    var subjectCode: Int = 0
    var subject: String = ""
    var problemNum: Int? = null
    var answerData: String = ""
    var correctTimes: Int = 312000203
    var totalTimes: Int = 0

    var unitPrefix: String? = null
    var unitSuffix: String? = null

    @Expose @SerializedName("result")
    var rawResult: Int? = null
    var deprecatedResult: Result? = null
    var userAnswer: String? = null
    var problemURL: String = ""
    var problemLevel: Int = 0
    var problemPoint: Int = 0

    // category enum 으로 바꿔야할거같다. SIMILAR, REFERENCE,
    @Expose @SerializedName("category")
    var rawCategory: StudyCategoryEnum = StudyCategoryEnum.ORIGIN

    @Expose @SerializedName("problemType")
    private var rawProblemType: String = ""
    val problemType: ProblemType
        get() = ProblemType.init(rawProblemType)

    @Expose @SerializedName("pieceCategory")
    val rawPieceCategory: Set<String> = HashSet()

    var unit: String = ""

    val correctRate: Float?
        get() {
            return if(totalTimes == 0)
                null
            else
                correctTimes.toFloat() / totalTimes.toFloat()
        }

    var parentProblemID: Int? = null
    var rootProblem: Problem? = null
    var similarProblems: ArrayList<Problem> = ArrayList()

    var solveDateTime: String? = null
    @Expose @SerializedName("updateDateTime")
    var rawUpdateDateTime: String? = null
    val updateDateTime: Date?
        get() {
            return if (rawUpdateDateTime == null) null
            else LocalDateTime.parse(rawUpdateDateTime).toDate()
        }
    val updateDateTime_yyyyMMdd: String
        get() {
            val targetDate = updateDateTime ?: Date()
            return DateTimeUtils.yyyyMMddFormat.format(targetDate)
        }

    @Expose @SerializedName("scrapDateTime")
    var rawScrapDateTime: String? = null
    val scrapDateTime: Date?
        get() {
            return if (rawScrapDateTime == null) null
            else LocalDateTime.parse(rawScrapDateTime).toDate()
        }
    val scrapDateTime_yyyyMMdd: String
        get() {
            val targetDate = scrapDateTime ?: Date()
            return DateTimeUtils.yyyyMMddFormat.format(targetDate)
        }

    @Expose @SerializedName("clearDateTime")
    var rawClearDateTime: String? = null
    val clearDateTime: Date?
        get() {
            return if (rawClearDateTime == null) null
            else LocalDateTime.parse(rawClearDateTime).toDate()
        }

    var page: Int? = null

    @Expose @SerializedName("clear")
    var isClear: Boolean = false
    @Expose @SerializedName("scrap")
    var isScrap: Boolean = false

    var detailInfo: ProblemDetailInfo? = null

    var standardCorrectRate: Int? = null
    var tagTop: String? = null
    val tag: List<String>
        get() {
            return if(tagTop == null)
                listOf()
            else {
                tagTop!!.split(",")
            }
        }

    val wrongCount: Int? = null
    var problemErrorStatus: ProblemErrorStatus = ProblemErrorStatus.NONE

    fun getResultByUserAnswer(): Result {
        if(userAnswer == answerData)
            return Result.correct
        else
            if(isUserAnswerInput())
                return Result.incorrect
            else
                return Result.yet
    }

    fun getResultByScoring(): Result {
        if(rawResult == 1)
            return Result.correct
        else if(rawResult == -2)
            return Result.incorrect
        else
            return Result.yet
    }

    fun isUserAnswerInput(): Boolean {
        return userAnswer != null && userAnswer!!.isNotEmpty()
    }
    fun isScoring(): Boolean {
        return getResultByScoring() == Result.correct || getResultByScoring() == Result.incorrect
    }

    fun isTodaySolved(): Boolean {
        if(updateDateTime == null)
            return false
        else
            return DateTimeUtils.isSameDate(updateDateTime!!, Date())
    }

    fun isSimilarProblem(): Boolean {
        return rawCategory == StudyCategoryEnum.SIMILAR
    }

    fun isFamily(problem: Problem): Boolean {
        if(rootProblem == null)
            return similarProblems.indexOf(problem) != -1
        else
            return rootProblem == problem.rootProblem
    }

    fun getNumberText(): String {
        val rootProblem = rootProblem
        return if(rootProblem == null)
            "${problemNum}"
        else {
            val index = rootProblem.similarProblems.indexOf(this)
            "${problemNum}+${index + 1}"
        }
    }

    fun getCurNumberText(): String {
        return "${problemNum}"
    }

    fun getExtNumberText(): String {
        val rootProblem = rootProblem
        return if(rootProblem == null)
            ""
        else {
            val index = rootProblem.similarProblems.indexOf(this)
            "+${index + 1}"
        }
    }

    fun getPieceCategory(): Set<PieceCategory> {
        return this.rawPieceCategory.map { PieceCategory.init(it) }.toSet()
    }

    fun getSimilarProblem(context: Context, user: UserV4, content: Content, cb: (problem: Problem?) -> Unit, deniedCb: () -> Unit) {

        val exceptionSimilarProblems = content.tempSimilarProblems
        val assignID = content.assignID!!
        val currentUnansweredSimilarProblems = content.getCurrentUnanswerdSimilarProblems()

        val param: Parameter = Parameter(
                "exceptionProblemIDs" to  exceptionSimilarProblems.map { it.id },
                "currentUnansweredSimilarProblems" to currentUnansweredSimilarProblems.map {it.id},
                "problemLevel" to problemLevel,
                "unitCode" to unitCode,
                "studentID" to user.studentID,
                "assignID" to assignID,
                "problemID" to id
        )

        API_V2.getSimilarProblem(param).enqueue(object : Callback<Template<Problem>> {
            override fun onFailure(call: Call<Template<Problem>>, t: Throwable) {
                deniedCb()
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Problem>>, response: Response<Template<Problem>>) {
                if(response.isSuccessful) {
                    val problem = response.body()?.data
                    problem?.rawCategory = StudyCategoryEnum.SIMILAR
                    cb(problem)
                }
            }
        })
    }

    fun getSubject(): SubjectV3 {
        return SubjectV3.codeToSubject(subjectCode)
    }

    fun isAllAnswered() : Boolean {
        for(similar in similarProblems) {
            if (similar.getResultByScoring() == Result.yet) {
                return false
            }
        }

        return true
    }

    companion object {

        fun convertFromNoteReviewProblem(nrProblem: NoteReviewProblem): Problem {
            return Problem().apply {
                studyID = nrProblem.studyID
                unitCode = nrProblem.unitCode
                answerData = nrProblem.answerData ?: ""
                userAnswer = nrProblem.userAnswer
                id = nrProblem.problemID
                problemLevel = nrProblem.problemLevel
                rawProblemType = nrProblem.problemType
                problemURL = nrProblem.problemURL ?: ""
                unit = nrProblem.unit ?: ""
                problemNum = nrProblem.problemNum
                rawCategory = nrProblem.category
                totalTimes = nrProblem.totalTimes
                correctTimes = nrProblem.correctTimes
                rawResult = nrProblem.result
                isClear = nrProblem.clear
                isScrap = nrProblem.scrap
                rawUpdateDateTime = nrProblem.updateDateTime
                problemErrorStatus = nrProblem.problemErrorStatus
            }
        }
        fun arrangeProblem(problems:List<Problem>) : List<Problem>{
            val problemIdDictionary = HashMap<Int, Problem>()
            for (problem in problems) {
                if(problem.isSimilarProblem()) {
                    val rootProblem = problemIdDictionary[problem.parentProblemID]
                    problem.rootProblem = rootProblem
                    problem.page = rootProblem?.page
                    problem.rawUpdateDateTime = rootProblem?.rawUpdateDateTime
                    if(rootProblem?.similarProblems != null)
                        rootProblem.similarProblems.add(problem)
                    else
                        rootProblem?.similarProblems = ArrayList()

                } else {
                    problemIdDictionary[problem.id] = problem
                }
            }
            return problems
        }
    }

    fun mark() {
        // 멀티선택 아닐경우
        try {
            if (userAnswer?.contains(",") == false)
                userAnswer = userAnswer?.let { it.toInt().toString() } // 앞에 0 있을 때 처리만

            rawResult = if (userAnswer == answerData)
                Result.correct.rawValue
            else
                Result.incorrect.rawValue
        } catch (e:Exception) {
            /* 오타처리 */
        }
    }


    fun getProblemLevel(): String {
        return when (problemLevel) {
            1 -> "하"
            2 -> "중하"
            3 -> "중"
            4 -> "상"
            5 -> "최상"
            else -> "중"
        }
    }

//    var s3: String = "https://s3.ap-northeast-2.amazonaws.com/mathflat"

    fun getThumbnailUrl(): String {
        return problemURL + "problem.png"
    }

    fun getProblemUrl(): String {
        return problemURL + "problem.png"
    }

    fun getSolutionUrl(): String {
        return problemURL + "solution.png"
    }
}



class ProblemDetailInfo {
    var chapterBig: String = ""
    var chapterMiddle: String = ""
    var chapterLittle: String = ""
    var unitName: String = ""
    var curriculumNumber: Int = 0

    var history: List<History> = listOf()
}

class History {
    val solveDateTime: Date = Date()
    val result: Int = 0
    val subjectTag: String = ""
}
class ProblemDummyDB {
    companion object {
        var todaySolvedProblem = HashSet<Problem>()
        var dummyProblemUrls = arrayOf(
                "/math_problems/Mo/MO_201903/h2/201903_Se_B/1_",
                "/math_problems/Mo/MO_201903/h2/201903_Se_B/4_",
                "/math_problems/Mo/MO_201903/h2/201903_Se_B/17_",
                "/math_problems/Mo/MO_201903/h2/201903_Se_B/2_",
                "/math_problems/Mo/MO_201903/h2/201903_Se_B/28_"
        )
    }
}
