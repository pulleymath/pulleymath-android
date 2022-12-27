package com.freewheelin.pulley.utils

import android.os.Handler
import android.os.Looper

class DelayUtils {
}

class DelayDebounce<T> {

    var handler: Handler? = Handler(Looper.getMainLooper())
    var isWaitingExecutionSignal = false

    fun invoke(value: T, waitMs: Long = 300L, callback: (T) -> Unit) {
        if (isWaitingExecutionSignal) {
            handler?.removeCallbacksAndMessages(null)
            handler = null
        } else {
            isWaitingExecutionSignal = true
        }
        handler = handler ?: Handler(Looper.getMainLooper())

        handler?.postDelayed({
            isWaitingExecutionSignal = false
            callback(value)
        }, waitMs)
    }
}