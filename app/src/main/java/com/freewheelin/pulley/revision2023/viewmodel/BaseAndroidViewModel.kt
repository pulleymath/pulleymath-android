package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler
import java.net.UnknownHostException
import kotlinx.coroutines.*

open class BaseAndroidViewModel(application: Application): AndroidViewModel(application) {
    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected val _errorAction = MutableLiveData<CoroutineExceptionType>()
    val errorAction: LiveData<CoroutineExceptionType> = _errorAction

    protected val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    protected var contentJob: Job? = null

    protected val contentExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        throwable.printStackTrace()

        println("throwable : $throwable")

        when (throwable) {
            is CancellationException -> {
                _isLoading.postValue(false)
                _errorAction.postValue(CoroutineExceptionType.Cancellation)
                println("throwable - CancellationException : ${throwable} / ${throwable.message}")
            }
            is UnknownHostException -> {
                _isLoading.postValue(false)
                _errorAction.postValue(CoroutineExceptionType.UnknownHost)
                println("throwable - UnknownHostException : ${throwable} / ${throwable.message}")
            }
            is NullPointerException -> {
                _errorAction.postValue(CoroutineExceptionType.NullPointer)
                println("throwable - NullPointerException : ${throwable} / ${throwable.message}")
            }
            is retrofit2.HttpException -> {
                println("throwable - HttpException : ${throwable.code()} / ${throwable.message}")
                val type = when (throwable.code()) {
                    400 -> CoroutineExceptionType.HttpException400
                    401 -> CoroutineExceptionType.HttpException401
                    403 -> CoroutineExceptionType.HttpException403
                    502 -> CoroutineExceptionType.HttpException502
                    else -> CoroutineExceptionType.HttpException
                }
                _errorAction.postValue(type)

                val responseStr = throwable.response()?.errorBody()?.string()
                Log.e("BaseAndroidViewModel", "throwable :: response: ${responseStr}, type: ${type}")
                if (responseStr?.startsWith("<html>") == false) {
                    val listType = object : TypeToken<ResponseBody<*>>() {}.type
                    val response: ResponseBody<*> = Gson().fromJson(responseStr, listType)
                    response.message?.let { _errorMessage.postValue(it) }
                }
            }
            else -> {
                CoroutineScope(Dispatchers.Main).launch {
                    responseFailed(getApplication<Application>().applicationContext, throwable)
                }
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

    fun guestException() {
        _errorAction.postValue(CoroutineExceptionType.GuestException)
    }
    fun errorStatusReset() {
        _errorAction.postValue(CoroutineExceptionType.NONE)
    }
}