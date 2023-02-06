package com.freewheelin.pulley.utils

import android.app.Activity
import android.os.Build
import android.util.Log
import java.io.Serializable

class VersionUtils {
}

fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
{
    var result: T? = null
    try {
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.intent.getSerializableExtra(name, clazz)!!
        } else {
            activity.intent.getSerializableExtra(name) as T
        }
    } catch (e: NullPointerException) {
        Log.e("VersionUtils", "error: ${e.localizedMessage}" )
    }
    return result ?: activity.intent.getSerializableExtra(name) as T
}