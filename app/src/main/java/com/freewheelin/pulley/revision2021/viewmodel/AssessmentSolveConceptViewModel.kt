package com.freewheelin.pulley.revision2021.viewmodel

import androidx.lifecycle.LifecycleObserver
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository

class AssessmentSolveConceptViewModel private constructor() : BaseViewModel(), LifecycleObserver {
    companion object {
        val instance: AssessmentSolveConceptViewModel by lazy { AssessmentSolveConceptViewModel() }
    }

    val assessmentRepository: AssessmentRepository by lazy { AssessmentRepository.instance }
    val currentProblem by lazy { assessmentRepository.currentProblem }




}