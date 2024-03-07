package com.freewheelin.pulley.legacy.core.API.ResponseModel

import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.model.SignInChannel
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import java.lang.Math.abs
import java.util.*

data class MainProfileV4 (
    val accountEmail: String,
    val email: String,
    val studentId: String,
    val studentName: String,
    val serviceType: PaidServiceType,
    val signInChannel: SignInChannel,
    val hasLesson: Boolean,
    val isAffiliated: Boolean,
    val affiliationInfo: AffiliationInfo?,
    val schoolName: String?,
    val regionName: String?,
    val grade: Int?,
    val profileImageUrl: String,
    val welcomeText: String,
    val numberOfUserText: String,
    val defaultDDay: DDay

) {

    val ddayStr: String
        get() {
            val existTarget = defaultDDay.getTargetTitleAndDate()
            val dday = defaultDDay.getDDay(existTarget?.third)
            return if (dday < 0) {
                defaultDDay.getDDayText(null)
            } else {
                defaultDDay.getDDayText(existTarget?.third)
            }
        }
    val affiliationAndGradeStr: String
        get() {
            return if (serviceType.isGuestUser) ""
            else {
                val affiliation = schoolName ?: regionName ?: ""
                val gradeStr = Grade.init(grade ?: return "학생").text

                "$affiliation · $gradeStr"
            }
        }
}
data class AffiliationInfo(
    val institutionName: String
)
class MainProfile {
    var studentName: String = ""
    var serviceType: PaidServiceType = PaidServiceType.NONE
    var totalSolvedProblemCount: Int = 0
    var profileImageUrl: String = ""
    var hashTag: List<String> = emptyList()
    var totalSolvedWeakProblemCount: Int = 0
    var curation: String = ""
    var numberOfUserText: Int = 0
    var studentGoalInfo: StudentGoalInfo = StudentGoalInfo()
//    var memberType = "" // deprecated
    var defaultDDay: DDay = DDay()

    val continuousGoalCount: Int
        get() = studentGoalInfo.continuousGoalCount

    val goalProblemCount: Int
        get() = studentGoalInfo.goalProblemCount

    val progressValue: Float
        get() = studentGoalInfo.dailySolvedProblemCount / studentGoalInfo.goalProblemCount.toFloat()

    val dailySolvedProblemCount: Int
        get() = studentGoalInfo.dailySolvedProblemCount

    val totalSolvedCountStr: String
        get() = "${totalSolvedProblemCount + totalSolvedWeakProblemCount}"

    val ddayStr: String
        get() {
            val existTarget = getTargetTitleAndDate()
            val dday = getDDay(existTarget?.third)
            return if (dday < 0) {
                getDDayText(null)
            } else {
                getDDayText(existTarget?.third)
            }
        }
    val ddayTargetStr: String
        get() {
            val existTarget = getTargetTitleAndDate()
            val dday = getDDay(existTarget?.third)
            return if (dday < 0) {
                getDDayTitle(null)
            } else {
                getDDayTitle(existTarget?.second)
            }
        }
//    fun getDDayTitle(targetTitle: String?): String {
//        if(targetTitle == null)
//            return defaultDDay.description
//        else
//            return targetTitle
//    }

    fun getDDayText(targetDay: Date?): String {
        val dday = getDDay(targetDay)
        return if(dday < 0 ) {
            "D+${abs(dday)}"
        } else if (dday == 0) {
            "D-Day"
        } else {
            "D-${abs(dday)}"
        }
    }

    fun getDDayTitle(targetTitle: String?): String {
        if(targetTitle == null)
            return defaultDDay.description
        else
            return targetTitle
    }

    fun getDDay(targetDay: Date?): Int {
        if(targetDay == null)
            return defaultDDay.getDDay()
        else
            return DateTimeUtils.getDayDifferences(Date(), targetDay)
    }

    fun getContinuousText(): String {
        if(continuousGoalCount == 0)
            return "달성 도전!"
        else
            return "연속달성 ${continuousGoalCount}일째\uD83D\uDD25"
    }

    fun getFreeGuideText(): String {
        return "${numberOfUserText}명이 풀리수학으로 열공 중!\n${studentName}님도 할 수 있어요!"
    }

    private fun getTargetTitleAndDate(): Triple<Int, String, Date>? {
        val targetTitle = Preferences.targetDateTitle.get()
        val targetDate = Preferences.targetDate.get()
        val targetID = Preferences.targetID.get()
        return if(targetTitle.isEmpty() || targetDate == 0L) {
            null
        } else {
            Triple(targetID, targetTitle, Date(targetDate))
        }
    }


}

class DDay {
    var id: Int = 0
    var type: String = ""
    var description: String = ""
    var startDate: Date = Date()

    fun getDDay(): Int {
        return DateTimeUtils.getDayDifferences(Date(), startDate)
    }

    fun getTargetTitleAndDate(): Triple<Int, String, Date>? {
        val targetTitle = Preferences.targetDateTitle.get()
        val targetDate = Preferences.targetDate.get()
        val targetID = Preferences.targetID.get()
        return if(targetTitle.isEmpty() || targetDate == 0L) {
            null
        } else {
            Triple(targetID, targetTitle, Date(targetDate))
        }
    }
    fun getDDayTitle(targetTitle: String?): String {
        if(targetTitle == null)
            return description
        else
            return targetTitle
    }
    fun getDDayText(targetDay: Date?): String {
        val dday = getDDay(targetDay)
        return if(dday < 0 ) {
            "D+${abs(dday)}"
        } else if (dday == 0) {
            "D-Day"
        } else {
            "D-${abs(dday)}"
        }
    }
    fun getDDay(targetDay: Date?): Int {
        if(targetDay == null)
            return getDDay()
        else
            return DateTimeUtils.getDayDifferences(Date(), targetDay)
    }
    val ddayTargetStr: String
        get() {
            val existTarget = getTargetTitleAndDate()
            val dday = getDDay(existTarget?.third)
            return if (dday < 0) {
                getDDayTitle(null)
            } else {
                getDDayTitle(existTarget?.second)
            }
        }
}