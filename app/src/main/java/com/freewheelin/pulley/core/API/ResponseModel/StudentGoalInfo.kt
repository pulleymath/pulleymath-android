package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.assets.Subject
import java.io.Serializable

class StudentGoalInfo: Serializable {
    var goalProblemCount: Int = 5
    var dailySolvedProblemCount = 0
    var continuousGoalCount = 0
}


class ScoredStudentGoalInfo: Serializable {
    var studentGoalInfo = StudentGoalInfo()
    var beforeScoreDailySolvedProblemCount: Int = 0
    var askAddSubjectCode = ""

    val continuousGoalCount: Int
        get() = studentGoalInfo.continuousGoalCount
    val goalProblemCount: Int
        get() = studentGoalInfo.goalProblemCount

    fun getAskAddSubjectCodes() : Set<Int> {
        return askAddSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
    }

    fun getAskAddSubjects() : Set<Subject> {
        return getAskAddSubjectCodes().map { Subject.init(it) }.toSet()
    }

    fun getAskAddSubjectCodeText() : String {
        return getAskAddSubjects().joinToString(", ")
    }

    fun isNeedToShowCompletedToast(): Boolean {
        return beforeScoreDailySolvedProblemCount < goalProblemCount &&
                studentGoalInfo.dailySolvedProblemCount >= goalProblemCount
    }
}