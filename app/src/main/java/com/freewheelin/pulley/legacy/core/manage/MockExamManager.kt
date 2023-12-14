package com.freewheelin.pulley.legacy.core.manage

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.*
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MockExamManager {
    const val ARG_MOCK_EXAM = "ARG_MOCK_EXAM"
    const val ARG_MOCK_NEED_TO_SUCCESS = "ARG_MOCK_NEED_TO_SUCCESS"
    const val ARG_SCORED_INFO = "ARG_SCORED_INFO"
    const val ARG_NEED_ASSIGN = "ARG_NEED_ASSIGN"
    const val ARG_START_PROBLEM = "ARG_START_PROBLEM"
    const val ARG_MOCK_IS_RESTART = "ARG_MOCK_IS_RESTART"
    const val ARG_STUDENT_ID = "ARG_STUDENT_ID"

    const val EVENT_MOCK_EXAM_SCORING = "EVENT_MOCK_EXAM_SCORING"

    const val EVENT_MOCK_EXAM_CLEAR = "EVENT_MOCK_EXAM_CLEAR"


    fun clearExam(context: Context, user: UserV4, cb: (() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)
        API_V1.clearAllMyMockExam(body).enqueue(object: Callback<Template<Map<String, String>>> {
            override fun onFailure(call: Call<Template<Map<String, String>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Map<String, String>>>, response: Response<Template<Map<String, String>>>) {
                if(response.isSuccessful) {
                    val intent = Intent(EVENT_MOCK_EXAM_CLEAR)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    ContentManager.isNeedToSyncMyContentList = true
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getMockExamReport(context: Context, exam: MockExam, user: UserV4, successCB: ((analysis: MockExamAnalysis) -> Unit), failCB: () -> Unit) {

        API_V3.getMockReport(user.studentID, exam.assignID!!).enqueue(object: Callback<MockExamAnalysis> {
            override fun onFailure(call: Call<MockExamAnalysis>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<MockExamAnalysis>, response: Response<MockExamAnalysis>) {
                if(response.isSuccessful) {
                    val analysis = response.body()!!
                    successCB(analysis)

                } else {
                    failCB()
                }
            }
        })

//        API_V1.getMockReport(param).enqueue(object: Callback<Template<MockExamAnalysis>> {
//            override fun onFailure(call: Call<Template<MockExamAnalysis>>, t: Throwable) {
//                responseFailed(context, t)
//            }
//
//            override fun onResponse(call: Call<Template<MockExamAnalysis>>, response: Response<Template<MockExamAnalysis>>) {
//                if(response.isSuccessful) {
//                    val analysis = response.body()!!.data
//                    successCB(analysis)
//
//                } else {
//                    failCB()
//                }
//            }
//        })
    }

    fun getStudentMockExamReport(context: Context, assignID: Int, studentID: String, successCB: ((analysis: MockExamAnalysis) -> Unit), failCB: () -> Unit) {

        API_V3.getMockReport(studentID, assignID).enqueue(object: Callback<MockExamAnalysis> {
            override fun onFailure(call: Call<MockExamAnalysis>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<MockExamAnalysis>, response: Response<MockExamAnalysis>) {
                if(response.isSuccessful) {
                    val analysis = response.body()!!
                    successCB(analysis)

                } else {
                    failCB()
                }
            }
        })
    }

    fun getProblems(context: Context, exam: MockExam, user: UserV4, isRestart: Boolean, successCB: (exam: MockExam) -> Unit) {
        API_V2.getMo(exam.pieceID, user.studentID, isRestart).enqueue(object: Callback<MockExam> {
            override fun onFailure(call: Call<MockExam>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<MockExam>, response: Response<MockExam>) {
                if(response.isSuccessful) {
                    val exams = response.body()!!
                    successCB(exams)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getMockProblems(context: Context, orgExam: MockExam, user: UserV4, successCB: (exam: MockExam) -> Unit) {

        Log.d("MockManager", "mockID=${orgExam.mockID}, optional=${orgExam.selectOptional}, isRestart=${orgExam.isRestart}")

        API_V3.getMock(orgExam.mockID, user.studentID, orgExam.selectOptional.joinToString(","), orgExam.isRestart).enqueue(object: Callback<MockExam> {
            override fun onFailure(call: Call<MockExam>, t: Throwable) {
                responseFailed(context, t)
            }
            override fun onResponse(call: Call<MockExam>, response: Response<MockExam>) {
                if(response.isSuccessful) {
                    val exams = response.body()!!
                    orgExam.assignID = exams.assignID
                    orgExam.problems = exams.problems
                    orgExam.markingState = exams.markingState
                    orgExam.time = exams.time
                    successCB(orgExam)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getExamReviewProblems(context: Context, exam: MockExam, user: UserV4, successCB: (mockExam: MockExam) -> Unit) {
        val params: Parameter = Parameter(
                "assignID" to exam.assignID!!,
                "studentID" to user.studentID
        )

        API_V1.getReviewInfo(params).enqueue(object: Callback<Template<MockExam>> {
            override fun onFailure(call: Call<Template<MockExam>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<MockExam>>, response: Response<Template<MockExam>>) {
                if(response.isSuccessful) {
                    response.body()?.data?.apply {
                        selectOptional = exam.selectOptional
                        majorType = exam.majorType
                        grade = exam.grade
                        arrangeProblem()
                        successCB(this)
                    }
                } else
                    responseError(context, response)
            }

        })
    }

    fun getMockCuration(context: Context, user: UserV4, successCB: (curation: String) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID
        )

        API_V1.getMockCuration(param).enqueue(object: Callback<Template<String>> {
            override fun onFailure(call: Call<Template<String>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<String>>, response: Response<Template<String>>) {
                val curation = response.body()?.data

                if(response.isSuccessful && curation != null) {
                    successCB(curation)
                }
            }

        })
    }


    fun getNewMockExamList(context: Context, user: UserV4, cb:(examList: List<MockExam>) -> Unit) {

        API_V3.getNewMockExam(user.studentID).enqueue(object: Callback<List<MockExam>> {
            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
                Log.d("FAILED??", t.message?:"")
            }

            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
                Log.d("SUCCESS??", response.body().toString())
                response.body()?.let {
                    cb(it)
                }
            }
        })

//        API_V2.getMoList(true, user.studentID).enqueue(object: Callback<List<MockExam>> {
//            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
//                Log.d("FAILED??", t.message)
//            }
//
//            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
//                Log.d("SUCCESS??", response.body().toString())
//                val examList = response.body()
//                cb(examList)
//            }
//        })

    }

    fun getMyMockExamList(context: Context, user: UserV4, cb: (examList: List<MockExam>?) -> Unit) {

        API_V3.getMyMockExam(user.studentID).enqueue(object: Callback<List<MockExam>> {
            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
                responseFailed(context!!, t)
            }

            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
                if(response.isSuccessful) {
                    val examList = response.body()
                    cb(examList)
                } else {
                    responseError(context, response)
                }
            }
        })

//        API_V2.getMoList(false, user.studentID).enqueue(object: Callback<List<MockExam>> {
//            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
//                responseFailed(context!!, t)
//            }
//
//            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
//                if(response.isSuccessful) {
//                    val examList = response.body()
//                    cb(examList)
//                } else {
//                    responseError(context, response)
//                }
//            }
//        })
    }

    fun getStudentMockExamList(context: Context, studentID: String, cb: (examList: List<MockExam>?) -> Unit) {

        API_V3.getMyMockExam(studentID).enqueue(object: Callback<List<MockExam>> {
            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
                responseFailed(context!!, t)
            }

            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
                if(response.isSuccessful) {
                    val examList = response.body()
                    cb(examList)
                } else {
                    responseError(context, response)
                }
            }
        })

//        API_V2.getMoList(false, user.studentID).enqueue(object: Callback<List<MockExam>> {
//            override fun onFailure(call: Call<List<MockExam>>, t: Throwable) {
//                responseFailed(context!!, t)
//            }
//
//            override fun onResponse(call: Call<List<MockExam>>, response: Response<List<MockExam>>) {
//                if(response.isSuccessful) {
//                    val examList = response.body()
//                    cb(examList)
//                } else {
//                    responseError(context, response)
//                }
//            }
//        })
    }

    fun getMockSummary(context: Context, content: Content, user: UserV4, cb: (summary: MockExamSummary?) -> Unit) {
        API_V1.getMockExamSummary(content.mockID, "${content.assignID}", user.studentID).enqueue(object : Callback<ResponseBody<MockExamSummary>> {
            override fun onFailure(call: Call<ResponseBody<MockExamSummary>>, t: Throwable) {
                responseFailed(context!!, t)
            }

            override fun onResponse(call: Call<ResponseBody<MockExamSummary>>, response: Response<ResponseBody<MockExamSummary>>) {
                if (response.isSuccessful) {
                    val examList = response.body()?.data
                    cb(examList)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun notifyTestScored(context: Context, mockExam: MockExam) {
        val intent = Intent(MockExamManager.EVENT_MOCK_EXAM_SCORING)
        intent.putExtra(ARG_MOCK_EXAM, mockExam)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun sendEmail(context: Context, mockExam: MockExam, user: UserV4, email: String, cb:() -> Unit) {
        val request = MockEmailRequest(user.studentID, mockExam.selectOptional.map { it.name }, mockExam.isRestart, email)

        API_V3.sendMockMail(mockExam.mockID, request).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                cb()
            }
        })
    }
}