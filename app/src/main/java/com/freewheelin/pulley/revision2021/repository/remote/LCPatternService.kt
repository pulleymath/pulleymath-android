package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import io.reactivex.Observable
import retrofit2.http.*

object LCPatternApi {
    fun lcPatternService(): LCPatternService = Network.retrofit(Network.Type.cooking).create(LCPatternService::class.java)
}
interface LCPatternService {

    @GET("patterns/{patternId}/users/{studentId}")
    fun fetchPatternInfo(
        @Path("patternId") patternId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingListResponse<LCPatternQuiz>>

    @PATCH("users/{studentId}/patterns/{patternQuizId}")
    fun patternQuizScoring(
        @Path("patternQuizId") patternQuizId: Int,
        @Path("studentId") studentId: String,
        @Query("type") type: String?,
        @Body userAnswer: ScoringReq
    ): Observable<BaseCookingResponse<LCPatternScoring>>

    @PATCH("patterns/quizzes/{patternQuizId}/hint/users/{studentId}")
    fun usePatternQuizHint(
        @Path("patternQuizId") patternQuizId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingResponse<Unit?>>

}