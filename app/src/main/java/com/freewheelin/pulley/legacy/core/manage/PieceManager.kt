package com.freewheelin.pulley.legacy.core.manage

import android.content.Context
import android.util.Log
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.dialogs.WrongManagementDialog
import com.freewheelin.pulley.legacy.model.ChapterAnalysis
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object PieceManager {
    const val ARG_PIECE_PROBLEMS = "ARG_PIECE_PROBLEMS"
    const val ARG_PIECE_SUBJECT = "ARG_PIECE_SUBJECT"
    const val ARG_PIECE = "ARG_PIECE"
    const val ARG_REVIEW_SYNC = "ARG_REVIEW_SYNC"
    // 탭 이동
    const val EVENT_MOVE_TAB = "EVENT_MOVE_TAB"
    const val EVENT_MOVE_TAB_INDEX = "EVENT_MOVE_TAB_INDEX"

    // 탭 스크롤
    const val EVENT_SCROLL = "EVENT_SCROLL"
    // 스크롤 타겟
    const val EVENT_SCROLL_UNIT_TOTAL_LABEL = "EVENT_SCROLL_UNIT_TOTAL_LABEL"
    // 스크롤 후 필터 설정
    const val EVENT_FILTER = "EVENT_FILTER"

    fun getReviewInfo(context: Context, subject: String, problems: List<Problem>, user: User, cb:(piece: Piece) -> Unit) {
        val params: Parameter = Parameter(
                "studentID" to user.studentID,
                "studyIDs" to problems.map { it.studyID },
                "subject" to subject
        )

        API_V1.getProblemReview(params).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                val piece = response.body()?.data
                if(response.isSuccessful && piece != null) {
                    cb(piece)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun score(context: Context, user: User, piece: Piece, cb: (() -> Unit)) {

        val params: Parameter = Parameter(
                "studentID" to user.studentID,
                "assignID" to piece.assignID!!,
                "needAssign" to false
        )

        params["studyData"] = piece.problems.map {
            val problemParam: Parameter = Parameter(
                "category" to it.rawCategory,
                "problemID" to it.id,
                "unitCode" to it.unitCode,
                "problemNum" to it.problemNum!!,
                "result" to it.getResultByUserAnswer().rawValue
            )

            if(it.userAnswer != null)
                problemParam["userAnswer"] = it.userAnswer!!

            if(it.studyID != null)
                problemParam["studyID"] = it.studyID

            if(it.rootProblem != null)
                problemParam["parentProblemID"] = it.rootProblem!!.id

            problemParam
        }

        API_V1.scoring(params).enqueue(object :Callback<Template<MockExam>>{
            override fun onFailure(call: Call<Template<MockExam>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<MockExam>>, response: Response<Template<MockExam>>) {
                if(response.isSuccessful) {
                    ContentManager.isNeedToSyncMyContentList = true
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun makeWeakPiece(context: Context, user: User, problems: List<Problem>,
                       isSimilar: Boolean, difficulty: WrongManagementDialog.Level? = null,
                       requestProblemNumber: Int? = null, isIncludeClearProblem: Boolean? = null,
                       noteType: String,
                       successCB: ((piece: Piece) -> Unit), failCB: () -> Unit) {

        val param: Parameter = Parameter(
                "sameOrSimilar" to if(isSimilar) "SIMILAR" else "SAME",
                "noteType" to noteType
        )

        if(isSimilar) {
            param["requestProblemNumber"] = requestProblemNumber!!
            param["difficulty"] = difficulty!!.text
            param["includeClearProblem"] = isIncludeClearProblem!!
        }

        val pieceParam: Parameter = Parameter(
                "studentID" to user.studentID,
                "maker" to user.fullName,
                "pieceCategory" to listOf("NOTE"),
                "pieceDerived" to "DERIVED"
        )


        val problemParam = problems.map {
            Parameter(
                    "problemLevel" to it.problemLevel,
                    "problemID" to it.id,
                    "unitCode" to it.unitCode,
                    "unit" to it.unit,
                    "correctTimes" to it.correctTimes,
                    "totalTimes" to it.totalTimes,
                    "problemURL" to it.problemURL
            )
        }

        pieceParam["problems"] = problemParam
        param["piece"] = pieceParam

        API_V1.getWeakPieceWithProblems(param).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                val piece = response.body()?.data
                if (response.isSuccessful && piece != null) {
                    ContentManager.isNeedToSyncMyContentList = true
                    successCB(piece)
                } else {
                    failCB()
                    responseError(context, response)
                }
            }
        })
    }

    fun spyMakePiece(context: Context, user: User, ids: List<String>,
                     successCB: ((piece: Piece) -> Unit), failCB: () -> Unit) {
        val param: Parameter = Parameter(
                "sameOrSimilar" to "SAME",
                "noteType" to "WRONG_NOTE"
        )

        val pieceParam: Parameter = Parameter(
                "studentID" to user.studentID,
                "maker" to user.fullName,
                "pieceCategory" to listOf("NOTE"),
                "pieceDerived" to "DERIVED"
        )

        val problemParam = ids.map {
            Parameter(
                    "problemID" to it,
                    "unitCode" to 33201111,
                    "unit" to "spy",
                    "correctTimes" to 999,
                    "totalTimes" to 999
            )
        }

        pieceParam["problems"] = problemParam
        param["piece"] = pieceParam

        API_V1.getWeakPieceWithProblems(param).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                val piece = response.body()?.data
                if (response.isSuccessful && piece != null) {
                    ContentManager.isNeedToSyncMyContentList = true
                    successCB(piece)
                } else {
                    failCB()
                    responseError(context, response)
                }
            }
        })
    }
    fun makeWeakPieceUsingPiece(context: Context, user: User, content: List<Content>,
                      isSimilar: Boolean, difficulty: WrongManagementDialog.Level? = null,
                      requestProblemNumber: Int? = null, isIncludeClearProblem: Boolean? = null, successCB: ((piece: Piece) -> Unit), failCB: () -> Unit) {
        val param: Parameter = Parameter(
                "sameOrSimilar" to if(isSimilar) "SIMILAR" else "SAME",
                "assignIDs" to content.map { it.assignID!! }
        )

        if(isSimilar) {
            param["requestProblemNumber"] = requestProblemNumber!!
            param["difficulty"] = difficulty!!.text
            param["includeClearProblem"] = isIncludeClearProblem!!
        }

        val pieceParam: Parameter = Parameter(
                "studentID" to user.studentID,
                "maker" to user.fullName,
                "pieceCategory" to listOf("NOTE"),
                "pieceDerived" to "DERIVED"
        )
        param["piece"] = pieceParam

        API_V1.getWeakPieceWithPieces(param).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                val piece = response.body()?.data
                if (response.isSuccessful && piece != null) {
                    ContentManager.isNeedToSyncMyContentList = true
                    successCB(piece)
                } else {
                    failCB()
                    responseError(context, response)
                }
            }
        })
    }

    fun makeWeakPieceUsingChapters(context: Context, user: User, chapters: List<ChapterAnalysis>,
                                   isSimilar: Boolean, difficulty: WrongManagementDialog.Level? = null,
                                   requestProblemNumber: Int? = null, isIncludeClearProblem: Boolean? = null,
                                   startDate: Date,
                                   endDate: Date,
                                   successCB: (piece: Piece) -> Unit,
                                   failCB: () -> Unit) {
        val param: Parameter = Parameter(
                "sameOrSimilar" to if(isSimilar) "SIMILAR" else "SAME",
                "chapterLittles" to chapters.map { it.code },
                "startDate" to DateTimeUtils.yyyy_MM_dd.format(startDate),
                "endDate" to DateTimeUtils.yyyy_MM_dd.format(endDate)
        )

        if(isSimilar) {
            param["requestProblemNumber"] = requestProblemNumber!!
            param["difficulty"] = difficulty!!.text
            param["includeClearProblem"] = isIncludeClearProblem!!
        }

        val pieceParam: Parameter = Parameter(
                "studentID" to user.studentID,
                "maker" to user.fullName,
                "pieceCategory" to listOf("NOTE"),
                "pieceDerived" to "DERIVED"
        )
        param["piece"] = pieceParam

        API_V1.getWeakPieceWithChapters(param).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                val piece = response.body()?.data
                if (response.isSuccessful && piece != null) {
                    ContentManager.isNeedToSyncMyContentList = true
                    successCB(piece)
                } else {
                    failCB()
                    responseError(context, response)
                }
            }

        })

    }

    fun getPieceReviewProblems(context: Context, content: Content, user: User, successCB: (piece: Piece) -> Unit) {
        val params: Parameter = Parameter(
                "assignID" to content.assignID!!,
                "studentID" to user.studentID
        )

        Log.d("유사문제","api param=${params}")

        API_V1.getPieceReviewInfo(params).enqueue(object: Callback<Template<Piece>> {
            override fun onFailure(call: Call<Template<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Piece>>, response: Response<Template<Piece>>) {
                if(response.isSuccessful) {
                    val responseContent = response.body()?.data!!
                    responseContent.arrangeProblem()
                    successCB(responseContent)
                } else
                    responseError(context, response)
            }
        })
    }

    fun getProblems(context: Context, piece: Piece, user: User, successCB: (problems: List<Problem>) -> Unit) {
        val params: Parameter = Parameter(
                "studentID" to user.studentID,
                "assignID" to piece.assignID!!
        )


        API_V1.getPieceProblemsRenew(params).enqueue(object: Callback<Template<List<Problem>>> {
            override fun onFailure(call: Call<Template<List<Problem>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<List<Problem>>>, response: Response<Template<List<Problem>>>) {
                if(response.isSuccessful) {
                    val problems = response.body()!!.data
                    successCB(Problem.arrangeProblem((problems)))
                } else {
                    responseError(context, response)
                }
            }
        })
    }
}