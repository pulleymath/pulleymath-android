package com.freewheelin.pulley.model

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.assets.*
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.DailyRecommend
import com.freewheelin.pulley.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.core.API.ResponseModel.DailySummary
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.utils.*
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
    var schoolName: String? = ""

    var regionID: Int? = 0
    var regionName: String? = ""

    var cellPhone: String = ""

    var initSettingCompleted: Boolean = false
    var initTestCompleted: Boolean = false
        set(value) {
            field = value
            commit("initTestCompleted")
        }
    var finishInitTestV2: Boolean = false

    @SerializedName("schoolType") @Expose
    var rawSchoolType: String = ""

    @Expose @SerializedName("initMoGrade")
    var rating: Int = 0
    val ratingText: String?
        get() {
            return if (rating in 1..9)
                "${rating}등급"
            else
                null
        }

    // add optional subject
    var noShowAddOptionalSubject = false
    var noShowAddOptionalDate:Long = 0 // 여기 timestamp 로 처리

    // 회원가입 개선 2021/04/13
    var token:String = ""

    // 중복기기 체크
    var isExceedDevice = false
    var isValidPhone = false
    var isValidEmail = false

    // 풀리플러스 사용여부
    var hasPulleyPlus = false

    var userUniversityMajorCode: String? = null
    val showMainKUTab: Boolean
        get () {
            return schoolID == 6000
        }

    companion object {
        val TYPE_FREE_ING = "FREE_ING"
        val TYPE_PAID_ING = "PAID_ING"
        val TYPE_NONE = "NONE"
        val TYPE_PAUSE = "PAUSE"


        const val EVENT_STUDENT_TYPE_SETTING = "EVENT_STUDENT_TYPE_SETTING"

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

    val studiedUnit: Set<BigUnit>
        get() {
            val ids = rawInitStudied.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
            return ids.mapNotNull { BigUnit.initOrNull(it) }
                .toSet()
        }

    val optionalUnit: Set<BigUnit>
        get() {
            val ids = rawInitOptional.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
            return ids.mapNotNull { BigUnit.initOrNull(it) }
                .toSet()
        }

    val recentUnit: Set<BigUnit>
        get() {
            val ids = recentSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
            return ids.mapNotNull { BigUnit.initOrNull(it) }
                .toSet()
        }

    val recentExcludedUnit: Set<BigUnit>
        get() {
            val ids = excludeSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull()
            return ids.mapNotNull { BigUnit.initOrNull(it) }
                .toSet()
        }

    var firstDate: Date = Date()
    var startDate: Date? = null
    var endDate: Date? = null
    var serviceName: String = ""

    var recommendLevel: Int? = 0
    var recommendChapter: Int? = 0
    var recommendStudyPoint: Int? = 0

    var agreeAlimtalk: Boolean = false
    var agreeAppPush: Boolean = false
    var agreeEmail: Boolean = false
    var agreeMarketing: Boolean = false

    var serviceType: String = "" // deprecated

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
    var canUpdateGrade: Boolean? = null

    var recentSubjectCode:String = ""
    var excludeSubjectCode:String = ""

    constructor(json: Map<String, String>) {}
    constructor() {}

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

        Log.d("USER MODEL", "recommendLevel: " + getRecommendLevelText())
        Log.d("USER MODEL", "recommendChapter: " + getRecommendRangeText())

        Log.d("USER MODEL", "recentSubjectCode: " + recentSubjectCode)
        Log.d("USER MODEL", "excludeSubjectCode: " + excludeSubjectCode)
    }

    fun logout(callback: (error:String?)->Unit) {
        API_V2.signout().enqueue(object: Callback<Template<String?>>{
            override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                Log.d(javaClass.simpleName, "로그아웃 성공")
                MyApplication.user?.token = ""
                MyApplication.user?.commit("logout")

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

    fun getStudiedUnit(subject: Subject): List<BigUnit> {
        return subject.bigUnits.filter { studiedUnit.contains(it) }
    }

    fun connectToCrashlytics() {
        FirebaseCrashlytics.getInstance().setUserId(studentID)
//        Crashlytics.setUserIdentifier(studentID)
//        Crashlytics.setUserEmail(this.email)
    }

    fun update(newUser:User?) {
        newUser?.let {
            if (newUser.email != null) this.email = newUser.email

            if (newUser.fullName != null) this.fullName = newUser.fullName

            if (newUser.studentID != null) this.studentID = newUser.studentID

            if (newUser.schoolID != null) this.schoolID = newUser.schoolID

            if (newUser.studentType != null) this.studentType = newUser.studentType

            if (newUser.initSettingCompleted != null) this.initSettingCompleted = newUser.initSettingCompleted

            if (newUser.grade != null) this.grade = newUser.grade

            if (newUser.rawMajorType != null) this.rawMajorType = newUser.rawMajorType

            if (newUser.cellPhone != null) this.cellPhone = newUser.cellPhone

            if (newUser.rating != null) this.rating = newUser.rating

            if (newUser.rawInitStudied?.isNotEmpty() == true) this.rawInitStudied = newUser.rawInitStudied

            if (newUser.rawInitOptional?.isNotEmpty() == true) this.rawInitOptional = newUser.rawInitOptional

            if (newUser.token != null) this.token = newUser.token

            if (newUser.hasPulleyPlus != null) this.hasPulleyPlus = newUser.hasPulleyPlus

            this.userUniversityMajorCode = newUser.userUniversityMajorCode

            commit("update")
        }
    }

    fun update(
            email: String? = null,
            fullName: String? = null,
            schoolType: String? = null,
            initSettingCompleted: Boolean? = null,
            majorType: String? = null,
            schoolLocation: String? = null,
            schoolID: Int? = null,
            schoolName: String? = null,
            regionID: Int? = null,
            regionName: String? = null,
            initMoGrade: Int? = null,
            initStudied: String? = null,
            initOptional: String? = null,
            recommendLevel: Int? = null,
            recommendChapter: Int? = null,
            recommendStudyPoint: Int? = null,
            grade: Int? = null,
            agreeAlimtalk: Boolean? = null,
            agreeAppPush: Boolean?= null,
            agreeEmail: Boolean?= null,
            agreeMarketing: Boolean? = null,
            token: String?= null,
            hasPulleyPlus: Boolean?= null
    ) {
        if (email != null) this.email = email

        if (fullName != null) this.fullName = fullName

        if (initSettingCompleted != null) this.initSettingCompleted = initSettingCompleted

        if (grade != null) this.rawGrade = grade

        if (majorType != null) this.rawMajorType = majorType

        if (schoolLocation != null) this.schoolLocation = schoolLocation

        if (schoolID != null) this.schoolID = schoolID else this.schoolID = 0

        if (schoolName != null) this.schoolName = schoolName

        if (regionID != null) this.regionID = regionID else this.regionID = 0

        if (regionName != null) this.regionName = regionName

        if (schoolType != null) this.rawSchoolType = schoolType

        if (initMoGrade != null) this.rating = initMoGrade

        if (initStudied?.isNotEmpty() == true) this.rawInitStudied = initStudied

        if (initOptional?.isNotEmpty() == true) this.rawInitOptional = initOptional

        if (recommendLevel != null) this.recommendLevel = recommendLevel

        if (recommendChapter != null) this.recommendChapter = recommendChapter

        if (recommendStudyPoint != null) this.recommendStudyPoint = recommendStudyPoint

        if(agreeAlimtalk != null) this.agreeAlimtalk = agreeAlimtalk

        if(agreeAppPush != null) this.agreeAppPush = agreeAppPush

        if(agreeEmail != null) this.agreeEmail = agreeEmail

        if(agreeMarketing != null) this.agreeMarketing = agreeMarketing

        if(token != null) this.token = token

        if(hasPulleyPlus != null) this.hasPulleyPlus = hasPulleyPlus

        commit("update")
    }

    fun getAnalysis(context: Context, startDate: Date, endDate: Date, formerDate: Date,  cb: (analysis: Analysis?) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to studentID,
                "formerDate" to DateTimeUtils.yyyy_MM_dd.format(formerDate),
                "endDate" to DateTimeUtils.yyyy_MM_dd.format(endDate),
                "startDate" to DateTimeUtils.yyyy_MM_dd.format(startDate)
        )

        API_V1.getAnalysis(param).enqueue(object: Callback<Template<Analysis>>{
            override fun onFailure(call: Call<Template<Analysis>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Analysis>>, response: Response<Template<Analysis>>) {
                val analysis = response.body()?.data
                cb(analysis)
                if(response.isSuccessful == false)
                    responseError(context, response)
            }
        })
    }

    fun getRecommendLevelText(): String {
        return when(recommendLevel) {
            0 -> "더 쉽게"
            1 -> "수준에 맞게"
            2 -> "더 어렵게"
            else -> {
//                LogUtils.assert(false, "예상못한 recommendLevel ${recommendLevel}, user: ${studentID}")
                Log.e(javaClass.simpleName, "예상못한 recommendLevel ${recommendLevel}, user: ${studentID}")
                "더 어렵게"
            }
        }
    }

    fun getRecommendRangeText(): String {
        return when(recommendChapter) {
            0 -> "최근 공부한 범위"
            1 -> "수능 전범위"
            2 -> "내가 선택한 과목"
            else -> {
//                LogUtils.assert(false, "예상못한 recommendChapter ${recommendChapter}, user: ${studentID}")
                Log.e(javaClass.simpleName, "예상못한 recommendChapter ${recommendChapter}, user: ${studentID}")
                "내가 선택한 과목"
            }
        }
    }

    fun getRecentSubjectText() : String {
        var result = ""
        if(recentSubjectCode.isNotEmpty()) {
            val subjects = recentSubjectCode.split(", ").map { it.trim().toIntOrNull() }.filterNotNull()
            val subjectSet = subjects.toSet()
            val excludes = excludeSubjectCode.split(", ").map { it.trim().toIntOrNull() }.filterNotNull()
            val excluded = subjectSet.minus(excludes)
            val temp = excluded.map { it / 10 }
            result = temp.sortedBy { it }.map { Subject.init(it).filterText }.toSet().joinToString(", ")
        }
        if(result.isEmpty()) {
            studiedUnit.map{ it.name }
        }
        return result
    }

    fun getCommonSubjectText() : String {
        val units = rawInitStudied.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
        val subjects = units.sortedBy {it} .map {it / 10}.map { Subject.init(it).filterText }.toSet().joinToString(", ")
        return subjects
    }

    fun getOptionalSubjectText() : String {
        val units = rawInitOptional.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
        val subjects = units.sortedBy {it}.map {it / 10}.map { Subject.init(it).filterText }.toSet().joinToString(", ")
        return subjects
    }

    fun getAllSubjectText() : String {
        var units = rawInitStudied.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
        val optionalUnits = rawInitOptional.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
        val subjects = units.plus(optionalUnits)
        return subjects.sortedBy{it}.map {it / 10}.map { Subject.init(it).filterText }.toSet().joinToString(", ")
    }

    fun isExpiredUser(): Boolean {
//        val availableSet = setOf(TYPE_FREE_ING, TYPE_PAID_ING)
//        return !availableSet.contains(serviceType)
        return !hasPulleyPlus
    }

    fun syncMyInfo(activity:Activity, cb: (user: User) -> Unit) {

        API_V2.getUser().enqueue(object: Callback<Template<User>> {
            override fun onFailure(call: Call<Template<User>>, t: Throwable) {}
            override fun onResponse(call: Call<Template<User>>, response: Response<Template<User>>) {
                val remoteUser = response.body()?.data

                if(response.isSuccessful && remoteUser != null) {
                    if(user != null) {
                        remoteUser.apply {
                            noShowAddOptionalDate = user!!.noShowAddOptionalDate
                            noShowAddOptionalSubject = user!!.noShowAddOptionalSubject
                            excludeSubjectCode = user!!.excludeSubjectCode
                            recentSubjectCode = user!!.recentSubjectCode
                        }
                    }
                    remoteUser.commit("syncMyInfo")
                    cb(remoteUser)
                }else if(response.code() == 401) {
                    MyApplication.user?.apply {
                        token = ""
                        commit("User syncMyInfo Session Expired")
                    }
                    DialogUtils.expiredSessionDialog(activity)
                }else {
                    DialogUtils.showServerErr(activity)
                }
            }
        })
    }

    fun setUserType(context: Context,
                    studentType: DessertType,
                    knowledgeScore: Int,
                    technicalScore: Int,
                    attitudePlanScore: Int,
                    attitudeMentalScore: Int,
                    successCB: () -> Unit) {
        val param: Parameter = Parameter(
                "studentType" to studentType,
                "knowledgeScore" to knowledgeScore,
                "technicalScore" to technicalScore,
                "attitudePlanScore" to attitudePlanScore,
                "attitudeMentalScore" to attitudeMentalScore
        )

        API_V2.setUserType(studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    this@User.studentType = studentType
                    commit("setUserType")
                    val intent = Intent(EVENT_STUDENT_TYPE_SETTING)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    successCB()
                } else {
                    responseError(context, response, param)
                }
            }

        })
    }

    fun getExpiredDday(): Int? {
        val today = Date()
        endDate ?: return null
        return DateTimeUtils.getDayDifferences(today, endDate!!)
    }

    // deprecated
    fun isNeedToStartFreeMembership(): Boolean {
        return serviceType == TYPE_NONE ||
                (serviceType == TYPE_FREE_ING && startDate == null)
    }

    // deprecated
    fun isNeedToShowExpiredDialog(): Boolean {
        val dday = getExpiredDday() ?: return false

        return (((serviceType == TYPE_FREE_ING || serviceType == TYPE_PAID_ING)
                && (dday in 0..3)) && !DateTimeUtils.isSameDate(Date(), lastExpiredShowingDate))

    }

    fun isNeedToUpdateGrade(): Boolean {
        return canUpdateGrade == true
    }


    fun updateGrade(context: Context, updateGrade: Grade, cb: (() -> Unit)? = null) {
        API_V2.plusGrade(studentID, updateGrade.value).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {}

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    grade = updateGrade
                    if (cb != null)
                        cb()
                }
            }
        })
    }
    fun startFreeMembership(context: Context, cb: () -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to studentID
        )

        API_V2.startFree(studentID).enqueue(object : Callback<Template<User>> {
            override fun onFailure(call: Call<Template<User>>, t: Throwable) {
                responseFailed(context, t)
            }
            override fun onResponse(call: Call<Template<User>>, response: Response<Template<User>>) {
                val user = response.body()?.data

                if (response.isSuccessful && user != null) {
                    user.commit("startFreeMembership")
                    cb()
                } else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun updateCellphone(context: Context, phone: String, successCB:() -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to studentID,
                "cellPhone" to phone
        )

        API_V1.updateCellphone(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    cellPhone = phone
                    commit("updateCellphone")
                    successCB()
                } else {
                    responseError(context, response, param)
                }
            }
        })
    }

    fun setStudyInfoVal(grade: Int, major: String, rating: Int, common: List<Int>?, optional: List<Int>?) {
        this.rawGrade = grade
        this.rawMajorType = major
        this.rating = rating

        if(common?.isNotEmpty() == true)
            this.rawInitStudied = common.joinToString()

        if(optional?.isNotEmpty() == true)
            this.rawInitOptional = optional.joinToString()

        Log.d("테스트", "rawStudied=${this.rawInitStudied}")
        Log.d("테스트", "rawOptional=${this.rawInitOptional}")

        commit("setStudyInfoVal")
    }

    fun setStudiedUnit(bigUnit: Collection<BigUnit>) {
        this.rawInitStudied = bigUnit.map { it.id }.joinToString()
        commit("setStudiedUnit")
    }

    fun setExcludeUnit(bigUnit: Collection<BigUnit>) {
        this.excludeSubjectCode = bigUnit.map { it.id }.joinToString()
        commit("setExcludeUnit")
    }

    fun setOptionalUnit(bigUnit: Collection<BigUnit>) {
        this.rawInitOptional = bigUnit.map { it.id }.joinToString()
        commit("setOptionalUnit")
    }

    fun setOptionalUnitBySubjects(subjects: Collection<Subject>) {
        val bigUnit = mutableListOf<BigUnit>()
        for(subject in subjects) {
            bigUnit.addAll(subject.bigUnits)
        }
        // 기존 optional unit에 더하기
        bigUnit.addAll(optionalUnit)

        this.rawInitOptional = bigUnit.map { it.id }.joinToString()
        commit("setOptionalUnitBySubjects")
    }

    fun setRecentStudyCode(studyCodes:String, exclude:String) {
        recentSubjectCode = studyCodes
        excludeSubjectCode = exclude
        commit("setRecentStudyCode")
    }

    fun getDailyAnalysis(context: Context, successCB: (summary: DailySummary) -> Unit) {
        API_V2.getDailySummary(studentID).enqueue(object: Callback<DailySummary>{
            override fun onFailure(call: Call<DailySummary>, t: Throwable) {

            }

            override fun onResponse(call: Call<DailySummary>, response: Response<DailySummary>) {
                val summary = response.body()
                if(response.isSuccessful && summary != null) {
                    successCB(summary)
                }
            }
        })
    }

    fun getStudyList(context: Context, successCB: (contents: List<Content>) -> Unit) {
        API_V2.getStudyList(studentID).enqueue(object: Callback<List<Content>>{
            override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                if(response.isSuccessful) {
                    val contents = response.body() ?: emptyList()
                    successCB(contents)
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getDailyStudy(context:Context, callback: (DailyStudy)->Unit) {
        API_V2.getDailyStudy(studentID).enqueue(object : Callback<DailyStudy> {
            override fun onResponse(call: Call<DailyStudy>, response: Response<DailyStudy>) {
                response.body()?.let {
                    callback(it)
                }
            }

            override fun onFailure(call: Call<DailyStudy>, t: Throwable) {}
        })
    }

    fun getDailyPiece(context:Context, callback: (List<Content>)->Unit) {
        API_V2.getDailyPiece(studentID).enqueue(object : Callback<List<Content>> {
            override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                response.body()?.let {
                    callback(it)
                }
            }

            override fun onFailure(call: Call<List<Content>>, t: Throwable) {}
        })
    }

    fun getDailyRecommend(context:Context, callback: (DailyRecommend?)->Unit, failCB: () -> Unit) {
        API_V2.getDailyRecommend(studentID).enqueue(object : Callback<DailyRecommend?> {
            override fun onResponse(call: Call<DailyRecommend?>, response: Response<DailyRecommend?>) {
                response.body().let {
                    callback(it)
                }
            }

            override fun onFailure(call: Call<DailyRecommend?>, t: Throwable) {
                failCB()
            }
        })
    }


    @JvmName("setNoShowAddOptionalSubject1")
    fun setNoShowAddOptionalSubject(noShow:Boolean) {
        val now = System.currentTimeMillis()
        noShowAddOptionalSubject = noShow
        if(noShow) {
            noShowAddOptionalDate = now
        }
        commit("setNoShowAddOptionalSubject")
    }

    fun isShowAddOptionalSubjectStatus() : Boolean {
        Log.d("테스트", "askAddSubjectCode: noShowAddOptionalSubject=$noShowAddOptionalSubject, noShowAddOptionalDate=$noShowAddOptionalDate")
        if(noShowAddOptionalSubject && noShowAddOptionalDate > 0) {
            val now = System.currentTimeMillis()
            val period = (now - noShowAddOptionalDate) / 1000 / 60 / 60 / 24 // 일단위
            Log.d("테스트", "askAddSubjectCode: period=$period, now=$now")
            if(period < 7) return false // 7일 미만이면 안보여줌
        }
        return true
    }
}