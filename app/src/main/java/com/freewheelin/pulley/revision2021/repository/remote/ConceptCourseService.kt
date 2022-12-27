package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.CourseSummary
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingListResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path



object ConceptCourseApi {
    fun conceptCourseService() : ConceptCourseService  = Network.retrofit(Network.Type.cooking).create(ConceptCourseService::class.java)
}
interface ConceptCourseService {

    @GET("subjects")
    fun getAvailableSubject(): Observable<BaseCookingListResponse<LCSubject>>

    @GET("subjects/{subjectId}/chapters/users/{studentId}")
    fun getChapterOnSubject(
        @Path("subjectId") subjectId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingListResponse<StudyChapter>>

    @POST("users/{studentId}/chapters/{chapterId}")
    fun createLearningCourse(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String,
    ): Completable

    @GET("users/{studentId}/chapters/{chapterId}/summary")
    fun fetchCourseSummary(
        @Path("chapterId") chapterId: Int,
        @Path("studentId") studentId: String,
    ): Observable<BaseCookingResponse<CourseSummary>>
}