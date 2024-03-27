package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.revision2021.model.response.base.BaseListResponse
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.UserPlanRequest
import com.freewheelin.pulley.revision2023.model.response.UserPlanResponse
import com.freewheelin.pulley.revision2023.model.response.MainWeeklyPlanResponse
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object PlannerApi {
    fun plannerService(): PlannerService = Network.retrofit(Network.Type.spring).create(PlannerService::class.java)
}
class MainUserPlanResponse : BaseListResponse<MainWeeklyPlanResponse>()

interface PlannerService {

    @GET("v1/users/plans")
    suspend fun getWeeklyPlans(
        @Query("from") from: String,
        @Query("to") to: String,
    ) : ResponseBody<List<MainWeeklyPlanResponse>>

    @GET("v1/users/plans")
    fun getWeeklyPlansOb(
        @Query("from") from: String,
        @Query("to") to: String
    ) : Observable<MainUserPlanResponse>

    @POST("v1/users/plans")
    suspend fun postUserPlan (
        @Body req: UserPlanRequest
    ): ResponseForceBody<UserPlanResponse>

    @DELETE("v1/users/plans/{dailyPlanId}/{itemId}")
    suspend fun deleteUserPlan (
        @Path("dailyPlanId") dailyPlanId: Int,
        @Path("itemId") itemId: Int
    ): ResponseBody<Nothing>

    @GET("v1/planner/workbooks")
    suspend fun fetchStudyPlanOnSubject(
        @Query("subjectId") subjectId: Int,
    ) : ResponseListBody<StudyPlannerItem>

    @GET("v1/users/plans/summary")
    suspend fun fetchUserPlanList(
        @Query("from") from: String,
        @Query("to") to: String,
    ) : ResponseListBody<WeeklyPlanResponse>


}