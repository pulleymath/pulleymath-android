package com.freewheelin.pulley.revision2021.model.response

import java.io.Serializable

class LCSubject: Serializable {
    var subjectId = -1
    lateinit var name: String
    lateinit var unitcode: String

    val subjectIndicator: SubjectIndicator
        get() {
            return when(subjectId) {
                1 -> SubjectIndicator.OutOfCurriculum
                2 -> SubjectIndicator.MiddleSchoolCurriculum
                3 -> SubjectIndicator.MathSang
                4 -> SubjectIndicator.MathHa
                5 -> SubjectIndicator.Math1
                6 -> SubjectIndicator.Math2
                7 -> SubjectIndicator.MathProbabilityAndStatistics
                8 -> SubjectIndicator.MathCalculus
                9 -> SubjectIndicator.MathKiha
                else -> SubjectIndicator.MathSang
            }
        }
    enum class SubjectIndicator(val rawValue: Int) {
        OutOfCurriculum(1),
        MiddleSchoolCurriculum(2),
        MathSang(3),
        MathHa(4),
        Math1(5),
        Math2(6),
        MathProbabilityAndStatistics(7),
        MathCalculus(8),
        MathKiha(9)
    }
}