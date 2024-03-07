package com.freewheelin.pulley.legacy.utils

//import com.crashlytics.android.Crashlytics
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.legacy.bases.isSPYMode
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.legacy.model.UserV4
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    PROBLEM_NOT_EXIST,
    LOGIN,
    LOGOUT,
    LEARNING_CARD_CLICK; // 학습 관련 카드 클릭

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
                "LEARNING_CARD_CLICK" -> return LEARNING_CARD_CLICK
                "LOGIN" -> return LOGIN
                "LOGOUT" -> return LOGOUT
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

    fun errorEvent(event: PulleyEvent, user:UserV4?, msg: String? = null, item_category: String? = null, item_name: String? = null, item_value: String? = null) {
        APHelper.getContext()?.let { context ->
            LogUtils.logEvent(context, user, event,)
        }
    }

    fun assert(throwable: Throwable) {
        assert(false, throwable.localizedMessage)
    }


    fun logEvent(context: Context, user: UserV4?, event_name: PulleyEvent, item_category: String? = null
                 , item_name: String? = null, item_value: String? = null, cb: (() -> Unit)? = null) {
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
            println("LogUtils :: ${logText}")
        }

        if (BuildConfig.FLAVOR != "beta") {
            item_category?.let { category ->
                FirebaseAnalytics.getInstance(context).logEvent(category) {
                    param("student_id", user?.studentID ?: "student_id_null")
                    param("item_name", item_name ?: "item_name_null")
                    param("item_value", item_value ?: "item_value_null")
                }
            }

            API_V1.logUser(param).enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    cb?.invoke()
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    cb?.invoke()
                }
            })
        } else {
            println("LogUtils :: v1/log/user 발사!")
            cb?.invoke()
        }
    }


    fun logSignUpEvent(context: Context, id: String) {
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param("user_id", id)
        }
        if(isSPYMode && Preferences.onLoggingEvent.get()) {
            try {
                Toast.makeText(context, "SignUp 이벤트가 생성되었습니다!!!", Toast.LENGTH_SHORT).show()
            } catch (e: NullPointerException) {
                Log.e("LogSignUpEvent"," SignUpEvent Toast ERROR")
            }
        }
    }
}