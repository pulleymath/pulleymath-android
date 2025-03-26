package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.BookFilterSection
import io.reactivex.Completable
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

object PatternStudyApi {
    fun patternStudyService(): PatternStudyService = Network.retrofit(Network.Type.spring).create(
        PatternStudyService::class.java)
}
interface PatternStudyService {
    @GET("v3/books/{studentID}/plans")
    suspend fun getPatternStudyPlanList(
        @Path("studentID") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseBody<MyBookList>

    @GET("v2/study-history/{studentID}/pieces/all")
    suspend fun getPatternStudyHistory(
        @Path("studentID") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseListBody<Book>

    @GET("v3/books/recommend/{studentID}")
    suspend fun getPatternStudyRecommendBookList(
        @Path("studentID") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseListBody<RecommendBookList>

    @GET("v3/books/all")
    suspend fun getAllPatternStudyBookList(
        @Query("filter") filter: String,
        @Query("order") order: String = FilterOrder.DEFAULT.text,
        @Query("category") category: String = FilterCategory.BOOK.text,
        @Query("schoolType") school: String? = schoolType.name
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
    @GET("v1/filters")
    suspend fun fetchBookFilter(
        @Query("type") bookType: String?,
        @Query("schoolType") school: String? = schoolType.name
    ): ResponseListBody<BookFilterSection>

}