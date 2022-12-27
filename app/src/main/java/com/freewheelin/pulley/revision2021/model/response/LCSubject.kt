package com.freewheelin.pulley.revision2021.model.response

import java.io.Serializable

class LCSubject: Serializable {
    var subjectId = -1
    lateinit var name: String
    lateinit var unitcode: String

    val subjectIndicator: SubjectIndicator
        get() {
            return when(subjectId) {
                0 -> SubjectIndicator.Tutorial
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
        Tutorial(0),
        OutOfCurriculum(1),
        MiddleSchoolCurriculum(2),
        MathSang(3),
        MathHa(4),
        Math1(5),
        Math2(6),
        MathProbabilityAndStatistics(7),
        MathCalculus(8),
        MathKiha(9);

        val inKorean: String
            get() {
                return when (this) {
                    Tutorial -> "튜토리얼"
                    OutOfCurriculum -> "커리큘럼 외"
                    MiddleSchoolCurriculum -> "중등"
                    MathSang -> "수학(상)"
                    MathHa -> "수학(하)"
                    Math1 -> "수학1"
                    Math2 -> "수학2"
                    MathProbabilityAndStatistics -> "확률과 통계"
                    MathCalculus -> "미적분"
                    MathKiha -> "기하"
                }
            }
    }
}