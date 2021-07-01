package com.freewheelin.pulley.model.contents

import java.io.Serializable

class MockExamSummary : Serializable {
    var mockID: Int = 0
    var year: Int = 0
    var month: Int = 0
    var grade: Int = 0
    var examType: String = ""
    var containOptional: Boolean = true
    var isIng: Boolean = true
    var markedNumber: Int = 0
    var commonSubjectSummary: Array<SubjectSummary> = arrayOf()
    var optionalSubjectSummary: Array<SubjectSummary> = arrayOf()

    fun getTotalNumber(selectedOptional:MutableList<SubjectSummary>) : Int {
        val total = commonSubjectSummary + selectedOptional
        var number = 0
        for(subject in total) {
            if(subject.isSelected) number += subject.count
        }
        return number
    }

  var majorType: Int = 0
  var updated: Boolean = false

  val type: MockExam.Type
    get() {
      return when (majorType) {
        1 -> MockExam.Type.la
        2 -> MockExam.Type.ns
        else -> MockExam.Type.nd
      }
    }
}

class PublicData : Serializable {
    var status: String = ""
}

class PersonalData : Serializable {
    var markingState: String = ""
    var totalNumber: Int = 0
    var markedNumber: Int = 0
    var correctCount: Int = 0
    var similarProblemNumber: Int = 0
    var score: Int = 0
    var correctRate: Int? = 0
    var percent: Int? = 0
    var rating: Int? = 0
    var optionalSubjectList: Array<SubjectSummary> = arrayOf()
}

class SubjectSummary : Serializable {
    var title: String = ""
    var count: Int = 0
    var subjectCodeType: String = ""
    var isSelected = false

    fun copy() :SubjectSummary {
        val summary = SubjectSummary()
        summary.title = title
        summary.count = count
        summary.subjectCodeType = subjectCodeType
        summary.isSelected = isSelected
        return summary
    }
}

data class MockRequest (
    var mockID: Int,
    var selectOptional: List<String>,
    var isRestart: Boolean
)

data class MockEmailRequest (
        var studentID: String,
        var selectOptional: List<String>,
        var isRestart: Boolean,
        var email:String
)