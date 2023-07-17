package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.WhaleSpaceLoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhaleSpaceLoginViewModel(application: Application): BaseAndroidViewModel(application) {
    private val whaleSpaceLoginRepository = WhaleSpaceLoginRepository(getApplication<Application>().applicationContext, viewModelScope)


    fun sendCode(code: String, cb: (Unit) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val response = whaleSpaceLoginRepository.sendCode(code)
            cb(response)
        }
    }
}