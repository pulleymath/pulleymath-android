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
                16 -> SubjectIndicator.Elementary1_1
                17 -> SubjectIndicator.Elementary1_2
                18 -> SubjectIndicator.Elementary2_1
                19 -> SubjectIndicator.Elementary2_2
                20 -> SubjectIndicator.Elementary3_1
                21 -> SubjectIndicator.Elementary3_2
                22 -> SubjectIndicator.Elementary4_1
                23 -> SubjectIndicator.Elementary4_2
                24 -> SubjectIndicator.Elementary5_1
                25 -> SubjectIndicator.Elementary5_2
                26 -> SubjectIndicator.Elementary6_1
                27 -> SubjectIndicator.Elementary6_2

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
        Middle3_2(15),
        Elementary1_1(16),
        Elementary1_2(17),
        Elementary2_1(18),
        Elementary2_2(19),
        Elementary3_1(20),
        Elementary3_2(21),
        Elementary4_1(22),
        Elementary4_2(23),
        Elementary5_1(24),
        Elementary5_2(25),
        Elementary6_1(26),
        Elementary6_2(27);

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
                    Elementary1_1 -> "초1-1"
                    Elementary1_2 -> "초1-2"
                    Elementary2_1 -> "초2-1"
                    Elementary2_2 -> "초2-2"
                    Elementary3_1 -> "초3-1"
                    Elementary3_2 -> "초3-2"
                    Elementary4_1 -> "초4-1"
                    Elementary4_2 -> "초4-2"
                    Elementary5_1 -> "초5-1"
                    Elementary5_2 -> "초5-2"
                    Elementary6_1 -> "초6-1"
                    Elementary6_2 -> "초6-2"
                }
            }
        companion object {

            fun convertStrToSubject(value: String): SubjectIndicator {
                return when (value) {
                    Tutorial.name -> { Tutorial }
                    Elementary1_1.name -> { Elementary1_1 }
                    Elementary1_2.name -> { Elementary1_2 }
                    Elementary2_1.name -> { Elementary2_1 }
                    Elementary2_2.name -> { Elementary2_2 }
                    Elementary3_1.name -> { Elementary3_1 }
                    Elementary3_2.name -> { Elementary3_2 }
                    Elementary4_1.name -> { Elementary4_1 }
                    Elementary4_2.name -> { Elementary4_2 }
                    Elementary5_1.name -> { Elementary5_1 }
                    Elementary5_2.name -> { Elementary5_2 }
                    Elementary6_1.name -> { Elementary6_1 }
                    Elementary6_2.name -> { Elementary6_2 }
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
//                        MiddleSchoolCurriculum.rawValue -> MiddleSchoolCurriculum
                        MathSang.rawValue -> MathSang
                        MathHa.rawValue -> MathHa
                        Math1.rawValue -> Math1
                        Math2.rawValue -> Math2
                        MathProbabilityAndStatistics.rawValue -> MathProbabilityAndStatistics
                        MathCalculus.rawValue -> MathCalculus
                        MathKiha.rawValue -> MathKiha
                        else -> OutOfCurriculum
                    }
                    SchoolType.ELEMENTARY -> when (rawValue) {
                        Tutorial.rawValue -> Tutorial
                        Elementary1_1.rawValue -> { Elementary1_1 }
                        Elementary1_2.rawValue -> { Elementary1_2 }
                        Elementary2_1.rawValue -> { Elementary2_1 }
                        Elementary2_2.rawValue -> { Elementary2_2 }
                        Elementary3_1.rawValue -> { Elementary3_1 }
                        Elementary3_2.rawValue -> { Elementary3_2 }
                        Elementary4_1.rawValue -> { Elementary4_1 }
                        Elementary4_2.rawValue -> { Elementary4_2 }
                        Elementary5_1.rawValue -> { Elementary5_1 }
                        Elementary5_2.rawValue -> { Elementary5_2 }
                        Elementary6_1.rawValue -> { Elementary6_1 }
                        Elementary6_2.rawValue -> { Elementary6_2 }
                        else -> OutOfCurriculum
                    }
                    else -> OutOfCurriculum
                }

            }

        }
    }
}