package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.revision2023.model.LCPatternMap
import kotlinx.coroutines.flow.Flow

interface PatternMapRepository {

    suspend fun fetchPatternMap(chapterId: Int): List<LCPatternMap>

    fun flowAllPatternMap(chapterId: Int): Flow<List<LCPatternMap>>
    suspend fun compareInfo(id: String): Boolean
    suspend fun insert(patternMap: LCPatternMap)
    suspend fun upsertAll(patternMaps: List<LCPatternMap>)
    suspend fun delete(patternMap: LCPatternMap)
}