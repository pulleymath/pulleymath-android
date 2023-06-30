package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.model.Analysis
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.request.AnalysisAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.repository.*
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate

class AnalysisTabActViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    private val anonymousRepository by lazy { AnonymousRepository.instance }
    private val analysisRepository by lazy { AnalysisRepository.instance }

    val schoolType = userRepository.schoolType
    val isSampleLiveData by lazy { MutableLiveData(false) }

    var isSample = false

    fun fetchAnalysis(from: LocalDate, to: LocalDate, formerDate: LocalDate, cb: (com.freewheelin.pulley.legacy.model.Analysis) -> Unit) {
        if (isSample) {
            contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
                val analysis = anonymousRepository.getAnalysisSample()
                withContext(Dispatchers.Main) {
                    cb(analysis)
                }
            }
        } else {
            contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
                val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
                val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
                val analysis = anonymousRepository.getAnalysis(startDate, endDate)
                withContext(Dispatchers.Main) {
                    cb(analysis)
                }
            }
        }
    }

    fun setAdvancedLearning(req: AnalysisAdvancedLearningRequest, cb: (Piece) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {

            val res = analysisRepository.advancedLearningFromAnalysis(req)
            withContext(Dispatchers.Main) {
                cb(res)
            }
        }

    }
}