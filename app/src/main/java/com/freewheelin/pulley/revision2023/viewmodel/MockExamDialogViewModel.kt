package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.contents.MockExamSummary
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MockExamDialogViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    val showProgress = MutableLiveData<Boolean>(false)
    val mockSummary = MutableLiveData<MockExamSummary?>(MockExamSummary())
    var mockId: Int? = null
    var assignId: Int = -999

    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }

    fun fetchMockSummary(cb: (MockExamSummary?) -> Unit) {
        _isLoading.postValue(true)
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            if (mockId == null || assignId == null) return@launch
            val summary = legacyV2Repository.fetchMockSummary(mockId!!, assignId ?: -999)
            mockSummary.postValue(summary)
            _isLoading.postValue(false)
            withContext(Dispatchers.Main) {
                cb(summary)
            }
        }
    }
}