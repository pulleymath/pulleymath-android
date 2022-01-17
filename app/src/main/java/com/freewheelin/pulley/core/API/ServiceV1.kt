package com.freewheelin.pulley.core.API

import com.freewheelin.pulley.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.core.API.ResponseModel.WeeklyProblemCount
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.ResponseBookInfo
import com.freewheelin.pulley.core.manage.ResponseBookList
import com.freewheelin.pulley.core.manage.ResponseProblemDetail
import com.freewheelin.pulley.core.manage.VersionInfo
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.model.curation.MainCuration
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

fun ServiceV1.postInquiry(inquiryType: String, subject: String, contents: String, studentID: String): Call<Void> {
    val param: Parameter = hashMapOf(
            "studentID" to studentID,
            "subject" to subject,
            "contents" to contents,
            "inquiryType" to inquiryType
    )
    return postInquiry(param)
}

fun ServiceV1.getMyMockExamList(user: User): Call<Template<List<MockExam>>> {
    val params: Parameter = Parameter(
            "publicSearch" to false,
            "pieceCategory" to "MO",
            "pieceDerived" to "BASIC",
            "studentID" to user.studentID
    )

    return getPieceList(params)
}

fun ServiceV1.getNewMockExamList(user: User): Call<Template<List<MockExam>>> {
    val params: Parameter = Parameter(
            "publicSearch" to true,
            "pieceCategory" to "MO",
            "pieceDerived" to "BASIC",
            "studentID" to user.studentID
    )

    return getPieceList(params)
}

fun ServiceV1.findEmail(name: String, phoneNum: String): Call<Template<String>> {
    val param: Parameter = Parameter(
            "cellPhone" to  phoneNum,
            "name" to name
    )

    return findEmail(param)
}

fun ServiceV1.findPassword(email: String): Call<Template<String>> {
    val params: Parameter = Parameter(
            "loginID" to email
    )
    return findPassword(params)
}

fun ServiceV1.assignMockExam(mock: MockExam, user: User): Call<Template<Map<String,String>>> {
    val params: Parameter = Parameter(
            "studentID" to user.studentID,
            "pieceID" to mock.id
    )
    return assignPiece(params)
}

fun ServiceV1.getProblemList(mock: MockExam, user: User): Call<Template<Map<String, String>>> {
    val params: Parameter = Parameter(
            "studentID" to user.studentID
    )

    if(mock.assignID != null) {
        params["assignID"] = mock.assignID!!
    } else {
        params["pieceID"] = mock.id
    }

    return getPieceInfo(params)
}



interface ServiceV1 {

//    @POST("/users/register")
//    fun signup(@Body params: Parameter): Call<Template<Map<String, String>>>

    @POST("/v1/users/signup/app")
    fun signup(@Body request: RequestSignup): Call<Template<String?>>

    @GET("/users/check_id/{loginID}")
    fun checkID(@Path("loginID") email: String): Call<Template<Map<String, String>>>


    @POST("/users/update")
    fun update(@Body param: Parameter): Subject<Response<Template<Map<String, String>>>>

    @POST("/users/find_id")
    fun findEmail(@Body parama: Parameter): Call<Template<String>>

    @POST("/users/find_pw")
    fun findPassword(@Body parama: Parameter): Call<Template<String>>

    @POST("/support/inquiry")
    fun postInquiry(@Body param: Parameter): Call<Void>

    @POST("/users/update")
    fun update2(@Body param: Parameter): Call<Template<Map<String, String>>>

    @GET("/support/notice")
    fun getNoticeList(): Call<Template<List<Notice>>>

    @GET("/support/faq")
    fun getFAQList(): Call<Template<List<FAQ>>>

    @GET("/version/")
    fun getVersionInfo(): Call<Template<VersionInfo>>

    @POST("/piece/list")
    fun getPieceList(@Body param: Parameter): Call<Template<List<MockExam>>>

    @POST("/review/piece")
    fun getReviewInfo(@Body param: Parameter): Call<Template<MockExam>>

    @POST("/review/piece")
    fun getPieceReviewInfo(@Body param: Parameter): Call<Template<Piece>>

    @POST("/review/problem")
    fun getReviewProblems(@Body param: Parameter): Call<Template<List<Problem>>>

    @POST("/piece/assign")
    fun assignPiece(@Body param: Parameter): Call<Template<Map<String, String>>>

    @POST("/piece/get")
    fun getPieceInfo(@Body param: Parameter): Call<Template<Map<String, String>>>

    @POST("/piece/problems")
    fun getPieceProblems(@Body param: Parameter): Call<Template<List<Problem>>>

    @POST("/piece/problems/renew")
    fun getPieceProblemsRenew(@Body param: Parameter): Call<Template<List<Problem>>>

    @POST("/piece/deleteAllMo")
    fun clearAllMyMockExam(@Body studentID: RequestBody): Call<Template<Map<String, String>>>

    @POST("/scoring/")
    fun scoring(@Body param: Parameter): Call<Template<MockExam>>

    @POST("/report/mo")
    fun getMockReport(@Body param: Parameter): Call<Template<MockExamAnalysis>>

    @POST("/mark/delete")
    fun deleteProblemMark(@Body param: Parameter): Call<Void>

    @POST("/mark/insert")
    fun addProblemMark(@Body param: Parameter): Call<Void>

    @POST("/similar/flexible")
    fun getSimilarProblem(@Body param :Parameter): Call<Template<Problem>>

    @POST("/notes/")
    fun getWrongNoteProblems(@Body param: Parameter): Call<Template<List<Problem>>>


    @POST("/book/get")
    fun getBookInfo(@Body param: Parameter): Call<Template<ResponseBookInfo>>

    @POST("/book/list")
    fun getBookList(@Body param: Parameter): Call<Template<ResponseBookList>>

    @POST("/book/assign")
    fun assignBook(@Body param: Parameter): Call<Template<Book>>

    @POST("/scoring/book")
    fun scoreBook(@Body param: Parameter): Call<Void>

    @POST("/book/deleteAllBook")
    fun clearAllBooks(@Body studentID: RequestBody): Call<Template<Map<String, String>>>

    @POST("/book/review")
    fun reviewBook(@Body param: Parameter): Call<Template<ResponseBookInfo>>

    @POST("/notes/detail")
    fun getProblemDetail(@Body param: Parameter): Call<Template<ResponseProblemDetail>>

    @POST("/review/problem")
    fun getProblemReview(@Body param: Parameter): Call<Template<Piece>>

    @POST("/weak/problems")
    fun getWeakPieceWithProblems(@Body param: Parameter): Call<Template<Piece>>

    @POST("/weak/pieces")
    fun getWeakPieceWithPieces(@Body param: Parameter): Call<Template<Piece>>

    @POST("/weak/chapters")
    fun getWeakPieceWithChapters(@Body param: Parameter): Call<Template<Piece>>

    @POST("/mark/deleteAllScrap")
    fun clearAllScrap(@Body studentID: RequestBody): Call<Void>

    @POST("/mark/deleteAllClear")
    fun clearAllClear(@Body studentID: RequestBody): Call<Void>

    @POST("/analysis/")
    fun getAnalysis(@Body param: Parameter): Call<Template<Analysis>>

    @POST("/scoring/deleteAll")
    fun clearAllScroing(@Body studentID: RequestBody): Call<Void>

    @POST("/piece/send")
    fun sendPieceEmail(@Body param: Parameter): Call<Void>

    @POST("/piece/list")
    fun getMyContents(@Body param: Parameter): Call<Template<List<Content>>>

    @POST("/test/list")
    fun getTests(@Body param: Parameter): Call<Template<List<Test>>>

    @POST("/scoring/")
    fun scoringTest(@Body param: Parameter): Call<Template<Test>>

    @POST("/review/piece")
    fun getTestReviewInfo(@Body param: Parameter): Call<Template<Test>>

    @POST("/test/weekly/count")
    fun getWeeklyProblemCount(@Body param: Parameter): Call<Template<WeeklyProblemCount>>

    @POST("/test/")
    fun getTest(@Body param: Parameter): Call<Template<Test>>

    @POST("/test/report")
    fun getTestReport(@Body param: Parameter): Call<Template<Test>>

    @POST("/test/deleteAllTest")
    fun clearAllTest(@Body studentID: RequestBody): Call<Void>

    @POST("/main/curation")
    fun getMainCuration(@Body param: Parameter): Call<Template<MainCuration>>

    @PUT("/mark/")
    fun putProblemsMark(@Body param: Parameter): Call<Void>

    @HTTP(method = "DELETE", path = "/mark/", hasBody = true)
    fun deleteProblemsMark(@Body param: Parameter): Call<Void>

    @POST("/review/chapter")
    fun getReviewFromChapter(@Body param: Parameter): Call<Template<Piece>>

    @POST("/piece/mo/curation")
    fun getMockCuration(@Body param: Parameter): Call<Template<String>>

    @POST("/log/user")
    fun logLaunch(@Body Param: Parameter): Call<Template<String>>

    @POST("/auth/sms_check")
    fun authCodeNum(@Body param: Parameter): Call<Void>

    @POST("/users/update_cellphone")
    fun updateCellphone(@Body param: Parameter): Call<Void>

    @POST("/auth/sms_auth")
    fun authPhoneNum(@Body param: Parameter): Call<Void>

    @POST("/auth/anonymous/sms_auth")
    fun authAnonyPhoneNum(@Body param: Parameter): Call<Void>

    @POST("/auth/anonymous/sms_check")
    fun authAnonyCodeNum(@Body param: Parameter): Call<Void>

    @POST("/auth/check_phone_number")
    fun checkPhoneExist(@Body param: Parameter): Call<Void>

    @POST("/log/user")
    fun logUser(@Body param: Parameter): Call<Void>
}