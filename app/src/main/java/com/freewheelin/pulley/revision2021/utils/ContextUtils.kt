package com.freewheelin.pulley.revision2021.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner

object ContextUtils {
}

fun Context.getLifecycleOwner(): LifecycleOwner {
    return try {
        this as LifecycleOwner
    } catch (exception: ClassCastException) {
        (this as ContextWrapper).baseContext as LifecycleOwner
    }
}