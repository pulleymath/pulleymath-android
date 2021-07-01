package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.User.Companion.TYPE_FREE_ING
import com.freewheelin.pulley.model.User.Companion.TYPE_PAID_ING
import com.freewheelin.pulley.utils.DateTimeUtils
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
    var memberType = ""
    var defaultDDay: DDay = DDay()

    val continuousGoalCount: Int
        get() = studentGoalInfo.continuousGoalCount

    val goalProblemCount: Int
        get() = studentGoalInfo.goalProblemCount

    val progressValue: Float
        get() = studentGoalInfo.dailySolvedProblemCount / studentGoalInfo.goalProblemCount.toFloat()

    val dailySolvedProblemCount: Int
        get() = studentGoalInfo.dailySolvedProblemCount

    fun getDDayTitle(targetTitle: String?): String {
        if(targetTitle == null)
            return defaultDDay.description
        else
            return targetTitle
    }

    fun getDDayText(targetDay: Date?): String {
        val dday = getDDay(targetDay)
        if(dday < 0 )
            return "D+${abs(dday)}"
        else
            return "D-${abs(dday)}"
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

        if(user.grade == Grade.BeforeHigh) {
            return "더 나은 나를 위한 도전!\n하루 ${goalProblemCount}문제 꼬박꼬박"
        } else {
            val ratingText = if (rating == 1) "1등급" else "${rating - 1}등급"
            return "${ratingText}을 위하여\n하루 ${goalProblemCount}문제 꼬박꼬박"
        }
    }

    fun getFreeGuideText(): String {
        return "${numberOfUserText}명이 풀리로 열공 중!\n${studentName}님도 할 수 있어요!"
    }

    fun getUserHashtag(): List<String> {
        if(memberType == User.TYPE_NONE)
            return emptyList()
        else
            return hashTag.filter { it.isNotEmpty() }
    }

    fun isExpiredUser(): Boolean {
        val availableSet = setOf(TYPE_FREE_ING, TYPE_PAID_ING)

        return !availableSet.contains(memberType)
    }

    fun isPaidUser(): Boolean {
        val availableSet = setOf(TYPE_PAID_ING)
        return availableSet.contains(memberType)
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