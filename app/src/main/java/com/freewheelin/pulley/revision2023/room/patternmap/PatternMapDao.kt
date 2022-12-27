package com.freewheelin.pulley.revision2023.room.patternmap

import androidx.room.*
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternMapDao {

    @Query("SELECT * FROM lc_pattern_map_table where chapterId=:chapterId")
    fun getAllPriorConcepts(chapterId: Int): Flow<List<LCPatternMap>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patternMap: LCPatternMap)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(patternMaps: List<LCPatternMap>)

    @Delete
    suspend fun delete(patternMap: LCPatternMap)

    @Query("SELECT COUNT(*) FROM lc_pattern_map_table WHERE chapterId LIKE :id")
    suspend fun compareInfo(id: String): Int

}