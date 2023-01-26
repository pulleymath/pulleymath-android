package com.freewheelin.pulley.model

import android.content.Context
import android.util.Log
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.model.contents.PieceCategory
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.NumberUtils
import com.freewheelin.pulley.utils.responseFailed
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
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
    temp(-999)
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
    var problemNum: Int? = null
    var answerData: String = ""
    var correctTimes: Int = 312000203
    var totalTimes: Int = 0

    @Expose @SerializedName("result")
    var rawResult: Int? = null
    var deprecatedResult: Result? = null
    var userAnswer: String? = null
    var problemURL: String = ""
    var problemLevel: Int = 0
    var problemPoint: Int = 0

    @Expose @SerializedName("category")
    var rawCategory: String = ""

    @Expose @SerializedName("problemType")
    private var rawProblemType: String = ""
    val problemType: ProblemType
        get() = ProblemType.init(rawProblemType)

    @Expose @SerializedName("pieceCategory")
    val rawPieceCategory: Set<String> = HashSet()

    val unit: String = ""

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
    var updateDateTime: Date? = null
    var scrapDateTime: Date? = null
    var clearDateTime: Date? = null
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

    fun isTodaySolved(): Boolean {
        if(updateDateTime == null)
            return false
        else
            return DateTimeUtils.isSameDate(updateDateTime!!, Date())
    }

    fun isSimilarProblem(): Boolean {
        return rawCategory == "SIMILAR"
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

    fun getSimilarProblem(context: Context, user: User, content: Content, cb: (problem: Problem?) -> Unit) {

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
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Problem>>, response: Response<Template<Problem>>) {
                if(response.isSuccessful) {
                    val problem = response.body()?.data
                    problem?.rawCategory = "SIMILAR"
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
        val results = arrayOf(0, 1, -2)

        fun dummy(isSolved: Boolean? = false): Problem {
            val problem = Problem()
            problem.problemLevel = NumberUtils.rand(1,6)

            if (isSolved == true)
                problem.rawResult = results[NumberUtils.rand(1,3)]
            else if (isSolved == false)
                problem.rawResult = results[NumberUtils.rand(0,1)]
            else
                problem.rawResult = results[NumberUtils.rand(0,3)]

            problem.problemURL = ProblemDummyDB.dummyProblemUrls[NumberUtils.rand(0, ProblemDummyDB.dummyProblemUrls.size)]

            problem.isClear = when(NumberUtils.rand(0,5)) {
                0 -> true
                else -> false
            }
            problem.isScrap = when(NumberUtils.rand(0,5)) {
                0 -> true
                else -> false
            }

            when(NumberUtils.rand(0,2)) {
                0 -> {
                    problem.rawProblemType = "객관식"
                    problem.answerData = NumberUtils.rand(1,6).toString()
                }
                1 -> {
                    problem.rawProblemType = "주관식"
                    problem.answerData = NumberUtils.rand(0,130).toString()
                }
            }

            if(problem.getResultByScoring() == Result.correct)
                problem.userAnswer = problem.answerData
            else if(problem.getResultByScoring() == Result.incorrect) {
                if(problem.rawProblemType == "객관식") {
                    problem.userAnswer = "4"
                } else {
                    problem.userAnswer = NumberUtils.rand(130,200).toString()
                }
            }



            when(NumberUtils.rand(1,100)) {
                in 0..9 -> problem.problemErrorStatus = ProblemErrorStatus.ERROR
                in 10..19 -> problem.problemErrorStatus = ProblemErrorStatus.REPORT
                else -> problem.problemErrorStatus = ProblemErrorStatus.NONE
            }
            return problem
        }

        fun dummies(cnt: Int, isSolved: Boolean? = false): ArrayList<Problem> {
            var problems = ArrayList<Problem>()
            var rand = NumberUtils.rand(1, 200)
            for (i in 0 until cnt) {
                val problem = dummy(isSolved)
                problem.problemNum = rand + i
                problems.add(problem)
            }

            return problems
        }

        fun arrangeProblem(problems:List<Problem>) : List<Problem>{
            val problemIdDictionary = HashMap<Int, Problem>()
            for (problem in problems) {
                if(problem.isSimilarProblem()) {
                    val rootProblem = problemIdDictionary[problem.parentProblemID]
                    problem.rootProblem = rootProblem
                    problem.page = rootProblem?.page
                    problem.updateDateTime = rootProblem?.updateDateTime
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

    /***
     * NOTE: (hyuntae) 아래건 쓰지말것 백단 연결되기 전까지
     */
    fun getResultByInput(): SolveLog.Result {
        return if(userAnswer == null || userAnswer.toString() != answerData)
            SolveLog.Result.incorrect
        else SolveLog.Result.correct
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
