package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

object UserApi {
    fun UserService(): UserService = Network.retrofit(Network.Type.spring).create(UserService::class.java)
}
interface UserService {

    @GET("v3/me/app")
    suspend fun getUser(): ResponseBody<User>

    @POST("admin/spy/signup/user")
    suspend fun adminCreateUser(
        @Body body: DummyCreatedUser
    ): ResponseBody<Unit>
}