package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.service.WhaleSpaceLoginApi
import com.freewheelin.pulley.revision2023.service.WhaleSpaceLoginService
import kotlinx.coroutines.CoroutineScope

class WhaleSpaceLoginRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val whaleSpaceLoginApi: WhaleSpaceLoginService by lazy { WhaleSpaceLoginApi.whaleSpaceLoginService() }


    suspend fun sendCode(code: String): ResponseBody<SignInAppToken> {
        return whaleSpaceLoginApi.sendCode(code)
    }


}