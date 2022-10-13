package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.LCPriorConceptInfo
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object LCPriorConceptApi {
    fun lcPriorConceptService(): LCPriorConceptService = Network.retrofit(Network.Type.cooking).create(LCPriorConceptService::class.java)
}
interface LCPriorConceptService {

    @GET("priorconcepts/chapters/{chapterId}/users/{studentId}")
    fun getPriorConceptChapters(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingListResponse<LCPriorConceptInfo>>

    @POST("review/{reviewId}/users/{studentId}")
    fun completedReview(
        @Path("reviewId") reviewId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingResponse<Unit?>>
}