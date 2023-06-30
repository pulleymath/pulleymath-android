package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2023.model.request.UpdateSubjectRequest
import com.freewheelin.pulley.revision2023.model.response.DailyTestRecommendResponse
import com.freewheelin.pulley.revision2023.model.response.RecommendSubjectResponse
import com.freewheelin.pulley.revision2023.service.MyPageApi
import com.freewheelin.pulley.revision2023.service.MyPageService

class MyPageRepository() {
//class MyPageRepository(val context: Context, private val applicationScope: CoroutineScope) {
    companion object {
        val instance: MyPageRepository by lazy { MyPageRepository() }
    }
    private val myPageApi: MyPageService by lazy { MyPageApi.myPageService() }


    suspend fun fetchDailyTestRecommend(): DailyTestRecommendResponse {
        return myPageApi.fetchDailyTestRecommend().data
    }
    suspend fun fetchRecommendSubject(): RecommendSubjectResponse {
        return myPageApi.fetchRecommendSubject().data
    }

    fun fetchRecommendSubjectOb() = myPageApi.fetchRecommendSubjectOb()

    suspend fun updateCommonSubject(selectedIds: List<Int>) {
        val chapters = UpdateSubjectRequest(selectedIds)
        myPageApi.updateCommonSubject(chapters)
    }
    suspend fun updateOptionalSubject(selectedIds: List<Int>) {
        val chapters = UpdateSubjectRequest(selectedIds)
        myPageApi.updateOptionalSubject(chapters)
    }

    suspend fun excludeSubjects(list: List<Int>) {
        val chapters = UpdateSubjectRequest(list)
        myPageApi.excludeSubjects(chapters)

    }
    suspend fun updateRecommends(params: Parameter) {
        myPageApi.updateRecommends(params)

    }
}