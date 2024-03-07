package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfileV4
import com.freewheelin.pulley.legacy.model.DummyCreatedUser
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.HighlightMessage
import com.freewheelin.pulley.revision2023.model.request.ChangeEmailRequest
import com.freewheelin.pulley.revision2023.model.response.MainWeeklyStudySummary
import com.freewheelin.pulley.revision2023.service.UserApi
import com.freewheelin.pulley.revision2023.service.UserService
import org.joda.time.LocalDate

class UserRepository() {

    companion object {
        val instance: UserRepository by lazy { UserRepository() }
    }
    private val api: UserService by lazy { UserApi.UserService() }

    private val _user = MutableLiveData<UserV4?>()
    val user: LiveData<UserV4?> = _user

    private val _mainProfileV4 = MutableLiveData<MainProfileV4>()
    val mainProfileV4: LiveData<MainProfileV4> = _mainProfileV4

    private val _mainProfile = MutableLiveData<MainProfile>()
    val mainProfile: LiveData<MainProfile> = _mainProfile

    private val _schoolType = MutableLiveData<SchoolType>()
    val schoolType: LiveData<SchoolType> = _schoolType

    suspend fun getUser(): UserV4 {
        return api.getUserV4().data!!.let {
            println("asoaso - - - - - getUser, ${it.token}")
            _user.postValue(it)
            MyApplication.user = it
            it
        }
    }
    suspend fun changeEmail(req: ChangeEmailRequest): Nothing? {
        return api.changeEmail(req).data
    }
    fun updateUser(newUser: UserV4?) {
        _user.postValue(newUser)
    }
    fun initSchoolType(type: SchoolType) {
        if (_schoolType.value == null) {
            _schoolType.postValue(type)
        }
    }
    fun updateSchoolType(type: SchoolType) {
        _schoolType.postValue(type)
    }

    suspend fun createDummyUser(email: String) {
        val dummyUser = DummyCreatedUser(email)
        api.adminCreateUser(dummyUser)
    }
    suspend fun getSignupMessage(): HighlightMessage {
        return api.getSignupMessage().data
    }
    suspend fun getMainProfile(): MainProfile {
        return api.getProfiles().data.let {
            _mainProfile.postValue(it)
            it
        }
    }
    suspend fun getMainProfileV4(): MainProfileV4 {
        return api.getRenewProfiles().data.let {
            _mainProfileV4.postValue(it)
            it
        }
    }
    suspend fun requestRewardSignUp() {
        api.requestRewardSignUp()
    }
    suspend fun getWeeklyStudySummary(todayDate: LocalDate): MainWeeklyStudySummary {
        val date = todayDate.toString("yyyy-MM-dd")
        return api.getWeeklyStudySummary(date).data
    }
    fun refreshToken() = api.refreshToken()
}