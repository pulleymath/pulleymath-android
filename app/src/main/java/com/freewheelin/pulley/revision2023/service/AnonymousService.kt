package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderWrapper
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object AnonymousApi {
    fun anonymousService(): AnonymousService = Network.retrofit(Network.Type.spring).create(AnonymousService::class.java)
}
interface AnonymousService {

    @GET("anonymous/v2/commerce/plus/android")
    suspend fun getPurchaseGuide(): ResponseForceBody<PurchaseGuide>

    @GET("/anonymous/v1/analysis/sample")
    suspend fun getAnalysisSample(): ResponseForceBody<Analysis>
}