package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object PriorConceptApi {
    fun priorConceptService(): PriorConceptService = Network.retrofit(Network.Type.cooking).create(
        PriorConceptService::class.java)
}
interface PriorConceptService {
    @GET("priorconcepts/chapters/{chapterId}/users/{studentId}")
    suspend fun getPriorConceptChapters(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String = user?.studentID!!,
    ): PriorConceptWrapper

    @POST("users/{studentId}/chapters/{chapterId}")
    fun createLearningCourse(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String = user?.studentID!!,
    ): Completable
}