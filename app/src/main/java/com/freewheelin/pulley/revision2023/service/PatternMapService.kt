package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternCard
import com.freewheelin.pulley.revision2021.model.LCPriorConceptInfo
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.repository.remote.LCPriorConceptService
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.LCPatternMapWrapper
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
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