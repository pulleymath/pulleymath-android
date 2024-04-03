package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isSPYMode
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {

    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val alarmRepository by lazy { AlarmRepository() }
    private val userRepository by lazy { UserRepository.instance }
    private val assessmentRepository by lazy { AssessmentRepository.instance }
    val user = userRepository.user
    val mainProfileV4 = userRepository.mainProfileV4
    val assessmentExamGroup = assessmentRepository.assessmentExamGroup
    val assessmentMetadata = assessmentRepository.assessmentMetadata
    val schoolType = userRepository.schoolType
    val joinedChallengeList = challengeRepository.joinedChallengeList
    val showWholeLoading = MutableLiveData<Boolean>(false)
    val schoolSpinnerPosition = MutableLiveData<Int>(null)

    var prevTab: Pair<MainTab, Int> = Pair(MainTab.메인, 0)
    val showDrawer = MutableLiveData<Boolean>(false)
    val showSpy = MutableLiveData<Boolean>(isSPYMode)

    fun setPageProgress(show: Boolean) {
        showWholeLoading.postValue(show)
    }

    fun fetchUser(cb: (UserV4) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val user = userRepository.getUser()
            initSchoolType(user.schoolType ?: SchoolType.HIGH)
//            println("aspasp mainact user.schoolType : ${user.schoolType}")
//            println("aspasp mainact fetchUser user.schoolType?.mainSpinnerPosition : ${user.schoolType?.mainSpinnerPosition}")
            schoolSpinnerPosition.postValue(user.schoolType?.mainSpinnerPosition ?: 2)
            _errorAction.postValue(CoroutineExceptionType.NONE)
            cb(user)
        }
    }
    fun fetchMainProfile() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            userRepository.getMainProfileV4()
        }
    }

    fun fetchUserChallenges() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            challengeRepository.getChallengesOnStatus()
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    fun fetchAssessmentGroupMetadata() {
        val schoolId = user.value?.schoolID ?: return assessmentRepository.clearMetadata()
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val assessmentMetadata = assessmentRepository.fetchAssessmentGroupMetadata(schoolId)
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    fun updateUser(user: UserV4?) {
        userRepository.updateUser(user)
    }

    fun showStartChallengeFinishEffect() : Boolean {
        joinedChallengeList.value?.find { it.isStartChallenge }?.let { sc ->
            val isDone = sc.userStatus == ChallengeUserStatus.DONE
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
    fun toggleDrawer() {
        showDrawer.postValue(showDrawer.value?.not())
    }

    fun checkNewAlarm(cb: (Boolean) -> Unit) {
        // TODO 새 알람 있는지 체크하는 api가 생기면 바꿔야함
        compositeDisposable += alarmRepository.fetchMessages()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "fetchMessages list=>${res.data}")

                var isNewAlarmExist = false
                res.data.forEach {
                    if (!it.isRead) {
                        isNewAlarmExist = true
                        return@forEach
                    }
                }
                cb(isNewAlarmExist)
            }, { error ->
                Log.e(javaClass.simpleName, "fetchMessages error=${error.localizedMessage}")
            })
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
    fun syncSchoolType() {
        if (userRepository.schoolType.value?.mainSpinnerPosition != schoolSpinnerPosition.value) {
            schoolSpinnerPosition.postValue(userRepository.schoolType.value?.mainSpinnerPosition ?: 2)
        }
    }
    fun updateSchoolSpinnerPosition(schoolType: SchoolType) {
        schoolSpinnerPosition.postValue(schoolType.mainSpinnerPosition)
    }

    fun fetchAssessmentExamList () {
        val studentId = user.value?.studentID ?: return assessmentRepository.clearGroupList()
        val schoolId = user.value?.schoolID ?: return assessmentRepository.clearGroupList()
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            assessmentRepository.getGroupList(studentId, schoolId)
        }
    }
}