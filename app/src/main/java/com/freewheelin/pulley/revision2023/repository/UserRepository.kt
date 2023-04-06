package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.model.DummyCreatedUser
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.service.UserApi
import com.freewheelin.pulley.revision2023.service.UserService

class UserRepository() {

    companion object {
        val instance: UserRepository by lazy { UserRepository() }
    }
    private val api: UserService by lazy { UserApi.UserService() }

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    suspend fun getUser(): User {
        return api.getUser().data!!.let {
            println("asoaso - - - - - getUser, ${it.token}")
            _user.postValue(it)
            MyApplication.user = it
            it
        }
    }
    fun updateUser(newUser: User?) {
        _user.postValue(newUser)
    }

    suspend fun createDummyUser(email: String) {
        val dummyUser = DummyCreatedUser(email)
        api.adminCreateUser(dummyUser)
    }
    suspend fun getSignupMessage(): HighlightMessage {
        return api.getSignupMessage().data
    }
}