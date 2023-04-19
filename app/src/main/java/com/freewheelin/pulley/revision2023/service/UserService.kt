package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("v2/info/messages/signup")
    suspend fun getSignupMessage(): ResponseForceBody<HighlightMessage>

    @GET("v3/profiles")
    suspend fun getProfiles() : ResponseForceBody<MainProfile>

    @POST("v1/users/{studentId}/rewards/signup")
    suspend fun requestRewardSignUp(
        @Path("studentId") studentId: String = user?.studentID ?: "dummyStudentId",
    ): ResponseBody<Unit?>
}