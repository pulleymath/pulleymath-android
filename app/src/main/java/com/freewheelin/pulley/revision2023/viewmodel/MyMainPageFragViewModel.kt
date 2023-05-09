package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestCard
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import com.freewheelin.pulley.revision2023.model.request.UpdateCommonSubjectRequest
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MyMainPageFragViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val myPageRepository by lazy { MyPageRepository.instance }
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user
    val schoolType = userRepository.schoolType

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()
    val recommendCommonSubjects: LiveData<List<RecommendSubject>> = _recommendCommonSubjects

    private val _recommendOptionalSubjects = MutableLiveData<List<RecommendSubject>>()
    val recommendOptionalSubjects: LiveData<List<RecommendSubject>> = _recommendOptionalSubjects

    fun updateUser(user: User?) {
        userRepository.updateUser(user)
    }

    fun fetchUser(cb: (User) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            withContext(Dispatchers.Main) {
                cb(tempToken.token)
            }
        }
    }
    fun changeParentPhoneNumber(parentNumber: String, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val req = ParentPhoneNumberRequest(parentNumber)
            legacyV2Repository.changeParentPhoneNumber(req)
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }
    fun fetchRecommendSubject() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val res = myPageRepository.fetchRecommendSubject()
            val commonSubjects = res.commonSubjects
            _recommendCommonSubjects.postValue(commonSubjects)
            val optionalSubjects = res.optionalSubjects
            _recommendOptionalSubjects.postValue(optionalSubjects)
        }
    }
    fun updateCommonSubject(selectedIds: List<Int>, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            println("zxozxo onModifyBtnClicked1-2 ")
            myPageRepository.updateCommonSubject(selectedIds)
            println("zxozxo onModifyBtnClicked1-9 ")
            withContext(Dispatchers.Main) {
                println("zxozxo onModifyBtnClicked1-10 ")
                cb()
            }

        }
    }
    fun excludeSubjects(selectedIds: List<Int>, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            myPageRepository.excludeSubjects(selectedIds)
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }
    fun updateRecommends(params: Parameter, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {

            myPageRepository.updateRecommends(params)
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }
}