package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.model.DummyCreatedUser
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.TempToken
import com.freewheelin.pulley.revision2023.service.AuthApi
import com.freewheelin.pulley.revision2023.service.AuthService
import com.freewheelin.pulley.revision2023.service.UserApi
import com.freewheelin.pulley.revision2023.service.UserService

class AuthRepository() {

    companion object {
        val instance: AuthRepository by lazy { AuthRepository() }
    }
    private val api: AuthService by lazy { AuthApi.authService() }

//    private val _user = MutableLiveData<User?>()
//    val user: LiveData<User?> = _user

    suspend fun getTempToken(): TempToken {
        return api.getTempToken().data
    }
}