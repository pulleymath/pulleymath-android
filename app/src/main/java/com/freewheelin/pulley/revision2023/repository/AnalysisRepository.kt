package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import android.os.Build
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.request.AnalysisAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import com.freewheelin.pulley.revision2023.service.AnalysisApi
import com.freewheelin.pulley.revision2023.service.AnalysisService
import com.freewheelin.pulley.revision2023.service.LegacyV2Api
import com.freewheelin.pulley.revision2023.service.LegacyV2Service
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.CoroutineScope

class AnalysisRepository() {

    companion object {
        val instance: AnalysisRepository by lazy { AnalysisRepository() }
    }
    private val analysisApi: AnalysisService by lazy { AnalysisApi.analysisService() }


    suspend fun advancedLearningFromAnalysis(req: AnalysisAdvancedLearningRequest): Piece {
        return analysisApi.advancedLearningFromAnalysis(req).data
    }
}