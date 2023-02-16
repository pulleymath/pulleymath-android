package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.Preferences
import java.lang.Math.abs
import java.util.*

class MainProfile {
    var profileImageUrl: String = ""
    var backgroundImageUrl: String = ""
    var studentName: String = ""
    var hashTag: List<String> = emptyList()
    var totalSolvedProblemCount: Int = 0
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

    fun getProblemCountGuideText(user: User): String {
        val rating = user.rating

        if(user.grade.isMiddle) {
            return "더 나은 나를 위한 도전!\n하루 ${goalProblemCount}문제 꼬박꼬박"
        } else {
            val ratingText = if (rating <= 1) "1등급" else "${rating - 1}등급"
            return "${ratingText}을 위하여\n하루 ${goalProblemCount}문제 꼬박꼬박"
        }
    }

    fun getFreeGuideText(): String {
        return "${numberOfUserText}명이 풀리수학으로 열공 중!\n${studentName}님도 할 수 있어요!"
    }

    fun getUserHashtag(user: User?): List<String> {
        user?.let { user
            if (user.hasPulleyPlus) return hashTag.filter { it.isNotEmpty() }
        }
        return emptyList()
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
    var description: String = ""
    var startDate: Date = Date()

    fun getDDay(): Int {
        return DateTimeUtils.getDayDifferences(Date(), startDate)
    }
}