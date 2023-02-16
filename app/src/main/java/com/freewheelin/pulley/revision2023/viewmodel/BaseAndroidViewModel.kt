package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler
import java.net.UnknownHostException
import kotlinx.coroutines.*

open class BaseAndroidViewModel(application: Application): AndroidViewModel(application) {
    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected var contentJob: Job? = null

    protected val contentExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()

        println("throwable : $throwable")
        when (throwable) {
            is CancellationException -> {
                _isLoading.postValue(false)
            }
            is UnknownHostException -> {
                _isLoading.postValue(false)
            }
        }
    }

    protected val compositeDisposable = CompositeDisposable()

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}