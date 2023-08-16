package com.freewheelin.pulley.legacy.model.contents

import android.util.Log
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.StudyCategoryEnum
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

enum class PieceCategory {
    mockExam,
    book,
    workbook,
    dailyTest,
    recommned, // 풀리에서 만든 추천학습지

    note,
    reference; // 오답노트 리뷰

    companion object {
        fun init(rawString: String): PieceCategory {
           return when(rawString) {
               "MO" -> mockExam
               "BOOK" -> book
               "CUSTOM_BOOK" -> workbook
               "NOTE" -> note
               "TEST" -> dailyTest
               "RECOMMEND" -> recommned
               "REFERENCE" -> reference
               else -> {
                   LogUtils.assert(false, "unexpected Case in PieceCategory rawString: ${rawString}")
                   mockExam
                }
            }
        }
    }

    fun getContentCategoryTitle(): String {
        return when(this) {
            mockExam -> "모의고사"
            book -> "유형학습"
            workbook -> "워크북"
            dailyTest -> "테스트"
            note -> "오답학습"
            recommned -> "추천학습"
            else -> "오답학습"
        }
    }
}
enum class MarkingState{
    YET, ING, COMPLETED, NONE
}
open class Content: Serializable {
//    @Expose @SerializedName("pieceID")
//    var id: Int = 0
    var pieceID: Int = 0

//    @PrimaryKey(autoGenerate = false) var assignID: Int? = null
    var assignID: Int? = null

    var problems: List<Problem> = listOf()
    @Ignore
    val tempSimilarProblems: ArrayList<Problem> = ArrayList()

    var score: Int = 0

    var markingState: String = ""

    open var subject: String = ""
    var subjectTag: String = ""
    var title: String = ""

    var totalNumber: Int = 0
    var markedNumber: Int = 0
    var similarProblemNumber: Int = 0

    @Ignore
    private var pieceCategory = HashSet<String>()

    @Ignore
    private var pieceDerived: String = ""
    var pieceCategoryTag: BookType = BookType.BOOK

    // NOTE: (hyuntae) MockExam에서만 씀
    open var time: Int? = null

    // 모의고사 신규
    var publicData:PublicData? = null
    var mockID:Int = 0

    // Book 과 Mock에서 사용함
    var chapter: String = ""

    var isLocked: Boolean = true
    var schoolType: SchoolType? = null

    val category: PieceCategory
        get() {
            if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.mockExam))
                return PieceCategory.mockExam

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.book))
                return PieceCategory.book

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.dailyTest))
                return PieceCategory.dailyTest

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.reference))
                return PieceCategory.reference

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.workbook))
                return PieceCategory.workbook

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.recommned))
                return PieceCategory.recommned

            return PieceCategory.note
        }

    // NOTE: (hyuntae) Book에서만
    var bookName: String? = null

    val itemBookPlanBookName: String
        get() {
            return bookName ?: subject
        }
    val itemBookPlanSubject: String
        get() {
            return if (bookName == null) ""
            else subject
        }

    @Expose @SerializedName("dateTime")
    var createDateTime: Date = Date()
    var updateDateTime: Date? = null
    var solveDateTime: Date? = null

    val updateDateTimeOnMMdd: String
        get() {
            return updateDateTime?.let { DateTimeUtils.mMDashddFormat.format(it) } ?: ""
        }
    val solveDateTimeOnMMdd: String
        get() {
            return solveDateTime?.let { DateTimeUtils.mMDashddFormat.format(it) } ?: ""
        }
    val isShowSolveDateTime: Boolean
        get() {
            return solveDateTime != null
        }

    var pieceSubCategory: String? = ""
    fun isStartChallengePiece(): Boolean {
        return pieceSubCategory == "START"
    }
    fun isStartChallengeRewardPiece(): Boolean {
        return pieceSubCategory == "START_REWARD"
    }
    fun isStartChallengeBookPiece(): Boolean {
        return pieceSubCategory == "START" && pieceCategoryTag == BookType.BOOK
    }
    fun isStartChallengeRewardBookPiece(): Boolean {
        return pieceSubCategory == "START_REWARD" && pieceCategoryTag == BookType.BOOK
    }
    constructor()
    constructor(content: Content) {
        this.pieceID = content.pieceID
        this.assignID = content.assignID
        this.score = content.score
        this.markingState = content.markingState
        this.subject = content.subject
        this.subjectTag = content.subjectTag
        this.totalNumber = content.totalNumber
        this.markedNumber = content.markedNumber
        this.similarProblemNumber = content.similarProblemNumber
        this.bookName = content.bookName
        this.pieceCategoryTag = content.pieceCategoryTag

        this.mockID = content.mockID
        this.pieceSubCategory = content.pieceSubCategory
        this.isLocked = content.isLocked
        this.updateDateTime = content.updateDateTime
        this.chapter = content.chapter
    }

    fun addSimilarProblem(problem: Problem) {
        tempSimilarProblems.add(problem)
        val mutableProblems = problems.toMutableList()

        val rootIndex = mutableProblems.indexOf(problem.rootProblem)
        val index = problem.rootProblem!!.similarProblems.indexOf(problem)
        mutableProblems.add(rootIndex + index + 1, problem)
        this.problems = mutableProblems
    }

    fun changeSimilarProblem(origin: Problem, target: Problem) {
        tempSimilarProblems.add(target)
        val mutableList = problems.toMutableList()

        val index = mutableList.indexOf(origin)
        mutableList.set(index, target)
        this.problems = mutableList
    }

    fun arrangeProblem() {
        Problem.arrangeProblem(problems)
    }

    fun getCurrentUnanswerdSimilarProblems() : List<Problem> {
        val list = mutableListOf<Problem>()
        for(problem in problems) {
            if(problem.isSimilarProblem() && problem.getResultByScoring() == Result.yet) {
                list.add(problem)
            }
        }
        return list
    }

    open fun isCompleted(): Boolean {
        return markingState == "COMPLETED"
    }

    fun getPieceCategory(): Set<PieceCategory> {
        return this.pieceCategory.map { PieceCategory.init(it) }.toSet()
    }

    fun isDerivedContent(): Boolean {
        return pieceDerived == "DERIVED"
    }

    fun getMakringState(): MarkingState {

        return when(publicData?.status) {
            "COMPLETED" -> MarkingState.COMPLETED
            "YET" -> MarkingState.YET
            "ING" -> MarkingState.ING
            "NONE" -> MarkingState.NONE
            else -> {
                Log.e("Content", "getMakringState() 예상치 못한 assert case ${publicData?.status}")
                MarkingState.NONE
            }
        }

//        return when(markingState) {
//            "COMPLETED" -> MarkingState.COMPLETED
//            "YET" -> MarkingState.YET
//            "ING" -> MarkingState.ING
//            "NONE" -> MarkingState.NONE
//            else -> {
//                LogUtils.assert(false, "예상치 못한 assert case $markingState")
//                MarkingState.NONE
//            }
//        }
    }

    var totalCount:Int = 0
        get() {
            return problems.size
        }

    var originCount:Int = 0
        get() {
            return totalCount - similarCount
        }

    var similarCount:Int = 0
        get() {
            return problems.filter { it.rawCategory == StudyCategoryEnum.SIMILAR }.size
        }
}