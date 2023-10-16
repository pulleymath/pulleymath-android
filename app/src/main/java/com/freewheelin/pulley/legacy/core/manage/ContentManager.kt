package com.freewheelin.pulley.legacy.core.manage

import android.content.Context
import android.util.Log
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.legacy.core.API.ResponseModel.StudentGoalInfo
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.TestManager.COUNT_MAXIMUM_DAILY_TEST
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.*
import com.freewheelin.pulley.legacy.utils.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*


object ContentManager {
    const val ARG_CONTENT = "ARG_CONTENT"

    var isNeedToSyncMyContentList = true

    fun clearAllScroing(context: Context, user: UserV4, cb:() -> Unit) {

    }

    fun sendEmail(context: Context, content: List<Content>, user: UserV4, email: String, cb:() -> Unit) {
        val param = Parameter(
                "studentID" to user.studentID,
                "receiverEmail" to email,
                "pieceIDs" to content.map {
                    if(it.pieceCategoryTag == BookType.CUSTOM_BOOK || it.pieceID == 0) // 워크북이거나, id가 0이면
                        it.assignID!!
                    else
                        it.pieceID
                }
        )

        API_V1.sendPieceEmail(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                cb()
            }
        })
    }

    fun getMyContentList(context: Context, user: UserV4, successCB:(contents: List<Content>) -> Unit, failCB: () -> Unit) {
        isNeedToSyncMyContentList = false

        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "pieceCategory" to "MY"
        )

        API_V1.getMyContents(param).enqueue(object: Callback<Template<List<Content>>> {
            override fun onFailure(call: Call<Template<List<Content>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<List<Content>>>, response: Response<Template<List<Content>>>) {
                val contents = response.body()?.data

                if(response.isSuccessful && contents != null) {
                    successCB(contents)
                } else {
                    failCB()
                    responseError(context, response)
                }
            }

        })
    }

    fun getReview(context: Context, user: UserV4, chapters: List<ChapterAnalysis>, startDate: Date, endDate: Date, successCB: (piece: Piece) -> Unit) {
        val param: Parameter = Parameter(
                "chapterLittles" to chapters.map { it.code },
                "studentID" to user.studentID,
                "subject" to "오답노트 리뷰",
                "startDate" to DateTimeUtils.yyyy_MM_dd.format(startDate),
                "endDate" to DateTimeUtils.yyyy_MM_dd.format(endDate)
        )

        API_V1.getReviewFromChapterV1(param).enqueue(object: Callback<ResponseForceBody<Piece>> {
            override fun onFailure(call: Call<ResponseForceBody<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseForceBody<Piece>>, response: Response<ResponseForceBody<Piece>>) {
                val piece = response.body()?.data
                if(response.isSuccessful && piece != null) {
                    successCB(piece)
                } else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun score(context: Context,
              user: UserV4,
              content: Content,
              problems: Set<Problem>,
              time: Int? = null , successCB: (goalInfo: ScoredStudentGoalInfo?) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "assignID" to content.assignID!!
        )
        param["scoringProblemRequest"] = problems.map {

            // 멀티 선택 처리
            if(it.userAnswer?.contains(",") == false && it.userAnswer?.contains("-") == false) { // - 주관식이나 선다형이 아닐때
                try { // 숫자 변환 시 Exceptio 처리
                    it.userAnswer = if (it.userAnswer != null) it.userAnswer!!.toFloat().toInt().toString() else null // 답 앞에 0 들어가는 것 전처리
                } catch (e:Exception) {
                    LogUtils.errorEvent(PulleyEvent.ERROR, user, "${it.userAnswer} 를 int로 변환할 수 없음")
                    it.userAnswer = null
                }
            }

            val problemParam = Parameter(
                    "userAnswer" to it.userAnswer,
                    "studyID" to it.studyID,
                    "result" to if(it.getResultByUserAnswer() == Result.correct) Result.correct.rawValue else Result.incorrect.rawValue
            )
            problemParam
        }


        if(time != null) {
            param["moRequest"] = Parameter(
                    "time" to time
            )
        }

        val similarProblems = problems.filter { it.isSimilarProblem() }
        if(similarProblems.isNotEmpty()) {
            param["addSimilarProblemRequest"] = similarProblems.map {
                // 멀티 선택 아닐 경우만
                if(it.userAnswer?.contains(",") == false) {
                    try { // 숫자 변환 시 Exceptio 처리
                        it.userAnswer = if (it.userAnswer != null) it.userAnswer!!.toFloat().toInt().toString() else null // 답 앞에 0 들어가는 것 전처리
                    } catch (e:Exception) {
                        LogUtils.errorEvent(PulleyEvent.ERROR, user, "${it.userAnswer} 를 int로 변환할 수 없음")
                        it.userAnswer = null
                    }
                }
                val similarParam = Parameter(
                        "userAnswer" to it.userAnswer,
                        "problemNum" to it.problemNum,
                        "result" to if(it.getResultByUserAnswer() == Result.correct) Result.correct.rawValue else Result.incorrect.rawValue,
                        "unitCode" to it.unitCode,
                        "problemID" to it.id,
                        "parentProblemID" to it.rootProblem!!.id
                )
                similarParam
            }
        }

        API_V2.score(param).enqueue(object: Callback<ScoredStudentGoalInfo> {
            override fun onFailure(call: Call<ScoredStudentGoalInfo>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ScoredStudentGoalInfo>, response: Response<ScoredStudentGoalInfo>) {
                if(response.isSuccessful) {
                    if(problems.isNotEmpty()) {
                        content.score = problems.filter { it.getResultByUserAnswer() == Result.correct }.size * 100 / problems.size
                    } else {
                        LogUtils.errorEvent(PulleyEvent.PROBLEM_NOT_EXIST, user,"err: problem is not exist in score\n" +
                                "pieceID: ${content.pieceID}" +
                                "assignID: ${content.assignID}" +
                                "studentID: ${user.studentID}")

                        DialogUtils.showServerErr(context)
                        return
                    }
                    isNeedToSyncMyContentList = true
                    when(content) {
                        is Book -> BookManager.notifyBookScored(context, content)
                        is Test -> {
                            content.scoringTestPieceCount = content.scoringTestPieceCount + 1
                            if(content.getTestType() == Test.TestType.daily && content.scoringTestPieceCount == COUNT_MAXIMUM_DAILY_TEST) {
                                TestManager.isNeedToFullDailyResultInTab = true
                            }
                            TestManager.notifyTestScored(context, content)
                        }
                        is MockExam -> MockExamManager.notifyTestScored(context, content)
                    }
                    val goalInfo = response.body()
                    successCB(goalInfo)

                } else {
                    responseError(context, response, param)
                }
            }

        })
    }

    fun makeRecommendPiece(context: Context, user: UserV4, successCB: (context: Book) -> Unit, failedCb: (String?) -> Unit) {
        API_V3.makeRecommend(user.studentID).enqueue(object: Callback<ResponseBody<Book>>{
            override fun onFailure(call: Call<ResponseBody<Book>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseBody<Book>>, response: Response<ResponseBody<Book>>) {
                val piece = response.body()
                if(response.isSuccessful && piece != null)
                    piece.data?.let { successCB(it) }
                else {
                    try {
                        val errorBody = Gson().fromJson(response.errorBody()?.string(), ResponseBody::class.java)
                        failedCb(errorBody.error)
                    } catch (e: Exception) {
                        Log.e("ContentManager", "errorBody Parsing ERROR!")
                    }
                }
            }
        })
    }

    fun makeWrongPiece(context: Context, user: UserV4, successCB: (context: Piece) -> Unit) {
        API_V3.makeWrongNote(user.studentID).enqueue(object: Callback<ResponseBody<Piece>>{
            override fun onFailure(call: Call<ResponseBody<Piece>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseBody<Piece>>, response: Response<ResponseBody<Piece>>) {
                val piece = response.body()?.data
                if(response.isSuccessful && piece != null)
                    successCB(piece)
                else
                    responseError(context, response)
            }
        })
    }
}