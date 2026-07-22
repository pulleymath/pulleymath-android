package com.freewheelin.pulley.legacy.model

import android.util.Log
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.legacy.core.API.ResponseModel.AffiliationInfo
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.google.gson.Gson

data class UserV4(
    var fullName: String,
    var cellPhone: String,
    val studentID: String,
    var accountEmail: String,
    var email: String?,
    var parentNumber: String?,
    val serviceType: PaidServiceType,
    var schoolType: SchoolType?,
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
    var affiliationInfo: AffiliationInfo?
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

    fun commit(from:String) {
        val gson = Gson()
        val json = gson.toJson(this)
        Log.d("테스트", "from:${from} set userDataString=${json}")
        Preferences.userDataString.set(json)
    }
}

enum class SignInChannel {
    WHALESPACE, PULLEY, NAVER, APPLE, AIEP
}

// 웹앱(WebView) 서비스 대상 채널 — 네이티브 Main 대신 AiepWebViewActivity(webapp)로 진입한다
val SignInChannel.usesWebApp: Boolean
    get() = this == SignInChannel.AIEP || this == SignInChannel.WHALESPACE