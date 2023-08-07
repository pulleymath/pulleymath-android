package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfileV4
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.model.request.ChangeEmailRequest
import com.freewheelin.pulley.revision2023.model.response.MainWeeklyStudySummary
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object UserApi {
    fun UserService(): UserService = Network.retrofit(Network.Type.spring).create(UserService::class.java)
}
interface UserService {

    @GET("v3/me/app")
    suspend fun getUser(): ResponseBody<User>
    @GET("v4/me/app")
    suspend fun getUserV4(): ResponseBody<UserV4>
    @PATCH("v4/me/email")
    suspend fun changeEmail(
        @Body req: ChangeEmailRequest
    ): ResponseBody<Nothing>

    @POST("admin/spy/signup/user")
    suspend fun adminCreateUser(
        @Body body: DummyCreatedUser
    ): ResponseBody<Unit>

    @GET("v2/info/messages/signup")
    suspend fun getSignupMessage(): ResponseForceBody<HighlightMessage>

    @GET("v3/profiles")
    suspend fun getProfiles() : ResponseForceBody<MainProfile>

    @GET("v1/users/main/profile")
    suspend fun getRenewProfiles() : ResponseForceBody<MainProfileV4>

    @POST("v1/users/{studentId}/rewards/signup")
    suspend fun requestRewardSignUp(
        @Path("studentId") studentId: String = user?.studentID ?: "dummyStudentId",
    ): ResponseBody<Unit?>

    @GET("v1/users/main/study-summary")
    suspend fun getWeeklyStudySummary(
        @Query("today") todayDate: String
    ) : ResponseForceBody<MainWeeklyStudySummary>

//
//    @GET("v1/users/plans")
//    suspend fun getWeeklyPlans(
//        @Query("from") from: String,
//        @Query("to") to: String,
//    ) : ResponseNullableListBody<MainWeeklyPlanResponse>
//    @GET("v1/planner/workbooks")
//    suspend fun fetchPlanOnSubject(
//        @Query("subjectId") subjectId: Int,
//    ) : ResponseListBody<PlanTreeItemResponse>
//
//    @GET("v1/users/plans/summary")
//    suspend fun fetchWeeklyPlanSummary(
//        @Query("from") from: String,
//        @Query("to") to: String,
//    ) : ResponseListBody<WeeklyPlanResponse>
}