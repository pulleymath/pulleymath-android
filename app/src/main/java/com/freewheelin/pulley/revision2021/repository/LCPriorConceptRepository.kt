package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.LCPriorConceptApi
import com.freewheelin.pulley.revision2021.repository.remote.LCPriorConceptService

class LCPriorConceptRepository {
    private val priorConceptService: LCPriorConceptService by lazy { LCPriorConceptApi.lcPriorConceptService() }

    fun fetchPriorConcept(chapterId: Int, studentId: String) = priorConceptService.getPriorConceptChapters(chapterId, studentId)
    fun completedReview(reviewId: Int, studentId: String) = priorConceptService.getPriorConceptChapters(reviewId, studentId)
}