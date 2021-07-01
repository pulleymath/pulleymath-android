package com.freewheelin.pulley.core.manage

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.responseError
import com.freewheelin.pulley.utils.responseFailed
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ResponseProblemDetail {
    val problemInfo: ProblemDetailInfo? = null
    val history: List<History> = listOf()
}

object ProblemManager {
    const val ARG_PROBLEM = "ARG_PROBLEM"

    const val EVENT_WRONG_NOTE_CHANGED = "EVENT_WRONG_NOTE_CHANGED"
    const val EVENT_PROBLEM_CLEAR_CHANGED = "EVENT_PROBLEM_CLEAR_CHANGED"
    const val EVENT_PROBLEM_SCRAP_CHANGED = "EVENT_PROBLEM_SCRAP_CHANGED"


    fun getScrapProblems(context: Context, user: User, from: Date, to: Date, cb:(problems: List<Problem>) -> Unit) {
        val param: Parameter = Parameter(
                "endDate" to DateTimeUtils.yyyy_MM_dd.format(to),
                "startDate" to DateTimeUtils.yyyy_MM_dd.format(from),
                "noteMode" to "SCRAP",
                "studentID" to user.studentID
        )

        API_V1.getWrongNoteProblems(param).enqueue(object: Callback<Template<List<Problem>>> {
            override fun onFailure(call: Call<Template<List<Problem>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<List<Problem>>>, response: Response<Template<List<Problem>>>) {
                if(response.isSuccessful) {
                    val problems = response.body()?.data ?: listOf()
                    cb(problems)
                }
            }

        })
    }

    fun getWrongProblems(context: Context, user: User, from: Date, to: Date, cb:(problems: List<Problem>) -> Unit) {
        val param: Parameter = Parameter(
                "endDate" to DateTimeUtils.yyyy_MM_dd.format(to),
                "startDate" to DateTimeUtils.yyyy_MM_dd.format(from),
                "noteMode" to "WRONG",
                "studentID" to user.studentID
        )

        API_V1.getWrongNoteProblems(param).enqueue(object: Callback<Template<List<Problem>>> {
            override fun onFailure(call: Call<Template<List<Problem>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<List<Problem>>>, response: Response<Template<List<Problem>>>) {
                val problems = response.body()?.data ?: listOf()
                cb(problems)
            }

        })
    }

    fun scrap(context: Context, user: User, problem: Problem, isScrap: Boolean, successCB: () -> Unit) {
        val params: Parameter = Parameter(
                "markType" to "SCRAP",
                "problemID" to problem.id,
                "studentID" to user.studentID
        )
        val apiCall = if(isScrap) API_V1.addProblemMark(params) else API_V1.deleteProblemMark(params)
        apiCall.enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful)
                    successCB()
                else
                    responseError(context, response)
            }
        })
    }

    fun clear(context: Context, user: User, problem: Problem, isClear: Boolean, needBroadCasting: Boolean = false, successCB: () -> Unit) {
        val params: Parameter = Parameter(
                "markType" to "CLEAR",
                "problemID" to problem.id,
                "studentID" to user.studentID
        )
        val apiCall = if(isClear) API_V1.addProblemMark(params) else API_V1.deleteProblemMark(params)
        apiCall.enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    successCB()
                    if(needBroadCasting) {
                        val intent = Intent(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
                        intent.putExtra(ProblemManager.ARG_PROBLEM, problem)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    }
                } else
                    responseError(context, response)
            }
        })
    }

    fun getDetailInfo(context: Context, user: User, problem: Problem, successCB: (problem: Problem, detail: ProblemDetailInfo?, history: List<History>) -> Unit) {
        val params: Parameter = Parameter(
                "problemID" to problem.id,
                "studentID" to user.studentID
        )

        API_V1.getProblemDetail(params).enqueue(object: Callback<Template<ResponseProblemDetail>> {
            override fun onFailure(call: Call<Template<ResponseProblemDetail>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<ResponseProblemDetail>>, response: Response<Template<ResponseProblemDetail>>) {
                val detail = response.body()?.data?.problemInfo
                val history = response.body()?.data?.history
                if(response.isSuccessful && history != null) {
                    successCB(problem, detail, history)
                }
            }
        })
    }


    fun clearAllScrap(context: Context, user: User, cb:(() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)
        API_V1.clearAllScrap(body).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    val intent = Intent(EVENT_WRONG_NOTE_CHANGED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun clearAllClear(context: Context, user: User, cb: (() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)
        API_V1.clearAllClear(body).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    val intent = Intent(EVENT_WRONG_NOTE_CHANGED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun clearAllScoring(context: Context, user: User, cb:(() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)
        API_V1.clearAllScroing(body).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun clearProblems(context: Context, problem: List<Problem>,  user: User, cb:(() -> Unit)) {
        val param: Parameter = Parameter(
                "markType" to "CLEAR",
                "problemIDs" to problem.map { it.id },
                "studentID" to user.studentID
        )

        API_V1.putProblemsMark(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful)
                    cb()
                else
                    responseError(context, response, param)
            }
        })
    }

    fun unclearProblems(context: Context, problem: List<Problem>, user: User, cb:(() -> Unit)) {
        val param: Parameter = Parameter(
                "markType" to "CLEAR",
                "problemIDs" to problem.map { it.id },
                "studentID" to user.studentID
        )

        API_V1.deleteProblemsMark(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful)
                    cb()
                else
                    responseError(context, response, param)
            }
        })
    }
}