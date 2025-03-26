package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.NoteStudyAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.response.NoteStudyDetailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object NotesApi {
    fun notesService(): NotesService = Network.retrofit(Network.Type.spring).create(
        NotesService::class.java)
}
interface NotesService {

    @GET("api/subjects")
    suspend fun getSubjects(): ResponseListBody<CurriculumSubject>

    @GET("v2/notes")
    suspend fun getNotes(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("noteMode") mode: String,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseListBody<Problem>

    @GET("v2/notes/problem/{problemID}")
    suspend fun getProblemDetail(
        @Path("problemID") problemId: String,
    ): ResponseForceBody<NoteStudyDetailResponse>


    @POST("v1/advanced/problems")
    suspend fun makeAdvancedLearning(
        @Body req: NoteStudyAdvancedLearningRequest,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseForceBody<Piece>
}