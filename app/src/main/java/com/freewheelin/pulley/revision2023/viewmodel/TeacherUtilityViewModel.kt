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

class TeacherUtilityViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType


    lateinit var onExitClickCallback: (() -> Unit)

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()

    fun exitBtn() {
        onExitClickCallback()
    }
}