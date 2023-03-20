package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.TempToken
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

object AuthApi {
    fun authService(): AuthService = Network.retrofit(Network.Type.spring).create(AuthService::class.java)
}
interface AuthService {


    @POST("v1/auth/token/temp")
    suspend fun getTempToken(
    ): ResponseForceBody<TempToken>
}