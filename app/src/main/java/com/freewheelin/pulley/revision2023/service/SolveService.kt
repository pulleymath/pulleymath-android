package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.NoteReviewRequest
import com.freewheelin.pulley.revision2023.model.response.NoteReviewResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

object SolveApi {
    fun solveService(): SolveService = Network.retrofit(Network.Type.spring).create(
        SolveService::class.java)
}
interface SolveService {

    @POST("v3/test/start")
    suspend fun getDailyTest(
        @Query("type") testType: String,
        @Query("schoolType") school: String? = schoolType.name,
    ): ResponseForceBody<Test>

    @POST("v1/review/problems")
    suspend fun getReviewProblems(
        @Body params: NoteReviewRequest
    ): ResponseForceBody<NoteReviewResponse>
}