package com.freewheelin.pulley.utils

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
//import com.crashlytics.android.Crashlytics
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.bases.isSPYMode
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.VersionManager
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class PulleyEvent {
    ACTIVE,
    INIT_SETTING,
    MENU_CLICK,
    BUTTON_CLICK,
    INIT_TEST,
    INDUCE,
    DIALOG,
    ERROR,
    PROBLEM_NOT_EXIST;

    companion object {
        fun init(value: String): PulleyEvent {
            return when(value) {
                "ACTIVE" -> return ACTIVE
                "INIT_SETTING" -> return INIT_SETTING
                "BUTTON_CLICK" -> return BUTTON_CLICK
                "MENU_CLICK" -> return MENU_CLICK
                "INIT_TEST" -> return INIT_TEST
                "DIALOG" -> return DIALOG
                "INDUCE" -> return INDUCE
                "ERROR" -> return ERROR
                "PROBLEM_NOT_EXIST" -> return PROBLEM_NOT_EXIST
                else -> {
                    LogUtils.assert(false, "unexpected Case ${value}")
                    return BUTTON_CLICK
                }
            }
        }
    }
}

object LogUtils {

    fun assert(check: Boolean, msg: String? = null) {
        if(!check) {
            if(BuildConfig.DEBUG)
                throw AssertionError(msg)
            else
                FirebaseCrashlytics.getInstance().recordException(AssertionError(msg))
//                Crashlytics.logException(AssertionError(msg))
        }
    }

    fun errorEvent(event: PulleyEvent, user:User?, msg: String? = null, item_category: String? = null, item_name: String? = null, item_value: String? = null) {
        APHelper.getContext()?.let { context ->
            LogUtils.logEvent(context, user, event,)
        }
    }

    fun assert(throwable: Throwable) {
        assert(false, throwable.localizedMessage)
    }


    fun logEvent(context: Context, user: User?, event_name: PulleyEvent, item_category: String? = null
                 , item_name: String? = null, item_value: String? = null) {
        val param: Parameter = Parameter(
                "event_name" to event_name,
                "deviceModel" to Build.MODEL,
                "versionSdk" to Build.VERSION.SDK_INT,
                "versionCode" to VersionManager.appVersionCode
        )

        if (user != null) {
            param["studentID"] = user.studentID
        }

        if(item_category != null) {
            param["item_category"] = item_category
        }

        if(item_name != null) {
            param["item_name"] = item_name
        }

        if(item_value != null) {
            param["item_value"] = item_value
        }

        var logText = if (user != null) {
            "studentID: ${user.studentID}" +
                "\nevent_name: ${event_name}" +
                "\nitem_category: $item_category"
        } else {
            "event_name: ${event_name}" +
                    "\nitem_category: $item_category"
        }

        if (item_name != null)
            logText += "\nitem_name: $item_name"

        if(item_value != null)
            logText += "\nitem_value: $item_value"

        if(Preferences.onLoggingEvent.get()) {
            val toast = Toast.makeText(context, logText, Toast.LENGTH_SHORT)
            toast.show()
        }

        API_V1.logUser(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {}

            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
        })
    }


    fun logSignUpEvent(context: Context, id: String) {
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param("user_id", id)
        }
        if(isSPYMode && Preferences.onLoggingEvent.get()) {
            Toast.makeText(context, "SignUp 이벤트가 생성되었습니다!!!", Toast.LENGTH_SHORT).show()
        }
    }
}