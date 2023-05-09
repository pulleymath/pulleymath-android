package com.freewheelin.pulley.revision2021.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
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

fun Context.getStatusBarHeight(): Int {
    val screenSizeType = this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    var statusbar = 0
    if (screenSizeType != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
        val resourceId =
            this.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbar = this.resources.getDimensionPixelSize(resourceId)
        }
    }
    return statusbar
}