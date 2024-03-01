package com.freewheelin.pulley.legacy.model

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.legacy.assets.*
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailyRecommend
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.legacy.utils.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

data class UserV4(
    var fullName: String,
    var cellPhone: String,
    val studentID: String,
    var accountEmail: String,
    var email: String?,
    var parentNumber: String?,
    val serviceType: PaidServiceType,
    val schoolType: SchoolType?,
    var majorType: String,
    var grade: Int,
    var initMoGrade: Int,
    val initStudied: String,
    val canUpdateGrade: Boolean,
    var schoolName: String?,
    var schoolID: Int?,
    var regionID: Int?,
    var regionName: String?,
    val userUniversityMajorCode: String?,
    var agreeMarketing: Boolean,
    var agreeAppPush: Boolean,
    var agreeAlimtalk: Boolean,
    var agreeEmail: Boolean,
    val signInChannel: SignInChannel,
    val isExceedDevice: Boolean,
    var isValidPhone: Boolean,
    var isValidEmail: Boolean,
) {
    var token:String = ""

    var userGrade: Grade
        get() = Grade.init(grade)
        set(value) {
            grade = value.value
            commit("grade")
        }
    val userMajor: Major
        get() = Major.init(majorType)

    val studiedUnit: Set<BigUnitV3>
        get() {
            val ids = initStudied.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
            return ids.mapNotNull { BigUnitV3.initOrNull(it) }
                .toSet()
        }

    fun commit(from:String) {
        val gson = Gson()
        val json = gson.toJson(this)
        Log.d("테스트", "from:${from} set userDataString=${json}")
        Preferences.userDataString.set(json)
    }
}

enum class SignInChannel {
    WHALESPACE, PULLEY, NAVER, APPLE
}