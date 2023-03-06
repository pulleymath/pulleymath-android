package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.core.manage.ResponseBookList
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.ResponseListBody
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

object PatternStudyApi {
    fun patternStudyService(): PatternStudyService = Network.retrofit(Network.Type.spring).create(
        PatternStudyService::class.java)
}
interface PatternStudyService {
    @GET("v3/books/{studentID}/plans")
    suspend fun getPatternStudyPlanList(
        @Path("studentID") studentId: String = user?.studentID!!
    ): ResponseBody<MyBookList>

    @GET("v3/books/recommend/{studentID}")
    suspend fun getPatternStudyRecommendBookList(
        @Path("studentID") studentId: String = user?.studentID!!
    ): ResponseListBody<RecommendBookList>

    @GET("v3/books/all")
    suspend fun getAllPatternStudyBookList(
        @Query("filter") filter: String,
        @Query("order") order: String = FilterOrder.DEFAULT.text,
        @Query("category") category: String = FilterCategory.BOOK.text
    ): ResponseListBody<Book>

    @PATCH("v2/books/{studentID}/pins")
    fun setPin(
        @Path("studentID") studentID: String = user?.studentID!!,
        @Query("pieceID") pieceID: Int,
        @Query("isPin") isPinned: Boolean,
    ): Completable

    @DELETE("v2/books/{studentID}/plans/pieces/{pieceID}")
    fun deleteFromMyBook(
        @Path("pieceID") pieceID: Int,
        @Path("studentID") studentID: String = user?.studentID!!,
    ): Completable


    // CustomizeBookDialog - 워크북 api
    @GET("v2/commercials")
    suspend fun getCommercials(
        @Query("subject") subject: CommercialSubject?
    ): List<CommercialBook>

}