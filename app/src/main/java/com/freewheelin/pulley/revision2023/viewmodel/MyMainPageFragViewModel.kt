package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.request.ChangeEmailRequest
import com.freewheelin.pulley.revision2023.model.request.ParentPhoneNumberRequest
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyMainPageFragViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val myPageRepository by lazy { MyPageRepository.instance }
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user
    val schoolType = userRepository.schoolType
    val joinedChallengeList = challengeRepository.joinedChallengeList

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()
    val recommendCommonSubjects: LiveData<List<RecommendSubject>> = _recommendCommonSubjects

    private val _recommendOptionalSubjects = MutableLiveData<List<RecommendSubject>>()
    val recommendOptionalSubjects: LiveData<List<RecommendSubject>> = _recommendOptionalSubjects

//    var startChallengeCouponStatus: String? = null
    val startChallengeCouponStatus = MutableLiveData<String>(null)

    fun updateUser(user: UserV4?) {
        userRepository.updateUser(user)
    }

    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            cb(user)
        }
    }
    fun changeEmail(email: String, confirmCode: String, cb:() -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val req = ChangeEmailRequest(auth = confirmCode, email = email)
            val nothing = userRepository.changeEmail(req)
            withContext(Dispatchers.Main) {
                cb()
            }
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
            myPageRepository.updateCommonSubject(selectedIds)
            withContext(Dispatchers.Main) {
                cb()
            }
        }
    }
    fun updateOptionalSubject(selectedIds: List<Int>, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            myPageRepository.updateOptionalSubject(selectedIds)
            withContext(Dispatchers.Main) {
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
    var userChallengeId: Int? = null
    fun stopChallenge(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            userChallengeId?.let {
                challengeRepository.stopChallenge(it)
                withContext(Dispatchers.Main) {
                    cb()
                }
            }
        }
    }

    fun askForRedeemOfChallenge(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            userChallengeId?.let {
                val res = challengeRepository.askForRedeemOfChallenge(it)
                _errorAction.postValue(CoroutineExceptionType.NONE)
                if (res.error != null) {
                    println("[[[[[ERROR askForRedeemOfChallenge]]]]]")
                    responseFailed(
                        getApplication<Application>().applicationContext,
                        Throwable("${res.error} ${res.message}")
                    )
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    cb()
                }
                res.data?.let {
                    challengeRepository.updateChallengeList(it)
                }
            }
        }
    }
}