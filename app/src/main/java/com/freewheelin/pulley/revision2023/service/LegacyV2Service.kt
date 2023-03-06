package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponseWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object LegacyV2Api {
    fun legacyV2Service(): LegacyV2Service = Network.retrofit(Network.Type.spring).create(
        LegacyV2Service::class.java)
}
interface LegacyV2Service {
    @POST("v2/log/user")
    suspend fun postLog(@Body log: V2LogUser): V2LogUserResponseWrapper

}