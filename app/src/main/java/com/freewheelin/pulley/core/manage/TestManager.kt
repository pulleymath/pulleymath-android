package com.freewheelin.pulley.core.manage

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.API_V3
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.responseError
import com.freewheelin.pulley.utils.responseFailed
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TestManager {
    const val ARG_TEST = "ARG_TEST"
    const val ARG_FROM_INIT_TEST = "ARG_FROM_INIT_TEST"
    const val EVENT_TEST_SCORING = "EVENT_TEST_SCORING"
    const val EVENT_TEST_SETTING = "EVENT_TEST_SETTING"

    const val COUNT_MAXIMUM_DAILY_TEST = 3

    var isNeedToFullDailyResultInTab = false

    fun getTestList(context: Context, user: User, cb: (tests: List<Test>) -> Unit) {
        API_V3.getTestList(user.studentID).enqueue(object: Callback<ResponseBody<List<Test>>> {
            override fun onFailure(call: Call<ResponseBody<List<Test>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseBody<List<Test>>>, response: Response<ResponseBody<List<Test>>>) {
                val test = response.body()
                if(test?.data != null && response.isSuccessful) {
                    cb(test.data)
                } else {
                    responseError(context, response)
                }
            }

        })
    }

    fun getDailyTest(context: Context, user: User, test: Test, successCB: (test: Test) -> Unit) {
        println("asoaso user!!.studentID :${user.studentID}")
        API_V2.getDailyTest(user.studentID).enqueue(object: Callback<Template<Test>>{
            override fun onFailure(call: Call<Template<Test>>, t: Throwable) {
                // 데이터를 가져올 수 없습니다.
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Test>>, response: Response<Template<Test>>) {
                val responseTest = response.body()?.data
                if(response.isSuccessful && responseTest != null) {
                    successCB(responseTest)
                } else {
                    responseError(context, response, Parameter("studentID" to user!!.studentID))
                }
            }
        })
    }


    fun getTest(context: Context, user: User, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "pieceSubCategory" to test.pieceSubCategory
        )

        API_V1.getTest(param).enqueue(object: Callback<Template<Test>>{
            override fun onFailure(call: Call<Template<Test>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Test>>, response: Response<Template<Test>>) {
                val responseTest = response.body()?.data

                if(response.isSuccessful && responseTest != null) {
                    successCB(responseTest)
                } else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun getInitTest(context: Context, user: User, successCB: (test: Test) -> Unit) {
        API_V2.getInitTest(user.studentID).enqueue(object: Callback<Test>{
            override fun onFailure(call: Call<Test>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Test>, response: Response<Test>) {
                val responseTest = response.body()
                if(response.isSuccessful && responseTest != null) {
                    successCB(responseTest)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getTestReport(context: Context, user: User, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter(
                "assignID" to test.assignID!!,
                "studentID" to user.studentID,
                "pieceSubCategory" to test.pieceSubCategory
        )

        API_V3.getTestReport(param).enqueue(object: Callback<Template<Test>> {
            override fun onFailure(call: Call<Template<Test>>, t: Throwable) {

            }

            override fun onResponse(call: Call<Template<Test>>, response: Response<Template<Test>>) {
                val responseTest = response.body()?.data
                if(response.isSuccessful && responseTest != null)
                    successCB(responseTest)
                else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun getDailyTestReport(context: Context, user: User, successCB: (tests: List<Test>) -> Unit) {

        API_V2.getDailyTestReport(user.studentID).enqueue(object: Callback<List<Test>> {
            override fun onFailure(call: Call<List<Test>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<List<Test>>, response: Response<List<Test>>) {
                val tests = response.body()
                if(tests != null && response.isSuccessful) {
                    successCB(tests)
                } else {
                    responseError(context, response)
                }

            }
        })
    }

    fun notifyTestScored(context: Context, test: Test) {
        val intent = Intent(EVENT_TEST_SCORING)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun getTestReview(context: Context, user: User, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter (
                "studentID" to user.studentID,
                "assignID" to test.assignID!!
        )
        API_V1.getTestReviewInfo(param).enqueue(object: Callback<Template<Test>> {
            override fun onFailure(call: Call<Template<Test>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Test>>, response: Response<Template<Test>>) {
                val resTest = response.body()?.data
                resTest?.arrangeProblem()
                if(response.isSuccessful && resTest != null) {
                    successCB(resTest)
                } else {
                    responseError(context, response, param)
                }
            }

        })
    }

    fun clearTests(context: Context, user: User, cb: (() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)

        API_V1.clearAllTest(body).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    cb()
                } else {
                    responseError(context, response, Parameter("studentID" to user.studentID))
                }
            }
        })
    }
}