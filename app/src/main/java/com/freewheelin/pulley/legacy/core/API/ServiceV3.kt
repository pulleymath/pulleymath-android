package com.freewheelin.pulley.legacy.core.API

import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API.ResponseModel.*
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.ResponseBookInfo2
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.*
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface  ServiceV3 {

    @GET("mock/public/{studentID}")
    fun getNewMockExam(@Path("studentID") studentID: String): Call<List<MockExam>>

    @GET("mock/personal/{studentID}")
    fun getMyMockExam(@Path("studentID") studentID: String): Call<List<MockExam>>

    @GET("mock/summary/{mockID}")
    fun getMockExamSummary(@Path("mockID") mockID: Int, @Query("studentID") studentID: String): Call<MockExamSummary>

    @GET("mock/{mockID}/problems")
    fun getMock(@Path("mockID") mockID: Int, @Query("studentID") studentID:String, @Query("optional") optional:String, @Query("isRestart") isRestart :Boolean): Call<MockExam>

    @POST("mock/{mockID}/problems/email")
    fun sendMockMail(@Path("mockID") mockID: Int, @Body mockEmailRequest: MockEmailRequest): Call<Void>

    @GET("mock/student/{studentID}/report/{assignID}")
    fun getMockReport(@Path("studentID") studentID: String, @Path("assignID") assignID: Int): Call<MockExamAnalysis>

    @GET("me/app")
    fun getUser(): Call<Template<User>>

    @GET("me/app")
    fun getUserObservable(): Observable<Template<User>>

    @POST("signin/app")
    fun loginApp(@Body params: RequestLogin): Call<Template<User?>>

    @POST("signin/app/token")
    fun getAppToken(@Body params: RequestLogin): Observable<ResponseBody<SignInAppToken>>

    @PUT("users/{studentID}/subjects/common")
    fun changeCommonSubject(
        @Path("studentID") studentID: String,
        @Body params: Parameter): Call<Void>

    @PUT("users/{studentID}/subjects/optional")
    fun changeOptionalSubject(
        @Path("studentID") studentID: String,
        @Body params: Parameter): Call<Void>

    @PUT("daily-test/{studentID}/exclude")
    fun setExcludeStudied(
        @Path("studentID") studentID: String,
        @Body params: Parameter): Call<Void>

    //deprecated
    @POST("/test/report")
    fun getTestReport(@Body param: Parameter): Call<Template<Test>>

    @GET("test/{assignID}/report")
    fun getTestReportWithAssignId(
        @Path("assignID") assignID: Int,
        @Query("pieceSubCategory") pieceSubCategory: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseForceBody<Test>>

    @GET("v3/books/all")
    fun getBooksNew(@Query("filter") filter: String,
                    @Query("order") order: String,
                    @Query("category") category: String): Call<List<Book>>

    @GET("books/{studentID}/{assignID}/problems")
    fun getBook(@Path("studentID") studentID: String,
                @Path("assignID") assignID: Int): Call<ResponseBody<ResponseBookInfo2>>


    @GET("books/{studentID}/plans")
    fun getMyBookList(@Path("studentID") studentID: String): Call<ResponseBody<MyBookList>>

    @GET("daily-summary/{studentID}/pieces/all")
    fun getStudyList(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseListBody<Content>>

    @GET("daily-summary/{studentID}/pieces")
    fun getDailyPiece(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseListBody<Content>>

//    @GET("test/{studentID}?now=2023-03-04 12:00:00")
    @GET("test")
    fun getTestList(
        @Query("schoolType") school: String? = schoolType.name,
    ): Call<ResponseBody<List<Test>>>

    @GET("test/all")
    fun getAllTestList(
        @Query("schoolType") school: String? = schoolType.name,
    ): Call<ResponseBody<List<Test>>>

    @GET("commercials/{pieceID}/pages")
    fun getCommercialBookPage(@Path("pieceID") pieceID: Int): Call<ResponseBody<CommercialBookPageResponse>>

    @POST("commercials/{pieceID}/similar/problems")
    fun getCommercialSimilarCnt(
        @Path("pieceID") pieceID: Int,
        @Body params: Parameter): Call<ResponseBody<Int>>


    @POST("commercials/{pieceID}/custom")
    fun makeCustomBook(@Path("pieceID") pieceID: Int,
                       @Body params: Parameter): Call<ResponseBody<Book>>

    @GET("commercials")
    fun getCommercials(
        @Query("subject") subject: CommercialSubject?,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseBody<List<CommercialBook>>>

    @GET("daily-summary/{studentID}/recommend")
    fun getDailyRecommend(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseBody<DailyRecommend>>

    @POST("daily-summary/{studentID}/notes")
    fun makeWrongNote(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseBody<Piece>>

    @POST("daily-summary/{studentID}/weak")
    fun makeRecommend(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseBody<Book>>

    @GET("daily-summary/{studentID}/studies")
    fun getDailyStudy(
        @Path("studentID") studentID: String,
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseBody<DailyStudy>>

    @GET("test/daily/report")
    fun getDailyTestReport(
        @Query("schoolType") school: String? = schoolType.name
    ): Call<ResponseListBody<Test>>
}