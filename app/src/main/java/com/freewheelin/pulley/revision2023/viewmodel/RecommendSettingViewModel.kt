package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2023.model.response.DailyTestRecommendResponse
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.model.response.SubjectChapter
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog
import com.freewheelin.pulley.revision2023.ui.fragment.SnackTestSelectExamRangeFragment.TestRangeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendSettingViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    lateinit var setStep: (SnackTestRecommendSettingDialog.ViewType) -> Unit
    lateinit var replaceStep: (SnackTestRecommendSettingDialog.ViewType) -> Unit
    lateinit var removeStep: (Fragment) -> Unit
    lateinit var onExitClickCallback: (() -> Unit)

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()
    val recommendCommonSubjects: LiveData<List<RecommendSubject>> = _recommendCommonSubjects

    val testRangeType = MutableLiveData<TestRangeType>(TestRangeType.MyChoice)
    var selectedLevelIndex = 1
    var selectedRangeIndex = 1

    var userRecommendInfo: DailyTestRecommendResponse? = null
    private val _userRecommendInfo = MutableLiveData<DailyTestRecommendResponse>()
    val userRecommendLiveData: LiveData<DailyTestRecommendResponse> = _userRecommendInfo

    val isRecentStudiedRangeEmpty = MutableLiveData<Boolean>(false)
    val isSelectedRecentStudiedRg = MutableLiveData<Boolean>(false)
    var test: Test? = null
    fun updateTestRangeType(type: TestRangeType) {
        testRangeType.postValue(type)
    }

    fun getRecentStudySubjects(info: DailyTestRecommendResponse? = null): List<RecommendSubject> {
        val recommendInfo = info ?: userRecommendInfo
        val recentStudiedSubjects = recommendInfo?.let {
            return@let it.recentStudySubjects
        }
        return recentStudiedSubjects ?: listOf()
    }
    fun getUserSelectedCommonSubjects(info: DailyTestRecommendResponse? = null): List<RecommendSubject> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedSubjects = recommendInfo?.let {
            return@let it.userSubjects.commonSubjects.filter { subject ->
                subject.chapters.any { chapter -> chapter.isSelected }
            }
        }
        return userSelectedSubjects ?: listOf()
    }
    fun getUserSelectedOptionalSubjects(info: DailyTestRecommendResponse? = null): List<RecommendSubject> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedSubjects = recommendInfo?.let {
            return@let it.userSubjects.optionalSubjects.filter { subject ->
                subject.chapters.any { chapter -> chapter.isSelected }
            }
        }
        return userSelectedSubjects ?: listOf()
    }
    fun getRecentStudyBigUnits(info: DailyTestRecommendResponse? = null): List<SubjectChapter> {
        val recommendInfo = info ?: userRecommendInfo
        val recentStudiedBigUnits = recommendInfo?.let {
            return@let it.recentStudySubjects
                .map { it.chapters }
                .reduceOrNull { prev, next ->
                    prev + next
                }
        }
        return recentStudiedBigUnits ?: listOf()
    }


    fun fetchDailyTestRecommend(cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val res = myPageRepository.fetchDailyTestRecommend()
            userRecommendInfo = res
            _userRecommendInfo.postValue(res)
            withContext(Dispatchers.Main) {
                cb()
            }
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
    fun updateRecommends(params: Parameter, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            myPageRepository.updateRecommends(params)
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
//            val optionalSubjects = res.optionalSubjects
//            _recommendOptionalSubjects.postValue(optionalSubjects)
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
    fun exitBtn() {
        onExitClickCallback()
    }
}