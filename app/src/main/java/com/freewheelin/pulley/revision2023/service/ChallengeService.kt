package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.ResponseListBody
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object ChallengeApi {
    fun challengeService(): ChallengeService = Network.retrofit(Network.Type.spring).create(ChallengeService::class.java)
}
interface ChallengeService {

    @GET("v1/challenges")
    suspend fun getAllChallengeHeaderItem(
    ): MainChallengeHeaderWrapper

    @GET("v1/users/{studentId}/challenges/{challengeId}")
    suspend fun getAllChallengeDetailItem(
        @Path("challengeId") challengeId: Int,
        @Path("studentId") studentId: String = user?.studentID!!,
    ): ResponseBody<Challenge>

    @POST("v1/users/{studentId}/challenges/{challengeId}")
    suspend fun joinChallenge (
        @Path("challengeId") challengeId: Int,
        @Path("studentId") studentId: String = user?.studentID!!
    ): ResponseBody<Challenge>

    @GET("v1/users/{studentId}/user-challenges")
    suspend fun getChallengesOnStatus(
        @Path("studentId") studentId: String = user?.studentID!!,
        @Query("status") status: ChallengeUserStatus?,
    ): ResponseListBody<Challenge>

    @POST("v1/users/{studentId}/user-challenges/{userChallengeId}/redeem")
    suspend fun askForRedeemOfChallenge(
        @Path("userChallengeId") userChallengeId: Int,
        @Path("studentId") studentId: String = user?.studentID!!,
    ): ResponseBody<Challenge>
}