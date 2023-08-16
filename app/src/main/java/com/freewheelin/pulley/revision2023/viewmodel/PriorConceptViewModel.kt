package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.repository.PriorConceptRepository
import com.freewheelin.pulley.revision2023.repository.impl.PriorConceptRepositoryImpl
import com.freewheelin.pulley.revision2023.ui.adapter.PriorConceptAdapter
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class PriorConceptViewModel(application: Application): BaseAndroidViewModel(application) {
    private val priorConceptRepository: PriorConceptRepository = PriorConceptRepositoryImpl(getApplication<Application>().applicationContext, viewModelScope)
    lateinit var priorConceptAdapter: PriorConceptAdapter

    var afterFetch = false

    private val _priorConcepts = MutableLiveData<List<PriorConcept>>()
    val priorConcepts: LiveData<List<PriorConcept>> = _priorConcepts

    fun initAdapterItem(chapterId: Int) {
        priorConceptRepository.run {
            flowAllPriorConcepts(chapterId)
                .onEach { concepts ->
                    _priorConcepts.value = concepts.sortedBy { it.sequence }
                }
                .launchIn(viewModelScope)
        }
        collectAllPriorConcepts(chapterId)
    }
    private fun collectAllPriorConcepts(chapterId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val newConcepts = fetchAllPriorConcepts(chapterId)
            _isLoading.postValue(false)



//            val oldConcepts = priorConcepts.value?.filterNot { it in newConcepts }
//            println("aspasp  - oldConcepts size :${oldConcepts?.size}")
//            oldConcepts?.forEach { deletePriorConcepts(it) }
            upsertPriorConcepts(newConcepts)
            afterFetch = true
            adapterUpdateIfNewConceptSizeZero(newConcepts)
        }
    }

    private fun adapterUpdateIfNewConceptSizeZero(newConcepts: List<PriorConcept>) {
        if (newConcepts.isEmpty()) {
            _priorConcepts.postValue(listOf())
        }
    }

    suspend fun fetchAllPriorConcepts(chapterId: Int): List<PriorConcept> {
        return priorConceptRepository.fetchPriorConcept(chapterId)
    }
    suspend fun upsertPriorConcepts(concepts: List<PriorConcept>) {
        priorConceptRepository.upsertAll(concepts)
    }
    suspend fun deletePriorConcepts(concept: PriorConcept){
        priorConceptRepository.deletePriorConcept(concept)
    }

    fun createLearningCourseOnStudentId(chapterId: Int, callback: () -> Unit) {
        priorConceptRepository.createLearningCourse(chapterId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete { callback() }
            .doOnError {
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${it.localizedMessage}")
            }.subscribe()
    }
}