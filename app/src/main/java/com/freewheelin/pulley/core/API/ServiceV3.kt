package com.freewheelin.pulley.core.API

import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.MockExamAnalysis
import com.freewheelin.pulley.model.Template
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
}