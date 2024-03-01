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

class User {
//    val name: String
//        get() = fullName
    var firstName: String = ""
    var lastName: String = ""
    var fullName: String = ""
    var studentID: String = ""
    var email: String = ""

    var grade: Grade
        get() = Grade.init(rawGrade)
        set(value) {
            rawGrade = value.value
            commit("grade")
        }
    var studentType: DessertType? = null

    @SerializedName("majorType") @Expose
    var rawMajorType: String = ""
    val major: Major
        get() = Major.init(rawMajorType)

    var currentCorrectRate: Float = 0f

    var schoolLocation: String = ""
    val location: LocationConfig?
        get() {
            if (schoolLocation.isEmpty())
                return null

            return LocationConfig.locations.filter { it.title == schoolLocation }.firstOrNull()
        }

    var schoolID: Int? = 0
    val isUnivUser: Boolean
        get () {
            return rawSchoolType == SchoolType.UNIVERSITY
        }
    var schoolName: String? = ""

    var regionID: Int? = 0
    var regionName: String? = ""

    var cellPhone: String = ""
    var parentNumber: String? = ""

    var initSettingCompleted: Boolean = false
    var initTestCompleted: Boolean = false
        set(value) {
            field = value
            commit("initTestCompleted")
        }
    var finishInitTestV2: Boolean = false

    @SerializedName("schoolType") @Expose
    var rawSchoolType: SchoolType? = null

    var initMoGrade: Int = 0
    val ratingText: String?
        get() {
            return if (initMoGrade in 1..9)
                "${initMoGrade}등급"
            else
                null
        }

    // add optional subject
//    var noShowAddOptionalSubject = false
//    var noShowAddOptionalDate:Long = 0 // 여기 timestamp 로 처리

    // 회원가입 개선 2021/04/13
    var token:String = ""

    // 중복기기 체크
//    var isExceedDevice = false
    var isValidPhone = false
    var isValidEmail = false

//    var userUniversityMajorCode: String? = null
//    val showMainKUTab: Boolean
//        get () {
//            return schoolID == 6000
//        }

    companion object {

//        fun signup(context: Context, lastName: String, firstName: String, email: String, pw: String, phone: String,
//                   successCB:() -> Unit,
//                   failCB: (Response<Template<Map<String, String>>>) -> Unit) {
//            val param: Parameter = Parameter(
//                    "loginID" to email,
//                    "loginPW" to pw,
//                    "firstName" to firstName,
//                    "lastName" to lastName,
//                    "cellPhone" to phone,
//                    "agreeService" to true,
//                    "agreeMarketing" to true
//            )
//
//            API_V1.signup(param).enqueue(object: Callback<Template<Map<String, String>>> {
//                override fun onFailure(call: Call<Template<Map<String, String>>>, t: Throwable) {
//                    responseFailed(context, t)
//                }
//
//                override fun onResponse(call: Call<Template<Map<String, String>>>, response: Response<Template<Map<String, String>>>) {
//                    if(response.isSuccessful) {
//                        successCB()
//                    } else {
//                        failCB(response)
//                    }
//                }
//            })
//        }
    }

//    val studiedUnit: Set<BigUnitV3>
//        get() {
//            val ids = rawInitStudied.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
//            return ids.mapNotNull { BigUnitV3.initOrNull(it) }
//                .toSet()
//        }

//    val optionalUnit: Set<BigUnitV3>
//        get() {
//            val ids = rawInitOptional.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
//            return ids.mapNotNull { BigUnitV3.initOrNull(it) }
//                .toSet()
//        }

//    val recentUnit: Set<BigUnitV3>
//        get() {
//            val ids = recentSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
//            return ids.mapNotNull { BigUnitV3.initOrNull(it) }
//                .toSet()
//        }
//
//    val recentExcludedUnit: Set<BigUnitV3>
//        get() {
//            val ids = excludeSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
//            return ids.mapNotNull { BigUnitV3.initOrNull(it) }
//                .toSet()
//        }

//    var firstDate: Date = Date()
//    var startDate: Date? = null
//    var endDate: Date? = null
//    var serviceName: String = ""

    var recommendLevel: Int? = 0
    var recommendChapter: Int? = 0
    var recommendStudyPoint: Int? = 0

    var agreeAlimtalk: Boolean = false
    var agreeAppPush: Boolean = false
    var agreeEmail: Boolean = false
    var agreeMarketing: Boolean = false

    var serviceType: PaidServiceType = PaidServiceType.NONE

    @Expose @SerializedName("grade")
    var rawGrade: Int = 0

    @Expose @SerializedName("initStudied")
    var rawInitStudied: String = ""

    @Expose @SerializedName("initOptional")
    var rawInitOptional: String = ""

    var lastExpiredShowingDate: Date
        get() {
            return Date(Preferences.lastExpiredShowingDate.get())
        }
        set(value) {
            Preferences.lastExpiredShowingDate.set(value.time)
        }
    var canUpdateGrade: Boolean = false


    fun log() {
        Log.d("USER MODEL", "StudentID: " + studentID)
        Log.d("USER MODEL", "email: " + email)
        Log.d("USER MODEL", "schoolName: " + schoolName)
        Log.d("USER MODEL", "majorType: " + rawMajorType)
        Log.d("USER MODEL", "currentCorrectRate: " + currentCorrectRate.toString())
        Log.d("USER MODEL", "rawSchoolType: " + rawSchoolType)
        Log.d("USER MODEL", "isInitSettingCompleted: " + initSettingCompleted.toString())
        Log.d("USER MODEL", "cellPhone: " + cellPhone)

        Log.d("USER MODEL", "studiedUnit: " + rawInitStudied)
        Log.d("USER MODEL", "optionalUnit: " + rawInitOptional)

    }

    fun logout(callback: (error:String?)->Unit) {
        API_V2.signout().enqueue(object: Callback<Template<String?>>{
            override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                Log.d(javaClass.simpleName, "로그아웃 성공")
                MyApplication.token = ""
                MyApplication.user?.token = ""
                MyApplication.user = null
                MyApplication.isAppFirstLaunch = true
                Preferences.userDataString.set("")

                callback(null)
            }

            override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                Log.e(javaClass.simpleName, "로그아웃 실패")
                callback(t.localizedMessage)
            }
        })
    }

    fun commit(from:String) {
        val gson = Gson()
        val json = gson.toJson(this)
        Log.d("테스트", "from:${from} set userDataString=${json}")
        Preferences.userDataString.set(json)
    }

//    fun getStudiedUnit(subject: SubjectV3): List<BigUnitV3> {
//        return subject.bigUnits.filter { studiedUnit.contains(it) }
//    }


}

data class DummyCreatedUser(
    val email: String,
)