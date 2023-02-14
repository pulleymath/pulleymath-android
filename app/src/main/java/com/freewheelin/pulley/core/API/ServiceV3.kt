package com.freewheelin.pulley.core.API

import com.freewheelin.pulley.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.contents.*
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

    @POST("signin/app")
    fun loginApp(@Body params: RequestLogin): Call<Template<User?>>

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

    @POST("/test/report")
    fun getTestReport(@Body param: Parameter): Call<Template<Test>>

//    @GET("v3/books/all")
//    fun getBooksNew(@Query("filter") filter: String,
//                    @Query("order") order: String,
//                    @Query("category") category: String): Call<List<Book>>

    @GET("books/{studentID}/plans")
    fun getMyBookList(@Path("studentID") studentID: String): Call<ResponseBody<MyBookList>>

    @GET("daily-summary/{studentID}/pieces/all")
    fun getStudyList(@Path("studentID") studentID: String): Call<ResponseListBody<Content>>

    @GET("daily-summary/{studentID}/pieces")
    fun getDailyPiece(@Path("studentID") studentID: String): Call<ResponseListBody<Content>>
}