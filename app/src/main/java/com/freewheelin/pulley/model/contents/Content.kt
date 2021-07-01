package com.freewheelin.pulley.model.contents

import android.util.Log
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.utils.LogUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

enum class PieceCategory {
    mockExam,
    book,
    test,
    theme,
    recommned,

    reference,
    note;

    companion object {
        fun init(rawString: String): PieceCategory {
           return when(rawString) {
               "MO" -> mockExam
               "BOOK", "CUSTOM_BOOK" -> book
               "THEME" -> theme
               "NOTE" -> note
               "TEST" -> test
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
            test -> "테스트"
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
    @Expose @SerializedName("pieceID")
    var id: Int = 0

    var assignID: Int? = null

    var problems: List<Problem> = listOf()
    val tempSimilarProblems: ArrayList<Problem> = ArrayList()

    var score: Int = 0

    var markingState: String = ""

    open var subject: String = ""
    var subjectTag: String = ""

    var totalNumber: Int = 0
    var markedNumber: Int = 0
    var similarProblemNumber: Int = 0

    private var pieceCategory = HashSet<String>()

    private var pieceDerived: String = ""
    var pieceCategoryTag: BookType = BookType.BOOK

    // NOTE: (hyuntae) MockExam에서만 씀
    open var time: Int? = null

    // 모의고사 신규
    var publicData:PublicData? = null
    var mockID:Int = 0

    val category: PieceCategory
        get() {
            if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.mockExam))
                return PieceCategory.mockExam

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.book))
                return PieceCategory.book

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.test))
                return PieceCategory.test

            else if(!isDerivedContent() && getPieceCategory().contains(PieceCategory.reference))
                return PieceCategory.reference

            return PieceCategory.note
        }

    // NOTE: (hyuntae) Book에서만
    var bookName: String? = null

    @Expose @SerializedName("dateTime")
    var createDateTime: Date = Date()
    var solveDateTime: Date? = null
    var updateDateTime: Date? = null

    var pieceSubCategory: String = ""
    constructor()
    constructor(content: Content) {
        this.id = content.id
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
        // mockID 추가
        this.mockID = content.mockID
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
            return problems.filter { it.rawCategory == "SIMILAR" }.size
        }
}