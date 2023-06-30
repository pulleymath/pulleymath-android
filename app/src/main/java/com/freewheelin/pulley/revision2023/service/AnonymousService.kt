package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.GuestSignInRequest
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.response.GuestSignInResponse
import retrofit2.Call
import retrofit2.http.*

object AnonymousApi {
    fun anonymousService(): AnonymousService = Network.retrofit(Network.Type.spring).create(AnonymousService::class.java)
}
interface AnonymousService {

    @GET("anonymous/v2/commerce/plus")
    suspend fun getPurchaseGuide(
        @Query("os") os: String = "ANDROID",
    ): ResponseForceBody<PurchaseGuide>

    @GET("v1/analysis/{studentId}")
    suspend fun getAnalysis(
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseForceBody<com.freewheelin.pulley.legacy.model.Analysis>

    @GET("/anonymous/v1/analysis/sample")
    suspend fun getAnalysisSample(): ResponseForceBody<com.freewheelin.pulley.legacy.model.Analysis>

    @GET("/anonymous/v2/analysis/sample")
    suspend fun getAnalysisSampleV2(
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseForceBody<com.freewheelin.pulley.legacy.model.Analysis>

    @POST("/anonymous/v1/signin")
    suspend fun guestSignIn(
        @Body guestSignInRequest: GuestSignInRequest
    ): ResponseForceBody<GuestSignInResponse>

    @POST("/anonymous/v1/signup")
    suspend fun guestSignUp(
        @Body req: RequestSignup
    ): ResponseBody<String?>


}