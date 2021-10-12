package com.freewheelin.pulley.bases

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
//import com.facebook.drawee.backends.pipeline.Fresco
//import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.freewheelin.pulley.core.manage.AppUsageMonitor
import com.freewheelin.pulley.core.manage.VersionInfo
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import com.google.gson.Gson
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid

@HiltAndroidApp
class MyApplication: Application(), LifecycleObserver {

    companion object {
        var isTest = false
//        var user:User? = null

        var user:User? = null
    }

    override fun onCreate() {
        super.onCreate()

        APHelper.init(applicationContext)
        APHelper.deviceId() // 디바이스ID 생성 - 랜덤이라 바뀌면 안됨
        APPreference.init(applicationContext, Preferences)

        Preferences.appLaunchCount = Preferences.appLaunchCount + 1

//        val config = ImagePipelineConfig
//                .newBuilder(applicationContext)
//                .setResizeAndRotateEnabledForNetwork(false)
//                .build()
//        Fresco.initialize(this, config)

        if(!isTest) {
            Realm.init(this)
        }
        JodaTimeAndroid.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        MyApplication.user = Gson().fromJson(Preferences.userDataString.get(), User::class.java)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        val user = user ?: return
        LogUtils.logEvent(this, user, PulleyEvent.ACTIVE)
        Log.d("APP LIFE CYCLE TEST", "그라운드냥?")
        AppUsageMonitor.isForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        Log.d("APP LIFE CYCLE TEST", "백그라운드냥?")

        AppUsageMonitor.addMonitorTimeIfNeed()
        AppUsageMonitor.isForeground = false
    }

    override fun onTerminate() {
        super.onTerminate()
        APHelper.clear()
    }

}

val user: User?
    get() {
        return try {
//            if(MyApplication.user == null) {
//                MyApplication.user = Gson().fromJson(Preferences.userDataString.get(), User::class.java)
//            }
            MyApplication.user
        } catch(e: Exception) {
            LogUtils.assert(e)
            null
        }
    }

val Application.user: User?
    get() {
        return try {
            MyApplication.user
        } catch(e: Exception) {
            LogUtils.assert(e)
            null
        }
    }



val suspendVersionInfo: VersionInfo?
    get() {
        return try {
            Gson().fromJson(Preferences.versionDataString.get(), VersionInfo::class.java)
        } catch (e: Exception) {
            LogUtils.assert(e)
            null
        }
    }
var isNeedOnboarding: Boolean
    set(value) {
        Preferences.isNeedOnboarding.set(value)
    }
    get() {
        return Preferences.isNeedOnboarding.get()
    }
var isSPYMode: Boolean
    set(value) {
        Preferences.isSpyMode.set(value)
    }
    get() {
        return Preferences.isSpyMode.get()
    }

val Context.isNetworkConnected: Boolean
    get() {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

val Context.is10InchUI: Boolean
    get() {
        val config = resources.configuration
        return (config.screenWidthDp >= 1280)
    }

val Context.isTablet: Boolean
    get() {
        val config = resources.configuration
        return (config.smallestScreenWidthDp >= 600)
    }

val Context.underMinHeight:Boolean
    get() {
        val config = resources.configuration
        return (config.screenHeightDp <= 680)
    }

enum class DensityLevel {
    Low, Mid, High
}

val Context.densityLevel: DensityLevel
    get() {
        val config = resources.configuration
        return when {
            config.densityDpi <= 160 -> DensityLevel.Low
            config.densityDpi >= 240 -> DensityLevel.High
            else -> DensityLevel.Mid
        }
    }

val Context.isMobileUI: Boolean
    get() {
        return (resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE
    }

fun Context.vibrate(period: Long = 300) {
    val vibrator = (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
        vibrator.vibrate(VibrationEffect.createOneShot(period, VibrationEffect.DEFAULT_AMPLITUDE))
    else
        vibrator.vibrate(period)
}

fun Context.hideKeyboard(view: View) {
    val imm = (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}