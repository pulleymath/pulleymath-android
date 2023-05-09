package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import android.os.Build
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.VersionManager
import com.freewheelin.pulley.revision2023.model.V2LogUser
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import com.freewheelin.pulley.revision2023.model.request.UpdateCommonSubjectRequest
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.model.response.RecommendSubjectResponse
import com.freewheelin.pulley.revision2023.service.LegacyV2Api
import com.freewheelin.pulley.revision2023.service.LegacyV2Service
import com.freewheelin.pulley.revision2023.service.MyPageApi
import com.freewheelin.pulley.revision2023.service.MyPageService
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.CoroutineScope
class MyPageRepository() {
//class MyPageRepository(val context: Context, private val applicationScope: CoroutineScope) {
    companion object {
        val instance: MyPageRepository by lazy { MyPageRepository() }
    }
    private val myPageApi: MyPageService by lazy { MyPageApi.myPageService() }


    suspend fun fetchRecommendSubject(): RecommendSubjectResponse {
        return myPageApi.fetchRecommendSubject().data
    }

    fun fetchRecommendSubjectOb() = myPageApi.fetchRecommendSubjectOb()

    suspend fun updateCommonSubject(selectedIds: List<Int>) {
        val chapters = UpdateCommonSubjectRequest(selectedIds)
        myPageApi.updateCommonSubject(chapters)
    }
    fun updateCommonSubjectRx(req: UpdateCommonSubjectRequest) = myPageApi.updateCommonSubjectRx(req)

    suspend fun excludeSubjects(list: List<Int>) {
        val chapters = UpdateCommonSubjectRequest(list)
        myPageApi.excludeSubjects(chapters)

    }
    suspend fun updateRecommends(params: Parameter) {
        myPageApi.updateRecommends(params)

    }
}