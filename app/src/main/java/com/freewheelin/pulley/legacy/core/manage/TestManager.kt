package com.freewheelin.pulley.legacy.core.manage

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
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

    fun getTestList(context: Context, cb: (tests: List<Test>) -> Unit) {
        if (BuildConfig.FLAVOR == "beta") {
            API_V3.getAllTestList().enqueue(object: Callback<ResponseBody<List<Test>>> {
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
        } else {
            API_V3.getTestList().enqueue(object: Callback<ResponseBody<List<Test>>> {
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
    }

    fun getDailyTest(context: Context, user: UserV4, test: Test, successCB: (test: Test) -> Unit) {
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


    fun getTest(context: Context, user: UserV4, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "pieceSubCategory" to test.pieceSubCategory!!
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

    fun getInitTest(context: Context, user: UserV4, successCB: (test: Test) -> Unit) {
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

    fun getAllSubjects(context: Context, successCB: (List<CurriculumSubject>) -> Unit) {
        API_V1.getAllSubjects().enqueue(object : Callback<ResponseListBody<CurriculumSubject>> {
            override fun onResponse(
                call: Call<ResponseListBody<CurriculumSubject>>,
                response: Response<ResponseListBody<CurriculumSubject>>
            ) {
                val curriculumSubjects = response.body()?.data
                if(response.isSuccessful && curriculumSubjects != null)
                    successCB(curriculumSubjects)
                else {
                    responseError(context, response)
                }
            }

            override fun onFailure(call: Call<ResponseListBody<CurriculumSubject>>, t: Throwable) {

            }

        })
    }
    fun getTestReport(context: Context, user: UserV4, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter(
                "assignID" to test.assignID!!,
                "studentID" to user.studentID,
                "pieceSubCategory" to test.pieceSubCategory!!
        )

        API_V3.getTestReportWithAssignId(test.assignID!!, test.pieceSubCategory!!).enqueue(object: Callback<ResponseForceBody<Test>> {
            override fun onFailure(call: Call<ResponseForceBody<Test>>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseForceBody<Test>>, response: Response<ResponseForceBody<Test>>) {
                val responseTest = response.body()?.data
                if(response.isSuccessful && responseTest != null)
                    successCB(responseTest)
                else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun getDailyTestReport(context: Context, user: UserV4, successCB: (tests: List<Test>) -> Unit) {

        API_V3.getDailyTestReport().enqueue(object: Callback<ResponseListBody<Test>> {
            override fun onFailure(call: Call<ResponseListBody<Test>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseListBody<Test>>, response: Response<ResponseListBody<Test>>) {
                val tests = response.body()?.data
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

    fun getTestReview(context: Context, user: UserV4, test: Test, successCB: (test: Test) -> Unit) {
        val param: Parameter = Parameter (
                "studentID" to user.studentID,
                "assignID" to test.assignID!!
        )
        API_V1.getTestReviewInfoV1(param).enqueue(object: Callback<ResponseForceBody<Test>> {
            override fun onFailure(call: Call<ResponseForceBody<Test>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseForceBody<Test>>, response: Response<ResponseForceBody<Test>>) {
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

    fun clearTests(context: Context, user: UserV4, cb: (() -> Unit)) {
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