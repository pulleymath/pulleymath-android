package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.ResponseForceBody
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.UpdateSubjectRequest
import com.freewheelin.pulley.revision2023.model.response.DailyTestRecommendResponse
import com.freewheelin.pulley.revision2023.model.response.RecommendSubjectResponse
import io.reactivex.Observable
import retrofit2.http.*

object MyPageApi {
    fun myPageService(): MyPageService = Network.retrofit(Network.Type.spring).create(
        MyPageService::class.java)
}
interface MyPageService {

    @GET("v3/test/daily/recommend")
    suspend fun fetchDailyTestRecommend(
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseForceBody<DailyTestRecommendResponse>

    @GET("v1/users/{studentId}/recommend/subjects")
    suspend fun fetchRecommendSubject(
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseForceBody<RecommendSubjectResponse>

    @GET("v1/users/{studentId}/recommend/subjects")
    fun fetchRecommendSubjectOb(
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): Observable<ResponseForceBody<RecommendSubjectResponse>>

    @PUT("v1/users/{studentId}/recommend/subjects/common")
    suspend fun updateCommonSubject(
        @Body chapters: UpdateSubjectRequest,
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseBody<Any>

    @PUT("v1/users/{studentId}/recommend/subjects/optional")
    suspend fun updateOptionalSubject(
        @Body chapters: UpdateSubjectRequest,
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseBody<Any>

    @PUT("v1/users/{studentId}/recommend/subjects/exclude")
    suspend fun excludeSubjects(
        @Body chapters: UpdateSubjectRequest,
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseBody<Any>

    @PUT("v1/users/{studentId}/recommend/detail")
    suspend fun updateRecommends(
        @Body params: Parameter,
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseBody<Any>


//    @POST("v2/log/user")
//    suspend fun postLog(@Body log: V2LogUser): V2LogUserResponseWrapper
//
//    @PATCH("v2/me/parent")
//    suspend fun patchParentPhoneNumber(@Body req: ParentPhoneNumberRequest): ResponseBody<String?>

}