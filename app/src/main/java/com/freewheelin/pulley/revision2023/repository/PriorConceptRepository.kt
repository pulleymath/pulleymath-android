package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.revision2023.model.PriorConcept
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface PriorConceptRepository {
    suspend fun fetchPriorConcept(chapterId: Int): List<PriorConcept>
    fun createLearningCourse(chapterId: Int): Completable

    fun flowAllPriorConcepts(chapterId: Int): Flow<List<PriorConcept>>
    suspend fun compareInfo(id: String): Boolean
    suspend fun insertPriorConcept(priorConcept: PriorConcept)
    suspend fun upsertAll(concepts: List<PriorConcept>)
    suspend fun deletePriorConcept(priorConcept: PriorConcept)
}