package com.freewheelin.pulley.legacy.core.API.ResponseModel

import java.io.Serializable

class StudentGoalInfo: Serializable {
    var goalProblemCount: Int = 5
    var dailySolvedProblemCount = 0
    var continuousGoalCount = 0
}


class ScoredStudentGoalInfo: Serializable {
    var studentGoalInfo = StudentGoalInfo()
    var beforeScoreDailySolvedProblemCount: Int = 0
    var askAddSubjectCode = "" // TODO SubjectV3 개편과 함께 사용하지 않게되었음

    val continuousGoalCount: Int
        get() = studentGoalInfo.continuousGoalCount
    val goalProblemCount: Int
        get() = studentGoalInfo.goalProblemCount

//    fun getAskAddSubjectCodes() : Set<Int> {
//        return askAddSubjectCode.split(",").map { it.trim().toIntOrNull() }.filterNotNull().toSet()
//    }
//
//    fun getAskAddSubjects() : Set<Subject> {
//        return getAskAddSubjectCodes().map { Subject.init(it) }.toSet()
//    }
//
//    fun getAskAddSubjectCodeText() : String {
//        return getAskAddSubjects().joinToString(", ")
//    }

    fun isNeedToShowCompletedToast(): Boolean {
        return beforeScoreDailySolvedProblemCount < goalProblemCount &&
                studentGoalInfo.dailySolvedProblemCount >= goalProblemCount
    }
}