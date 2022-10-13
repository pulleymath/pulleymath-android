package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.remote.LCCookingApi
import com.freewheelin.pulley.revision2021.repository.remote.LCCookingService

class LCCookingRepository {
    private val cookingService: LCCookingService by lazy { LCCookingApi.lcCookingService() }

    fun fetchCookingGroceries(conceptCookingId: Int, studentId: String) = cookingService.fetchCookingGroceries(conceptCookingId, studentId)
    fun useHint(exerciseQuizId: Int, studentId: String) = cookingService.useHint(exerciseQuizId, studentId)
    fun scoringCookingQuiz(exerciseQuizId: Int, studentId: String, userAnswer: ScoringReq) = cookingService.scoringCookingQuiz(exerciseQuizId, studentId, userAnswer)
}