package com.freewheelin.pulley.revision2021.model.response

import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.revision2023.SchoolType
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
                10 -> SubjectIndicator.Middle1_1
                11 -> SubjectIndicator.Middle1_2
                12 -> SubjectIndicator.Middle2_1
                13 -> SubjectIndicator.Middle2_2
                14 -> SubjectIndicator.Middle3_1
                15 -> SubjectIndicator.Middle3_2

                else -> SubjectIndicator.OutOfCurriculum
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
        MathKiha(9),
        Middle1_1(10),
        Middle1_2(11),
        Middle2_1(12),
        Middle2_2(13),
        Middle3_1(14),
        Middle3_2(15);

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
                    Middle1_1 -> "중1-1"
                    Middle1_2 -> "중1-2"
                    Middle2_1 -> "중2-1"
                    Middle2_2 -> "중2-2"
                    Middle3_1 -> "중3-1"
                    Middle3_2 -> "중3-2"
                }
            }
        companion object {

            fun convertStrToSubject(value: String): SubjectIndicator {
                return when (value) {
                    Tutorial.name -> { Tutorial }
                    Middle1_1.name -> { Middle1_1 }
                    Middle1_2.name -> { Middle1_2 }
                    Middle2_1.name -> { Middle2_1 }
                    Middle2_2.name -> { Middle2_2 }
                    Middle3_1.name -> { Middle3_1 }
                    Middle3_2.name -> { Middle3_2 }
                    MathSang.name -> { MathSang }
                    MathHa.name -> { MathHa }
                    Math1.name -> { Math1 }
                    Math2.name -> { Math2 }
                    MathProbabilityAndStatistics.name -> { MathProbabilityAndStatistics }
                    MathCalculus.name -> { MathCalculus }
                    MathKiha.name -> { MathKiha }
                    else -> { Tutorial }
                }
            }
            fun convertRawToSubject(rawValue: Int): SubjectIndicator {
                return when (schoolType) {
                    SchoolType.MIDDLE -> when (rawValue) {
                        Tutorial.rawValue -> Tutorial
                        Middle1_1.rawValue -> Middle1_1
                        Middle1_2.rawValue -> Middle1_2
                        Middle2_1.rawValue -> Middle2_1
                        Middle2_2.rawValue -> Middle2_2
                        Middle3_1.rawValue -> Middle3_1
                        Middle3_2.rawValue -> Middle3_2
                        else -> OutOfCurriculum
                    }
                    SchoolType.HIGH -> when (rawValue) {
                        Tutorial.rawValue -> Tutorial
                        OutOfCurriculum.rawValue -> OutOfCurriculum
                        MiddleSchoolCurriculum.rawValue -> MiddleSchoolCurriculum
                        MathSang.rawValue -> MathSang
                        MathHa.rawValue -> MathHa
                        Math1.rawValue -> Math1
                        Math2.rawValue -> Math2
                        MathProbabilityAndStatistics.rawValue -> MathProbabilityAndStatistics
                        MathCalculus.rawValue -> MathCalculus
                        MathKiha.rawValue -> MathKiha
                        else -> OutOfCurriculum
                    }
                    else -> OutOfCurriculum
                }

            }

        }
    }
}