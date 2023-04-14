package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.GuestSignInRequest
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.response.GuestSignInResponse
import retrofit2.http.*

object AnonymousApi {
    fun anonymousService(): AnonymousService = Network.retrofit(Network.Type.spring).create(AnonymousService::class.java)
}
interface AnonymousService {

    @GET("anonymous/v2/commerce/plus/android")
    suspend fun getPurchaseGuide(): ResponseForceBody<PurchaseGuide>

    @GET("/anonymous/v1/analysis/sample")
    suspend fun getAnalysisSample(): ResponseForceBody<Analysis>

    @POST("/anonymous/v1/signin")
    suspend fun guestSignIn(
        @Body guestSignInRequest: GuestSignInRequest
    ): ResponseForceBody<GuestSignInResponse>

    @POST("/anonymous/v1/signup")
    suspend fun guestSignUp(
        @Body req: RequestSignup
    ): ResponseBody<String?>


}