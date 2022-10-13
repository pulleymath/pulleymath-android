package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path


object LearningCourseApi {
    fun learningCourseService() : LearningCourseService  = Network.retrofit(Network.Type.cooking).create(LearningCourseService::class.java)
}
interface LearningCourseService {

    @GET("chapters/{chapterId}")
    fun fetchCourseList(
        @Path("chapterId") chapterId: Int,
    ): Observable<BaseCookingListResponse<SingleCourseDesc>>
}