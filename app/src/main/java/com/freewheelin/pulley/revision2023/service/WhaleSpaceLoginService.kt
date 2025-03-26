package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import retrofit2.http.POST
import retrofit2.http.Query

object WhaleSpaceLoginApi {
    fun whaleSpaceLoginService(): WhaleSpaceLoginService = Network.retrofit(Network.Type.spring).create(
        WhaleSpaceLoginService::class.java)
}
interface WhaleSpaceLoginService {

    @POST("v1/signin/oauth2/whalespace")
    suspend fun sendCode(
        @Query("code") code: String
    ): ResponseBody<SignInAppToken>

}