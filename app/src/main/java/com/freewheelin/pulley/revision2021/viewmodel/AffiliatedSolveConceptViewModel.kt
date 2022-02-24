package com.freewheelin.pulley.revision2021.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository

class AffiliatedSolveConceptViewModel private constructor() : BaseViewModel(), LifecycleObserver {
    companion object {
        val instance: AffiliatedSolveConceptViewModel by lazy { AffiliatedSolveConceptViewModel() }
    }

    val affiliatedTestRepository: AffiliatedTestRepository by lazy { AffiliatedTestRepository.instance }
    val currentProblem by lazy { affiliatedTestRepository.currentProblem }

//    val currentProblem by lazy { MutableLiveData<AffiliatedTestProblem>() }



}