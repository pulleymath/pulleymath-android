package com.freewheelin.pulley.core.API

import com.freewheelin.pulley.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.MockExamAnalysis
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import io.reactivex.Observable
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

    @POST("/test/report")
    fun getTestReport(@Body param: Parameter): Call<Template<Test>>

}