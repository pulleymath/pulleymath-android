package com.freewheelin.pulley.revision2021.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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