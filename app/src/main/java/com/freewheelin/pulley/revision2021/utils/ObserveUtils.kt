package com.freewheelin.pulley.revision2021.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ObserveUtils {
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
fun <T> LiveData<List<T>>.observeListOnce(lifecycleOwner: LifecycleOwner, observer: Observer<List<T>>) {
    observe(lifecycleOwner, object : Observer<List<T>> {
        override fun onChanged(t: List<T>?) {
            if (t?.isNotEmpty() == true) {
                observer.onChanged(t)
                removeObserver(this)
            }
        }
    })
}

fun <T> LiveData<T>.observeThrottle(lifecycleOwner: LifecycleOwner, delayTime: Long = 200L, observer: Observer<T>) {
    var throttleJob: Job? = null
    var latestParam: T
    observe(lifecycleOwner) { t ->
        latestParam = t
        if (throttleJob?.isCompleted != false) {
            throttleJob = lifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Default) {
                delay(delayTime)
                observer.onChanged(latestParam)
            }
        }
    }
}
fun <T> throttleLatest(
    intervalMs: Long = 300L,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var throttleJob: Job? = null
    var latestParam: T
    return { param: T ->
        latestParam = param
        if (throttleJob?.isCompleted != false) {
            throttleJob = coroutineScope.launch {
                delay(intervalMs)
                latestParam.let(destinationFunction)
            }
        }
    }
}

fun <T> debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { upstream ->
        val currentTime = System.currentTimeMillis()
        val mayEmit = currentTime - lastEmissionTime > windowDuration
        if (mayEmit)
        {
            lastEmissionTime = currentTime
            emit(upstream)
        }
    }
}