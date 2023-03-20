package com.freewheelin.pulley.revision2023.repository.impl

import android.content.Context
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.repository.PatternMapRepository
import com.freewheelin.pulley.revision2023.room.patternmap.PatternMapDao
import com.freewheelin.pulley.revision2023.room.patternmap.PatternMapDatabase
import com.freewheelin.pulley.revision2023.service.PatternMapApi
import com.freewheelin.pulley.revision2023.service.PatternMapService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class PatternMapRepositoryImpl(val context: Context, private val applicationScope: CoroutineScope): PatternMapRepository {
    private val patternMapApi: PatternMapService by lazy { PatternMapApi.patternMapService() }
    private val dao: PatternMapDao = PatternMapDatabase.getDatabase(context, applicationScope).patternMapDao()

    override suspend fun fetchPatternMap(chapterId: Int): List<LCPatternMap> {
        return patternMapApi.fetchPatternMap(
            chapterId = chapterId
        )
            .data.let {
                val header = LCPatternMap.getHeader(chapterId)
                val cardList = listOf(header) + it
                cardList
            }
    }

    override fun flowAllPatternMap(chapterId: Int): Flow<List<LCPatternMap>> {
        return dao.getAllPriorConcepts(chapterId)
    }

    override suspend fun compareInfo(id: String): Boolean {
        return dao.compareInfo(id) > 0
    }

    override suspend fun insert(patternMap: LCPatternMap) {
        dao.insert(patternMap)
    }

    override suspend fun upsertAll(patternMaps: List<LCPatternMap>) {
        val newMaps = patternMaps.map {
            it.copy(studentId = user?.studentID)
        }
        dao.upsertAll(newMaps)
    }

    override suspend fun delete(patternMap: LCPatternMap) {
        dao.delete(patternMap)
    }
}