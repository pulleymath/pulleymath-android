package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

object StudyMemoApi {
    fun studyMemoService(): StudyMemoService = Network.retrofit(Network.Type.spring).create(
        StudyMemoService::class.java)
}
interface StudyMemoService {

    @GET("api/v1/memo")
    suspend fun fetchMemos(
        @Query("student_id") studentId: String,
        @Query("main_id") mainId: Int, // assignId
        @Query("sub_id") subId: Int?, // problemId
        @Query("os") os: String?, // android / ios
        @Query("memo_case") memoCase: String?, // PROBLEM / SOLUTION
        @Query("latest") latest: Long?,
    ): ResponseListBody<StudyMemo>

    @Multipart
    @POST("api/v1/memo")
    suspend fun uploadMemoByteArray(@Part image: MultipartBody.Part,
                                    @Part("student_id") studentId: RequestBody,
                                    @Part("main_id") mainId: RequestBody,
                                    @Part("sub_id") subId: RequestBody,
                                    @Part("memo_case") memoCase: RequestBody,
                                    @Part("os") os: RequestBody,
                                    @Part("updated_at") updatedAt: RequestBody,
    ): ResponseBody<StudyMemo>

//    @Multipart
    @POST("api/v1/memo")
    suspend fun uploadMemo(@Body body: StudyMemoRequest): ResponseBody<StudyMemo>
}