package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.Analysis
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate

class AnalysisTabActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    private val anonymousRepository by lazy { AnonymousRepository.instance }

    val isSampleLiveData by lazy { MutableLiveData(false) }

    var isSample = false

    fun fetchAnalysis(from: LocalDate, to: LocalDate, formerDate: LocalDate, cb: (Analysis) -> Unit) {
        if (isSample) {
            contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
                val analysis = anonymousRepository.getAnalysisSample()
                withContext(Dispatchers.Main) {
                    cb(analysis)
                }
            }
        } else {
            user!!.getAnalysis(getApplication<Application>().applicationContext, from.toDate(), to.toDate(), formerDate.toDate()) { analysis ->
                analysis?.let { cb(it) }
            }
        }
    }
}