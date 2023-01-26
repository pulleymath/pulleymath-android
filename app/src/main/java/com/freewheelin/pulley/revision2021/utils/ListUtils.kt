package com.freewheelin.pulley.revision2021.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner

fun <T> List<T>.replace(t: T): List<T> {
    val newList = this.map {
        if (it == t) {
            t
        } else {
            it
        }
    }
    return newList
}