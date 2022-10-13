package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import io.reactivex.Observable
import retrofit2.http.*

object LCWrongNoteMapApi {
    fun lcWrongNoteMapService(): LCWrongNoteMapService = Network.retrofit(Network.Type.cooking).create(LCWrongNoteMapService::class.java)
}
interface LCWrongNoteMapService {

    @GET("wrong/chapters/{chapterId}/users/{studentId}")
    fun fetchLcWrongNote(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String,
        @Query("filter") majorID: String
    ): Observable<BaseCookingResponse<LCWrongNoteMapCardWrapper>>

}