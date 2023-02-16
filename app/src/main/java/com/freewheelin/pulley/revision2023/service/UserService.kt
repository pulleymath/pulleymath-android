package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.ResponseListBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderWrapper
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object UserApi {
    fun UserService(): UserService = Network.retrofit(Network.Type.spring).create(UserService::class.java)
}
interface UserService {

    @GET("v3/me/app")
    suspend fun getUser(): ResponseBody<User>
}