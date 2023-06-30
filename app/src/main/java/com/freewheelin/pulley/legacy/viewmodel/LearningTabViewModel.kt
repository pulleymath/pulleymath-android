package com.freewheelin.pulley.legacy.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LearningTabViewModel(application: Application): BaseAndroidViewModel(application) {

    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType
    val showWholeLoading = MutableLiveData<Boolean>(false)

    fun setPageProgress(show: Boolean) {
        showWholeLoading.postValue(show)
    }
    fun fetchUser(cb: (User) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            initSchoolType(user.rawSchoolType ?: SchoolType.HIGH)
            _errorAction.postValue(CoroutineExceptionType.NONE)
            cb(user)
        }
    }
    fun fetchUserChallenges() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            challengeRepository.getChallengesOnStatus()
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    fun showStartChallengeFinishEffect() : Boolean {
        joinedChallengeList.value?.find { it.isStartChallenge }?.let { sc ->
            val isDone = sc.userStatus == ChallengeUserStatus.DONE
            println("asoaso - isDone : ${isDone}, sc.remainRewardsCount > 0 :${sc.remainRewardsCount} , ${sc.remainRewardsCount > 0}")
            return isDone && sc.remainRewardsCount > 0 && !sc.finishEffectAlreadyAppear
        }
        return false
    }
    fun updateChallengeFinishFlag() {
        joinedChallengeList.value?.find { it.isStartChallenge }?.let { sc ->
            val isDone = sc.userStatus == ChallengeUserStatus.DONE
            if (isDone && sc.remainRewardsCount > 0) {
                sc.finishEffectAlreadyAppear = true
            }
        }
    }
    fun putFcmToken() {
        if(MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    compositeDisposable += API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({ _ ->
                            Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
                        }, {
                            Log.e(javaClass.simpleName, "putFcmToken ERROR")
                        })
                }
            })
        }
    }
    fun initSchoolType(level: SchoolType) {
        userRepository.initSchoolType(level)
    }
    fun updateSchoolType(level: SchoolType) {
        userRepository.updateSchoolType(level)
    }
}