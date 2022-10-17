package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


object LearningCourseApi {
    fun learningCourseService() : LearningCourseService  = Network.retrofit(Network.Type.cooking).create(LearningCourseService::class.java)
}
interface LearningCourseService {

    @GET("chapters/{chapterId}")
    fun fetchCourseList(
        @Path("chapterId") chapterId: Int,
    ): Observable<BaseCookingListResponse<SingleCourseDesc>>

    @POST("users/{studentId}/chapters/{chapterId}/timer")
    fun postUserConceptLearningTime(
        @Path("studentId") studentId: String,
        @Path("chapterId") chapterId: Int,
        @Body param: Parameter,
    ): Observable<BaseCookingResponse<Unit?>>
}