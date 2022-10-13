package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.LCPatternCard
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

object LCPatternMapApi {
    fun lcPatternMapService(): LCPatternMapService = Network.retrofit(Network.Type.cooking).create(LCPatternMapService::class.java)
}
interface LCPatternMapService {

    @GET("patterns/chapters/{chapterId}/users/{studentId}")
    fun fetchPatternMapInfo(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingListResponse<LCPatternCard>>

}