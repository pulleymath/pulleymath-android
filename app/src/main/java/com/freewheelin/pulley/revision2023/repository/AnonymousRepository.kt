package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.model.Analysis
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
    suspend fun getAnalysisSample(): Analysis {
        return api.getAnalysisSample().data
    }
    suspend fun guestSignIn(req: GuestSignInRequest): GuestSignInResponse {
        return api.guestSignIn(req).data
    }
    suspend fun guestSignUp(req: RequestSignup): String? {
        return api.guestSignUp(req).data
    }
}