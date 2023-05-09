package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.ResponseForceBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponseWrapper
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

object SolveApi {
    fun solveService(): SolveService = Network.retrofit(Network.Type.spring).create(
        SolveService::class.java)
}
interface SolveService {

    @POST("v3/test/start")
    suspend fun getDailyTest(
        @Query("type") testType: String,
        @Query("schoolType") school: String? = schoolType.name,
    ): ResponseForceBody<Test>

}