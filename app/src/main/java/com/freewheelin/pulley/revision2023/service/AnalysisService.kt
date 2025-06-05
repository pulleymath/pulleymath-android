package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.request.AnalysisAdvancedLearningRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

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