package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.freewheelin.pulley.model.Analysis
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
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
}