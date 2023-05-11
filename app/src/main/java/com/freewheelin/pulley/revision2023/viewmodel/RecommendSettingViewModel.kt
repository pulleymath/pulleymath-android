package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2023.model.response.DailyTestRecommendResponse
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog
import com.freewheelin.pulley.revision2023.ui.fragment.SnackTestSelectExamRangeFragment.*
import com.freewheelin.pulley.views.DaebakInputSelection
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

    fun updateTestRangeType(type: TestRangeType) {
        testRangeType.postValue(type)
    }

    fun getRecentStudySubjects(info: DailyTestRecommendResponse? = null): List<SubjectV3> {
        val recommendInfo = info ?: userRecommendInfo
        val recentStudiedSubjects = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedSubject(it.recentStudySubjects)
        }
        return recentStudiedSubjects ?: listOf()
    }
    fun getUserSelectedCommonSubjects(info: DailyTestRecommendResponse? = null): List<SubjectV3> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedSubjects = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedSubject(it.userSubjects.commonSubjects)

        }
        return userSelectedSubjects ?: listOf()
    }
    fun getUserSelectedOptionalSubjects(info: DailyTestRecommendResponse? = null): List<SubjectV3> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedSubjects = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedSubject(it.userSubjects.optionalSubjects)
        }
        return userSelectedSubjects ?: listOf()
    }
    fun getRecentStudyBigUnits(info: DailyTestRecommendResponse? = null): List<BigUnitV3> {
        val recommendInfo = info ?: userRecommendInfo
        val recentStudiedBigUnits = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedBigUnit(it.recentStudySubjects)
        }
        return recentStudiedBigUnits ?: listOf()
    }

    fun getUserSelectedCommonBigUnits(info: DailyTestRecommendResponse? = null): List<BigUnitV3> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedUnits = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedBigUnit(it.userSubjects.commonSubjects)
        }
        return userSelectedUnits ?: listOf()
    }
    fun getUserSelectedOptionalBigUnits(info: DailyTestRecommendResponse? = null): List<BigUnitV3> {
        val recommendInfo = info ?: userRecommendInfo
        val userSelectedUnits = recommendInfo?.let {
            return@let convertRecommendSubjectToSelectedBigUnit(it.userSubjects.optionalSubjects)
        }
        return userSelectedUnits ?: listOf()
    }


    fun convertRecommendSubjectToSelectedSubject(list: List<RecommendSubject>): List<SubjectV3>? {
        return list
            .map { it.chapters }
            .reduceOrNull { prev, next ->
                prev + next
            }
            ?.filter { it.isSelected }
            ?.map { BigUnitV3.idOfNonNull(it.chapterId).subject }
    }
    fun convertRecommendSubjectToSelectedBigUnit(list: List<RecommendSubject>): List<BigUnitV3>? {
        return list
            .map { it.chapters }
            .reduceOrNull { prev, next ->
                prev + next
            }
            ?.filter { it.isSelected }
            ?.map { BigUnitV3.idOfNonNull(it.chapterId) }
    }
    fun fetchDailyTestRecommend(cb: () -> Unit) {
        println("zxpzxp - fetchDailyTestRecommend ")
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

    val getSelectedUnits = fun (view: DaebakInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
        return if(view.result.first())
            subject.bigUnits
        else {
            val selectionResult = view.result.subList(1, view.result.size)
            val unitMap = subject.bigUnits.toList().zip(selectionResult)

            unitMap.filter { it.second }.map { it.first }
        }
    }
    fun exitBtn() {
        onExitClickCallback()
    }
}