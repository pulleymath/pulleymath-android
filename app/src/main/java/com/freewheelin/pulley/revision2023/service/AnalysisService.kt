package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponseWrapper
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.request.AnalysisAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

object AnalysisApi {
    fun analysisService(): AnalysisService = Network.retrofit(Network.Type.spring).create(
        AnalysisService::class.java)
}
interface AnalysisService {
    @POST("v1/advanced/chapters")
    suspend fun advancedLearningFromAnalysis(
        @Body req: AnalysisAdvancedLearningRequest,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): ResponseForceBody<Piece>

//
//    @PATCH("v2/me/parent")
//    suspend fun patchParentPhoneNumber(@Body req: ParentPhoneNumberRequest): ResponseBody<String?>

}