package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.core.manage.ResponseBookList
import com.freewheelin.pulley.legacy.core.manage.ResponseProblemDetail
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.BookFilterItem
import com.freewheelin.pulley.revision2023.model.BookFilterSection
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import com.freewheelin.pulley.revision2023.model.request.NoteStudyAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.response.NoteStudyAdvancedLearningResponse
import com.freewheelin.pulley.revision2023.model.response.NoteStudyDetailResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

object NotesApi {
    fun notesService(): NotesService = Network.retrofit(Network.Type.spring).create(
        NotesService::class.java)
}
interface NotesService {

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