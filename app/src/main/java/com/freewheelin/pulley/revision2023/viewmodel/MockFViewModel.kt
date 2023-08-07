package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MockFViewModel(application: Application): BaseAndroidViewModel(application) {

    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    var newMockFragmentProgressHidePending = false

    var mockOrgList = MutableLiveData<List<MockExam>>()
    var filteredMockList = MutableLiveData<List<MockExam>>()

    val yearSelectedPosition = MutableLiveData(0)
    val monthSelectedPosition = MutableLiveData(0)
    val gradeSelectedPosition = MutableLiveData(0)
    val typeSelectedPosition = MutableLiveData(0)

    var year = mutableMapOf<String, String>()
    var month = mutableMapOf<String, String>()
    var grade = mutableMapOf<String, String>()
    var type = mutableMapOf<String, String>()

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }

    val filteredItemText = MutableLiveData<String>()
    var filteredItemList = mutableListOf<String>()
    fun filter() {
        mockOrgList.value?.let { orgList ->
            var result = orgList
            filteredItemList.clear()

            val selectedYear = year.keys.toList().get(yearSelectedPosition.value ?: 0)
            if (selectedYear != "출제 연도 전체") {
                result = result.filter {"${it.year}년" == selectedYear }
                filteredItemList.add(selectedYear)
            }
            val selectedMonth = month.keys.toList().get(monthSelectedPosition.value ?: 0)
            if (selectedMonth != "출제월 전체") {
                result = result.filter {"${it.month}월" == selectedMonth }
                filteredItemList.add(selectedMonth)
            }
            val selectedGrade = grade.keys.toList().get(gradeSelectedPosition.value ?: 0)
            if (selectedGrade != "학년 전체") {
                result = result.filter {"고${it.grade}" == selectedGrade }
                filteredItemList.add(selectedGrade)
            }
            val selectedType = type.keys.toList().get(typeSelectedPosition.value ?: 0)
            if (selectedType != "계열 전체") {
                result = result.filter {it.type.getStr() == selectedType }
                filteredItemList.add(selectedType)
            }

            filteredMockList.postValue(result)

            val filterText = filteredItemList.joinToString(", ")
            filteredItemText.postValue(filterText)
        }
    }
}