package com.freewheelin.pulley.revision2023.model

import com.freewheelin.pulley.R

enum class AssessmentDesignSkin(val schoolIds: List<Int>) {
    KONKUK(listOf(6000)),
    SOONGSIL(listOf(7000)),
    JNE(listOf(48, 199, 224, 225, 226, 227, 228, 250, 268, 270, 271, 272, 273, 274, 314, 318, 364, 424, 425, 426, 437, 458, 459, 470, 477, 487, 488,
        651,748,773,775, 776, 777, 778, 779, 780, 781, 782, 783, 784, 785, 786, 790, 810, 812, 861, 863, 864, 865, 869, 872, 880, 901, 962, 1018, 1043, 1216, 1280, 1314,
        1315, 1316, 1317, 1318, 1319, 1320, 1321, 1322, 1323, 1324, 1325, 1326, 1327, 1363, 1414, 1426, 1459, 1462, 1463, 7077, 1465, 1466, 1467, 1468,
        1469, 1470, 1471, 1477, 1490, 1491, 1493, 1516, 1517, 1527, 1528, 1529, 7076, 1551, 1552, 1576, 1590, 1591, 7078, 1807, 1820, 1822, 1832, 1833,
        1841, 1842, 1844, 1845, 7075, 1849, 1850, 1851, 1852, 1853, 1885, 1921, 1941, 1957, 1967, 1968, 1969, 1977, 2033, 2209, 2236, 2239, 7081, 7079,
        2269, 2280, 2283, 2297, 2298, 7080, 2307, 2308, 2310, 2345, 2373, 2374, 2376)),
    ANDONG(listOf(16000)),
    DEFAULT(listOf(0));

    val univTabText: String
        get() {
            return when (this) {
                KONKUK -> "KU진단"
                SOONGSIL -> "SSU진단"
                JNE -> "JNE진단"
                ANDONG -> "ANU진단"
                DEFAULT -> "진단평가"
            }
        }

    val inShortTermEnglish: String
        get() {
            return when (this) {
                KONKUK -> "KU"
                SOONGSIL -> "SSU"
                JNE -> "전남메타스쿨"
                ANDONG -> "ANU"
                DEFAULT -> ""
            }
        }
    val tooltipReportClId : Int
        get() {
            return when (this) {
                KONKUK -> R.id.kuToolTipCl
                SOONGSIL, JNE, ANDONG, DEFAULT -> R.id.defaultToolTipCl
            }
        }

    val completedSrc : Int
        get() {
            return when (this) {
                KONKUK -> R.drawable.box_colorful_ku
                SOONGSIL -> R.drawable.soongsoong_wink
                JNE -> R.drawable.jne_character
                ANDONG -> R.drawable.andong_baseball
                DEFAULT -> R.drawable.pulling_character_happy
            }
        }

    val cardBackgroundSrc : Int
        get() {
            return when (this) {
                KONKUK -> R.drawable.bg_konkuk_primary_round
                SOONGSIL -> R.drawable.bg_soongsil_primary_round
                JNE -> R.drawable.bg_purple_300_round
                ANDONG -> R.drawable.bg_purple_300_round // TODO
                DEFAULT -> R.drawable.bg_purple_300_round
            }
        }
    val cardCharacterSrc : Int
        get() {
            return when (this) {
                KONKUK -> R.mipmap.kudoctor
                SOONGSIL -> R.drawable.soongsoong_disabled
                JNE -> R.drawable.jne_disabled
                ANDONG -> R.drawable.andong_disabled
                DEFAULT -> R.drawable.pulling_disabled
            }
        }

    fun chipTitle(school: String): String {
        return when (this) {
            KONKUK -> "건국대학교 제휴"
            SOONGSIL -> "숭실대학교 제휴"
            JNE -> "전남메타스쿨"
            ANDONG -> "안동대학교 제휴"
            DEFAULT -> school
        }
    }
    fun stepOnScore(score: Int, subject: String? = null): Int {
        return when (this) {
            KONKUK -> {
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
            SOONGSIL -> {
                when (score) {
                    in 0..45 -> 1
                    in 46..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }

            JNE -> when (score) {
                in 0..45 -> 1
                in 46..85 -> 2
                in 86..100 -> 3
                else -> 2
            }
            ANDONG -> when (score) {
                in 0..45 -> 1
                in 46..85 -> 2
                in 86..100 -> 3
                else -> 2
            }
            DEFAULT ->  when (score) {
                in 0..45 -> 1
                in 46..85 -> 2
                in 86..100 -> 3
                else -> 2
            }
        }
    }

    fun reportResultBodyText(step: Int): String {
        return when(this) {
            KONKUK -> {
                when (step) {
                    1 -> "많이 어려웠나요? \uD83D\uDE22 \n원활한 전공과목 이수를 위해 1학점 연계 교과목을 필수로 이수해주세요!\n필수 이수가 어렵다면, 보완학습을 진행해볼까요?"
                    2 -> "약점 채우기 딱 좋은 기회네요!\n원활한 전공과목 이수를 위해 1학점 연계 교과목을 이수하거나\n풀리수학과 함께 Dr.KU AI 튜터 시스템으로 보완학습을 진행해봐요."
                    3 -> "어려운 문제 때문에 힘들지 않았나요?\n풀리수학과 함께 Dr.KU AI 튜터 시스템으로 보완학습을 진행하고\n더 만족스러운 결과를 만들어봐요!"
                    else -> ""
                }
            }
            SOONGSIL -> {
                when (step) {
                    1 -> "많이 어려웠나요? \uD83D\uDE22 \n우선 리뷰하기를 통해 내가 틀린 문제를 다시 점검해볼까요?\n진단평가 리뷰를 끝내고, 보완학습을 진행하여 나의 실력을 올려보세요!"
                    2 -> "약점 채우기 딱 좋은 기회네요!\n풀리수학과 함께 AI 튜터 시스템 슈터디로 보완학습을 진행하고\n실력을 UP 해보세요!"
                    3 -> "어려운 문제 때문에 힘들지 않았나요?\n풀리수학과 함께 AI 튜터 시스템 슈터디로 보완학습을 진행하고\n더 만족스러운 결과를 만들어봐요!"
                    else -> ""
                }
            }

            ANDONG, JNE, DEFAULT -> when (step) {
                1 -> "많이 어려웠나요? \uD83D\uDE22 \n우선 리뷰하기를 통해 내가 틀린 문제를 다시 점검해볼까요?\n진단평가 리뷰를 끝내고, 보완학습을 진행하여 나의 실력을 올려보세요!"
                2 -> "약점 채우기 딱 좋은 기회네요!\n풀리수학과 함께 보완학습을 진행하고 실력을 UP 해보세요!"
                3 -> "어려운 문제 때문에 힘들지 않았나요?\n풀리수학과 함께 보완학습을 진행하고 더 만족스러운 결과를 만들어봐요!"
                else -> ""
            }
        }
    }

    companion object {
        fun convertGroupCodeToSkin(groupCode: String?): AssessmentDesignSkin {
            return when (groupCode) {
                "1" -> KONKUK
                "2" -> SOONGSIL
                "3" -> JNE
                "4" -> ANDONG
                else -> DEFAULT
            }
        }


        fun convertSkinBySchoolIdOfNonNull(schoolId: Int?): AssessmentDesignSkin =
            AssessmentDesignSkin.values()
                .firstOrNull { it.schoolIds.contains(schoolId) } ?: DEFAULT

        fun univTabText(schoolId: Int?): String {
            val skin = convertSkinBySchoolIdOfNonNull(schoolId)
            return skin.univTabText
        }
        fun univTabTextList(): List<String> {
            return AssessmentDesignSkin.values().map { it.univTabText }
        }
    }
}