package com.freewheelin.pulley.legacy.assets

import com.freewheelin.pulley.revision2023.SchoolType

enum class Grade(val value: Int) {
    Etc(0),
    High_1(1),
    High_2(2),
    High_3(3),
    AfterHigh(4), //n수생
    Middle_1(5),
    Middle_2(6),
    Middle_3(7),
    Elementary_1(11),
    Elementary_2(12),
    Elementary_3(13),
    Elementary_4(14),
    Elementary_5(15),
    Elementary_6(16),
    Adult(17);

    val isMiddle: Boolean
        get() = when (this) {
            Middle_1, Middle_2, Middle_3 -> true
            else -> false
        }
    val isHigh: Boolean
        get() = when (this) {
            High_1, High_2, High_3, AfterHigh -> true
            else -> false
        }
    val text: String
        get() = when (this) {
//            BeforeHigh -> "중학생 · 예비 고1"
            High_1 -> "고1"
            High_2 -> "고2"
            High_3 -> "고3"
            AfterHigh -> "N수"
            Middle_1 -> "중1"
            Middle_2 -> "중2"
            Middle_3 -> "중3"
            Elementary_1 -> "초1"
            Elementary_2 -> "초2"
            Elementary_3 -> "초3"
            Elementary_4 -> "초4"
            Elementary_5 -> "초5"
            Elementary_6 -> "초6"
            Adult -> "성인"
            Etc -> "기타"
        }

    val nextGrade: Grade
        get() = when(this) {
//            BeforeHigh -> High_1
            Elementary_1 -> Elementary_2
            Elementary_2 -> Elementary_3
            Elementary_3 -> Elementary_4
            Elementary_4 -> Elementary_5
            Elementary_5 -> Elementary_6
            Elementary_6 -> Middle_1
            Middle_1 -> Middle_2
            Middle_2 -> Middle_3
            Middle_3 -> High_1
            High_1 -> High_2
            High_2 -> High_3
            High_3 -> AfterHigh
            AfterHigh -> AfterHigh
            Adult -> Adult
            Etc -> Etc
        }

    val tabTitle: String
        get() = when (this) {
//            BeforeHigh -> "중등 & 예비 고1"
            High_1 -> "고1"
            High_2 -> "고2"
            High_3 -> "고3"
            AfterHigh -> "N수"
            Middle_1 -> "중1"
            Middle_2 -> "중2"
            Middle_3 -> "중3"
            Elementary_1 -> "초1"
            Elementary_2 -> "초2"
            Elementary_3 -> "초3"
            Elementary_4 -> "초4"
            Elementary_5 -> "초5"
            Elementary_6 -> "초6"
            Adult -> "성인"
            Etc -> "기타"
        }
    val isInitialSchoolTypeHigh: Boolean
        get() = when (this) {
            Adult,
            Etc,
            High_1,
            High_2,
            High_3,
            AfterHigh -> true
            Middle_1,
            Middle_2,
            Middle_3,
            Elementary_1,
            Elementary_2,
            Elementary_3,
            Elementary_4,
            Elementary_5,
            Elementary_6 -> false
        }
    val schoolType: SchoolType
        get() = when (this) {
            Etc,
            High_1,
            High_2,
            High_3,
            AfterHigh,
            Adult -> SchoolType.HIGH
            Middle_1,
            Middle_2,
            Middle_3 -> SchoolType.MIDDLE
            Elementary_1,
            Elementary_2,
            Elementary_3,
            Elementary_4,
            Elementary_5,
            Elementary_6 -> SchoolType.ELEMENTARY
        }

    companion object {
        val serviceGradeList = listOf(Middle_1, Middle_2, Middle_3, High_1, High_2, High_3, AfterHigh, Etc)
        val elementaryList = listOf(Elementary_1, Elementary_2, Elementary_3, Elementary_4, Elementary_5, Elementary_6)
        val middleList = listOf(Middle_1, Middle_2, Middle_3)
        val highList = listOf(High_1, High_2, High_3, AfterHigh)
        val etcList = listOf(Elementary_1, Elementary_2, Elementary_3, Elementary_4, Elementary_5, Elementary_6, Adult)

        fun init(value: Int): Grade {
            return when (value) {
                0 -> Etc
                1 -> High_1
                2 -> High_2
                3 -> High_3
                4 -> AfterHigh
                5 -> Middle_1
                6 -> Middle_2
                7 -> Middle_3
                11 -> Elementary_1
                12 -> Elementary_2
                13 -> Elementary_3
                14 -> Elementary_4
                15 -> Elementary_5
                16 -> Elementary_6
                17 -> Adult
                else -> {
                    High_1
//                    throw IllegalAccessException("grade case error")
                }
            }
        }
        fun isMiddle(value: Int): Boolean {
            return when(value) {
                Middle_1.value,
                Middle_2.value,
                Middle_3.value -> true
                else -> false
            }
        }
        fun isHigh(value: Int): Boolean {
            return when(value) {
                High_1.value,
                High_2.value,
                High_3.value,
                AfterHigh.value -> true
                else -> false
            }
        }
        fun isElementary(value: Int): Boolean {
            return when(value) {
                Elementary_1.value,
                Elementary_2.value,
                Elementary_3.value,
                Elementary_4.value,
                Elementary_5.value,
                Elementary_6.value -> true
                else -> false
            }
        }
    }
}
