package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.repository.PriorConceptRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.repository.impl.PriorConceptRepositoryImpl
import com.freewheelin.pulley.revision2023.ui.adapter.PriorConceptAdapter
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class WrongNoteFragViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

    var afterFetch = false

    private val _priorConcepts = MutableLiveData<List<PriorConcept>>()
    val priorConcepts: LiveData<List<PriorConcept>> = _priorConcepts

    val showLockIcon = MutableLiveData<Boolean>()
}