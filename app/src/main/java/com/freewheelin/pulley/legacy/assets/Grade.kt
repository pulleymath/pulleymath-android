package com.freewheelin.pulley.legacy.assets

enum class Grade(val value: Int) {
    High_1(1),
    High_2(2),
    High_3(3),
    AfterHigh(4), //n수생
    Middle_1(5),
    Middle_2(6),
    Middle_3(7);

    val isMiddle: Boolean
        get() = when (this) {
            Middle_1, Middle_2, Middle_3 -> true
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
        }

    val nextGrade: Grade
        get() = when(this) {
//            BeforeHigh -> High_1
            High_1 -> High_2
            High_2 -> High_3
            High_3 -> AfterHigh
            AfterHigh -> AfterHigh
            Middle_1 -> Middle_2
            Middle_2 -> Middle_3
            Middle_3 -> High_1
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
        }

    companion object {
        val list = listOf(Middle_1, Middle_2, Middle_3, High_1, High_2, High_3, AfterHigh)
        val middleList = listOf(Middle_1, Middle_2, Middle_3)
        val highList = listOf(High_1, High_2, High_3, AfterHigh)

        fun init(value: Int): Grade {
            return when (value) {
//                0 -> BeforeHigh
                1 -> High_1
                2 -> High_2
                3 -> High_3
                4 -> AfterHigh
                5 -> Middle_1
                6 -> Middle_2
                7 -> Middle_3
                else -> {
                    High_1
//                    throw IllegalAccessException("grade case error")
                }
            }
        }
        fun isMiddle(value: Int): Boolean {
            return when(value) {
                1, 2, 3, 4 -> false
                else -> true
            }
        }
        fun isHigh(value: Int): Boolean {
            return when(value) {
                5, 6, 7 -> false
                else -> true
            }
        }
    }
}
