package com.freewheelin.pulley.revision2023.room.pattern

import androidx.room.*
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Query("SELECT * FROM lc_pattern_table where patternId=:patternId")
    fun getAllPatternQuiz(patternId: Int): Flow<List<LCPatternQuiz>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pattern: LCPatternQuiz)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(patterns: List<LCPatternQuiz>)

    @Delete
    suspend fun delete(pattern: LCPatternQuiz)

    @Query("SELECT COUNT(*) FROM lc_pattern_table WHERE patternId LIKE :id")
    suspend fun compareInfo(id: Int): Int

}