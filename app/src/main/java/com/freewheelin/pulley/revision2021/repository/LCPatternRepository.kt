package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.remote.LCPatternApi
import com.freewheelin.pulley.revision2021.repository.remote.LCPatternService

class LCPatternRepository {

    private val patternService: LCPatternService by lazy { LCPatternApi.lcPatternService() }

    fun fetchPatternInfo(patternId: Int, studentId: String) = patternService.fetchPatternInfo(patternId, studentId)
    fun patternQuizScoring(patternQuizId: Int, studentId: String, type: String? = "PATTERN_QUIZ", userAnswer: ScoringReq) = patternService.patternQuizScoring(patternQuizId, studentId, type, userAnswer)
    fun usePatternQuizHint(patternQuizId: Int, studentId: String) = patternService.usePatternQuizHint(patternQuizId, studentId)
}