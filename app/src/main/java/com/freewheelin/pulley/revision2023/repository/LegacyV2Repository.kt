package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import android.os.Build
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.legacy.model.contents.MockExamSummary
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import com.freewheelin.pulley.revision2023.service.LegacyV2Api
import com.freewheelin.pulley.revision2023.service.LegacyV2Service
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.CoroutineScope

class LegacyV2Repository(val context: Context, private val applicationScope: CoroutineScope) {
    private val legacyV2Api: LegacyV2Service by lazy { LegacyV2Api.legacyV2Service() }

    suspend fun postLog(
            event: PulleyEvent,
            itemCategory: String? = null,
            itemName: String? = null,
            itemValue: String? = null,
            itemNote: String? = null): V2LogUserResponse {

        val log = V2LogUser(
            studentID = user?.studentID!!,
            eventName = event,
            itemCategory = itemCategory,
            itemName = itemName,
            itemValue = itemValue,
            itemNote = itemNote,
            deviceModel = Build.MODEL,
            versionSdk = "${Build.VERSION.SDK_INT}",
            versionCode = VersionManager.appVersion,
        )
        return legacyV2Api.postLog(log).data
    }

    suspend fun changeParentPhoneNumber(req: ParentPhoneNumberRequest) {
        legacyV2Api.patchParentPhoneNumber(req)
    }
    suspend fun fetchMockSummary(mockId: Int, assignId: Int): MockExamSummary? {
        return legacyV2Api.fetchMockSummary(mockId, assignId).data
    }
}