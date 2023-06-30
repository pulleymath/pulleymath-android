package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.model.Analysis
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.request.GuestSignInRequest
import com.freewheelin.pulley.revision2023.model.response.GuestSignInResponse
import com.freewheelin.pulley.revision2023.service.*

class AnonymousRepository() {

    companion object {
        val instance: AnonymousRepository by lazy { AnonymousRepository() }
    }
    private val api: AnonymousService by lazy { AnonymousApi.anonymousService() }

//    private val _user = MutableLiveData<User>()
//    val user: LiveData<User> = _user

    suspend fun getPurchaseGuide(): PurchaseGuide {
        return api.getPurchaseGuide().data
    }
    suspend fun getAnalysis(startDate: String, endDate: String): com.freewheelin.pulley.legacy.model.Analysis {
        return api.getAnalysis(startDate = startDate, endDate = endDate).data
    }
    suspend fun getAnalysisSample(): com.freewheelin.pulley.legacy.model.Analysis {
        return api.getAnalysisSampleV2().data
    }
    suspend fun guestSignIn(req: GuestSignInRequest): GuestSignInResponse {
        return api.guestSignIn(req).data
    }
    suspend fun guestSignUp(req: RequestSignup): String? {
        return api.guestSignUp(req).data
    }
}