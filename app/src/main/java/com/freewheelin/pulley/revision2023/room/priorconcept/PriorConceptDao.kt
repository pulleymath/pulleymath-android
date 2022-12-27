package com.freewheelin.pulley.revision2023.room.priorconcept

import androidx.room.*
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface PriorConceptDao {

    @Query("SELECT * FROM lc_prior_concept_table where chapterId=:chapterId")
    fun getAllPriorConcepts(chapterId: Int): Flow<List<PriorConcept>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriorConcept(priorConcept: PriorConcept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(concepts: List<PriorConcept>)

    @Delete
    suspend fun deletePriorConcept(priorConcept: PriorConcept)

    @Query("DELETE FROM lc_prior_concept_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lc_prior_concept_table WHERE learningCoursePriorConceptId LIKE :id")
    suspend fun compareInfo(id: String): Int

}