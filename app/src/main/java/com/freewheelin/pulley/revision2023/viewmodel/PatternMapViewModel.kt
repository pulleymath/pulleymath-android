package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.repository.PatternMapRepository
import com.freewheelin.pulley.revision2023.repository.impl.PatternMapRepositoryImpl
import com.freewheelin.pulley.revision2023.ui.adapter.LCPatternMapListAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PatternMapViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val patternMapRepository: PatternMapRepository = PatternMapRepositoryImpl(getApplication<Application>().applicationContext, viewModelScope)

    lateinit var adapter: LCPatternMapListAdapter

    private val _lcPatternMaps = MutableLiveData<List<LCPatternMap>>()
    val lcPatternMaps: LiveData<List<LCPatternMap>> = _lcPatternMaps

    val showNextStepBtn by lazy { MutableLiveData<Boolean>(false) }

    fun initAdapterItem(chapterId: Int) {
        patternMapRepository.run {
            flowAllPatternMap(chapterId)
                .onEach { maps ->
                    _lcPatternMaps.value = maps
                    val showStepBtn = lcPatternMaps.value
                        ?.map { it.isCompleteCard }
                        ?.takeIf { it.isNotEmpty() }
                        ?.reduce { prev, next -> prev || next }

                    showNextStepBtn.postValue(showStepBtn)
                }
                .launchIn(viewModelScope)
        }
        collectAllPatternMaps(chapterId)
    }

    fun collectAllPatternMaps(chapterId: Int) {
        contentJob?.cancel("연속동작 취소", CancellationException())
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val newPatternMaps = fetchAllPatternMaps(chapterId)
            val oldPatternMaps = lcPatternMaps.value?.filterNot { it in newPatternMaps }
            oldPatternMaps?.forEach { deletePatternMap(it) }
//            println("연속동작 딜레이이전")
            delay(300)
//            println("연속동작 딜레이이후")
            upsertPatternMaps(newPatternMaps)
            _isLoading.postValue(false)
        }
    }
    suspend fun fetchAllPatternMaps(chapterId: Int): List<LCPatternMap> {
        return patternMapRepository.fetchPatternMap(chapterId)
    }
    suspend fun upsertPatternMaps(patternMaps: List<LCPatternMap>) {
        patternMapRepository.upsertAll(patternMaps)
    }
    suspend fun deletePatternMap(patternMap: LCPatternMap) {
        patternMapRepository.delete(patternMap)
    }
}