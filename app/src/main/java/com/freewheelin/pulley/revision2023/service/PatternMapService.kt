package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.LCPatternMapWrapper
import retrofit2.http.GET
import retrofit2.http.Path

object PatternMapApi {
    fun patternMapService(): PatternMapService = Network.retrofit(Network.Type.cooking).create(PatternMapService::class.java)
}
interface PatternMapService {

    @GET("patterns/chapters/{chapterId}/users/{studentId}")
    suspend fun fetchPatternMap(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String = user?.studentID!!,
    ): LCPatternMapWrapper
}