package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.utils.responseFailed
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler
import java.net.UnknownHostException
import kotlinx.coroutines.*
import retrofit2.HttpException

open class BaseAndroidViewModel(application: Application): AndroidViewModel(application) {
    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected var contentJob: Job? = null

    protected val contentExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        throwable.printStackTrace()

        println("throwable : $throwable")

        when (throwable) {
            is CancellationException -> {
                _isLoading.postValue(false)
                println("throwable CancellationException : ${throwable} / ${throwable.message}")
            }
            is UnknownHostException -> {
                _isLoading.postValue(false)
                println("throwable UnknownHostException : ${throwable} / ${throwable.message}")
            }
            is NullPointerException -> {
                println("throwable NullPointerException : ${throwable} / ${throwable.message}")
            }
            is retrofit2.HttpException -> {
                println("throwable HttpException 1 : ${throwable.code()} / ${throwable.message}")
            }
            else -> {
                responseFailed(getApplication<Application>().applicationContext, throwable)
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