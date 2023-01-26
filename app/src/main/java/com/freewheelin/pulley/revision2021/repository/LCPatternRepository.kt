package com.freewheelin.pulley.revision2021.repository

import android.content.Context
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.remote.LCPatternApi
import com.freewheelin.pulley.revision2021.repository.remote.LCPatternService
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.room.pattern.PatternDao
import com.freewheelin.pulley.revision2023.room.pattern.PatternDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class LCPatternRepository(val context: Context, private val applicationScope: CoroutineScope) {

    private val patternService: LCPatternService by lazy { LCPatternApi.lcPatternService() }
    private val dao: PatternDao = PatternDatabase.getDatabase(context, applicationScope).patternDao()

    fun patternQuizScoring(patternQuizId: Int, studentId: String, type: String? = "PATTERN_QUIZ", userAnswer: ScoringReq) = patternService.patternQuizScoring(patternQuizId, studentId, type, userAnswer)
    fun usePatternQuizHint(patternQuizId: Int, studentId: String) = patternService.usePatternQuizHint(patternQuizId, studentId)
    suspend fun fetchPatternInfo(patternId: Int): List<LCPatternQuiz> {
        return patternService.fetchPatternInfo(patternId)
            .data

    }

    fun flowAllPatternInfo(patternId: Int): Flow<List<LCPatternQuiz>> {
        return dao.getAllPatternQuiz(patternId)
    }
    suspend fun insert(pattern: LCPatternQuiz) {
        dao.insert(pattern)
    }
    suspend fun upsertAll(patterns: List<LCPatternQuiz>) {
        dao.upsertAll(patterns)
    }
    suspend fun delete(pattern: LCPatternQuiz) {
        dao.delete(pattern)
    }

}