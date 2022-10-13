package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.model.response.ScoringResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path


object LCCookingApi {
    fun lcCookingService(): LCCookingService = Network.retrofit(Network.Type.cooking).create(LCCookingService::class.java)
}
interface LCCookingService {

    @GET("concepts/{conceptCookingId}/users/{studentId}")
    fun fetchCookingGroceries(
        @Path("conceptCookingId") conceptCookingId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingResponse<CookingInfo>>

    @PATCH("concepts/exercises/{exerciseQuizId}/hint/users/{studentId}")
    fun useHint(
        @Path("exerciseQuizId") exerciseQuizId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingResponse<Unit?>>

    @PATCH("users/{studentId}/exercises/{exerciseQuizId}")
    fun scoringCookingQuiz(
        @Path("exerciseQuizId") exerciseQuizId: Int,
        @Path("studentId") studentId: String,
        @Body userAnswer: ScoringReq
    ): Observable<BaseCookingResponse<ScoringResponse>>

}