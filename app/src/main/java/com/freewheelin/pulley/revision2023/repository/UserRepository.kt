package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.service.ChallengeApi
import com.freewheelin.pulley.revision2023.service.ChallengeService
import com.freewheelin.pulley.revision2023.service.UserApi
import com.freewheelin.pulley.revision2023.service.UserService

class UserRepository() {

    companion object {
        val instance: UserRepository by lazy { UserRepository() }
    }
    private val api: UserService by lazy { UserApi.UserService() }

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    suspend fun getUser(): User {
        return api.getUser().data!!.let {
            _user.postValue(it)
            it
        }
    }
    fun updateUser(newUser: User) {
        _user.postValue(newUser)
    }
}