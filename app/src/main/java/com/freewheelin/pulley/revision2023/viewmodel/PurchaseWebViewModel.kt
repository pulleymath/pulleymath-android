package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.repository.AuthRepository
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PurchaseWebViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            withContext(Dispatchers.Main) {
                cb(tempToken.token)
            }
        }
    }
}