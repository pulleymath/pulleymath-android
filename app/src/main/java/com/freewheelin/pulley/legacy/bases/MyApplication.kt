package com.freewheelin.pulley.legacy.bases

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
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.AppUsageMonitor
import com.freewheelin.pulley.legacy.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.legacy.core.manage.VersionInfo
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.APHelper
import com.freewheelin.pulley.legacy.utils.APPreference
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.AssessmentDesignSkin
import com.freewheelin.pulley.revision2023.viewmodel.AppViewModel
import com.google.gson.Gson
import net.danlew.android.joda.JodaTimeAndroid

//@HiltAndroidApp
class MyApplication: Application(), LifecycleObserver, LifecycleEventObserver {

    companion object {
        var isTest = false
//        var user:User? = null

        var user:UserV4? = null
        var assessmentDesignSkin: AssessmentDesignSkin? = null
        var schoolType: SchoolType = SchoolType.HIGH
        var token: String? = null
        var isAppFirstLaunch: Boolean = true
    }

    var viewModel: AppViewModel = AppViewModel(this)

    override fun onCreate() {
        super.onCreate()
        APHelper.init(applicationContext)
        APHelper.deviceId() // 디바이스ID 생성 - 랜덤이라 바뀌면 안됨
        APPreference.init(applicationContext, Preferences)

        Preferences.appLaunchCount = Preferences.appLaunchCount + 1

        JodaTimeAndroid.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (Preferences.userDataString.get().isNotEmpty()) {
            val user = Gson().fromJson(Preferences.userDataString.get(), UserV4::class.java)
            viewModel.updateUser(user)
        }
        setSchoolType()
    }
    private fun setSchoolType() {
        val savedSchoolType = Preferences.schoolType.get()
        if (savedSchoolType.isNotEmpty()) {
            schoolType = SchoolType.convertFromStr(savedSchoolType)
            viewModel.updateSchoolType(schoolType)
        }
    }

    fun onAppForeground() {
        val user = user ?: return
        LogUtils.logEvent(this, user, PulleyEvent.ACTIVE)
        Log.d("APP LIFE CYCLE TEST", "그라운드냥?")
        AppUsageMonitor.isForeground = true
        ConceptLearningUsageMonitor.isForeground = true
    }

    fun onAppBackground() {
        Log.d("APP LIFE CYCLE TEST", "백그라운드냥?")

        AppUsageMonitor.postUsageTime()
        AppUsageMonitor.isForeground = false
        ConceptLearningUsageMonitor.isForeground = false
        ConceptLearningUsageMonitor.postConceptLearningTime()
    }

    override fun onTerminate() {
        super.onTerminate()
        APHelper.clear()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                onAppForeground()
                viewModel.fetchUserChallenges()

            }
            Lifecycle.Event.ON_STOP -> {
                onAppBackground()
            }
            else -> {}
        }
    }

}

val user: UserV4?
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

val Application.user: UserV4?
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

var isNeedNewOnBoarding: Boolean
    set(value) {
        Preferences.isNeedNewOnBoarding.set(value)
    }
    get() {
        return Preferences.isNeedNewOnBoarding.get()
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
        return resources.getBoolean(R.bool.isTablet)
    }

val Context.isMobile: Boolean
    get() {
        return resources.getBoolean(R.bool.isMobile)
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