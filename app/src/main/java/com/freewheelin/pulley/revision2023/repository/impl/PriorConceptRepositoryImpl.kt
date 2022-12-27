package com.freewheelin.pulley.revision2023.repository.impl

import android.content.Context
import com.freewheelin.pulley.revision2023.room.priorconcept.PriorConceptDao
import com.freewheelin.pulley.revision2023.room.priorconcept.PriorConceptDatabase
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.repository.PriorConceptRepository
import com.freewheelin.pulley.revision2023.service.PriorConceptApi
import com.freewheelin.pulley.revision2023.service.PriorConceptService
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class PriorConceptRepositoryImpl(val context: Context, private val applicationScope: CoroutineScope): PriorConceptRepository {
    private val priorConceptApi: PriorConceptService by lazy { PriorConceptApi.priorConceptService() }
    private val dao: PriorConceptDao = PriorConceptDatabase.getDatabase(context, applicationScope).priorConceptDao()
//    private val priorConceptDao: PriorConceptDao = PriorConceptDatabase().priorConceptDao()

    override suspend fun fetchPriorConcept(chapterId: Int): List<PriorConcept> {
        return priorConceptApi.getPriorConceptChapters(
            chapterId = chapterId
        )
            .data
    }
    override fun createLearningCourse(chapterId: Int): Completable {
        return priorConceptApi.createLearningCourse(chapterId = chapterId)
    }

    override fun flowAllPriorConcepts(chapterId: Int): Flow<List<PriorConcept>> {
        return dao.getAllPriorConcepts(chapterId)
    }

    override suspend fun compareInfo(id: String): Boolean {
        return dao.compareInfo(id) > 0
    }

    override suspend fun insertPriorConcept(priorConcept: PriorConcept) {
        dao.insertPriorConcept(priorConcept)
    }

    override suspend fun upsertAll(concepts: List<PriorConcept>) {
        dao.upsertAll(concepts)
    }

    override suspend fun deletePriorConcept(priorConcept: PriorConcept) {
        dao.deletePriorConcept(priorConcept)
    }

}