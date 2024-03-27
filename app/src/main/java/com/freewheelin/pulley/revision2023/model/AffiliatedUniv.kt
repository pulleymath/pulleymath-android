package com.freewheelin.pulley.revision2023.model

import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user

enum class AffiliatedUniv(val schoolId: Int) {

    Konkuk(6000),
    Soongsil(7000);

    val inShortTermEnglish: String
        get() {
            return when (this) {
                Konkuk -> "KU"
                Soongsil -> "SSU"
//                JNE -> TODO()
//                DEFAULT -> TODO()
            }
        }
    val isKonkuk: Boolean
        get() = this == Konkuk
    val isSoongsil: Boolean
        get() = this == Soongsil

    val isThirdTestExist: Boolean
        get() {
            return when(this) {
                Konkuk -> true
                Soongsil -> false
//                JNE -> TODO()
//                DEFAULT -> TODO()
            }
        }

    val firstTabName: String
        get() {
            return when(this) {
                Konkuk -> "기초학습 사전진단"
                Soongsil -> "1차 진단평가"
                else -> "평가"
            }
        }
    val secondTabName: String
        get() {
            return when(this) {
                Konkuk -> "최종 자가진단 1차"
                Soongsil -> "2차 진단평가"
                else -> "평가"
            }
        }
    val thirdTabName: String
        get() {
            return when(this) {
                Konkuk -> "최종 자가진단 2차"
                Soongsil -> ""
                else -> ""
            }
        }
    val firstTestName: String
        get () {
            return when (this) {
                Konkuk -> "사전진단평가"
                Soongsil -> "1차 진단평가"
                else -> "평가"
            }
        }
    val secondTestName: String
        get () {
            return when (this) {
                Konkuk -> "자가진단 1차"
                Soongsil -> "2차 진단평가"
                else -> "평가"
            }
        }
    val thirdTestName: String
        get () {
            return when (this) {
                Konkuk -> "자가진단 2차"
                Soongsil -> "평가"
                else -> "평가"
            }
        }
    fun stepOnScore(score: Int, subject: String? = null): Int {
        return when (this) {
            Konkuk -> {
                if (subject == null) 2
                else {
                    when(subject) {
                        "확률과 통계" -> {
                            when (score) {
                                in 0..50 -> 1
                                in 51..85 -> 2
                                in 86..100 -> 3
                                else -> 2
                            }
                        }
                        "미적분" -> {
                            when (score) {
                                in 0..45 -> 1
                                in 46..85 -> 2
                                in 86..100 -> 3
                                else -> 2
                            }
                        }
                        "물리학" -> {
                            when (score) {
                                in 0..40 -> 1
                                in 41..70 -> 2
                                in 71..100 -> 3
                                else -> 2
                            }
                        }
                        "화학" -> {
                            when (score) {
                                in 0..45 -> 1
                                in 46..85 -> 2
                                in 86..100 -> 3
                                else -> 2
                            }
                        }
                        "생명과학" -> {
                            when (score) {
                                in 0..40 -> 1
                                in 41..85 -> 2
                                in 86..100 -> 3
                                else -> 2
                            }
                        }
                        else -> 2
                    }
                }
            }
            Soongsil -> {
                when (score) {
                    in 0..45 -> 1
                    in 46..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
        }
    }

    fun reportResultBodyText(step: Int): String {
        return when(this) {
            Konkuk -> {
                when (step) {
                    1 -> "많이 어려웠나요? \uD83D\uDE22 \n원활한 전공과목 이수를 위해 1학점 연계 교과목을 필수로 이수해주세요!\n필수 이수가 어렵다면, 보완학습을 진행해볼까요?"
                    2 -> "약점 채우기 딱 좋은 기회네요!\n원활한 전공과목 이수를 위해 1학점 연계 교과목을 이수하거나\n풀리수학과 함께 Dr.KU AI 튜터 시스템으로 보완학습을 진행해봐요."
                    3 -> "어려운 문제 때문에 힘들지 않았나요?\n풀리수학과 함께 Dr.KU AI 튜터 시스템으로 보완학습을 진행하고\n더 만족스러운 결과를 만들어봐요!"
                    else -> ""
                }
            }
            Soongsil -> {
                when (step) {
                    1 -> "많이 어려웠나요? \uD83D\uDE22 \n우선 리뷰하기를 통해 내가 틀린 문제를 다시 점검해볼까요?\n진단평가 리뷰를 끝내고, 보완학습을 진행하여 나의 실력을 올려보세요!"
                    2 -> "약점 채우기 딱 좋은 기회네요!\n풀리수학과 함께 AI 튜터 시스템 슈터디로 보완학습을 진행하고\n실력을 UP 해보세요!"
                    3 -> "어려운 문제 때문에 힘들지 않았나요?\n풀리수학과 함께 AI 튜터 시스템 슈터디로 보완학습을 진행하고\n더 만족스러운 결과를 만들어봐요!"
                    else -> ""
                }
            }
        }
    }

    val tooltipReportClId : Int
        get() {
            return when (this) {
                Konkuk -> R.id.kuToolTipCl
                Soongsil -> R.id.defaultToolTipCl
            }
        }

    val testSize: Int
        get() = when(this) {
            Konkuk -> 3
            Soongsil -> 2
        }


    companion object {
        fun schoolIdOfNonNull(schoolId: Int?): AffiliatedUniv =
            values()
                .firstOrNull { it.schoolId == schoolId } ?: Konkuk
    }
}