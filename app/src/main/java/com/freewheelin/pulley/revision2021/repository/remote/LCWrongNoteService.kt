package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query


object LCWrongNoteApi {
    fun lcWrongNoteService() : LCWrongNoteService  = Network.retrofit(Network.Type.cooking).create(LCWrongNoteService::class.java)
}
interface LCWrongNoteService {

    @GET("chapters/{chapterId}")
    fun fetchCourseList(
        @Path("chapterId") chapterId: Int,
    ): Observable<BaseCookingListResponse<SingleCourseDesc>>

    @PATCH("users/{studentId}/patterns/{patternQuizId}")
    fun fetchCourseList2(
        @Path("studentId") studentId: Int,
        @Path("patternQuizId") patternQuizId: Int,
        @Query("type") type: String
        ): Observable<BaseCookingListResponse<SingleCourseDesc>>
}