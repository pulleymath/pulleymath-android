package com.freewheelin.pulley.assets

enum class Grade(val value: Int) {
    BeforeHigh(0),
    High_1(1),
    High_2(2),
    High_3(3),
    AfterHigh(4);


    val text: String
        get() = when (this) {
            BeforeHigh -> "중학생 · 예비 고1"
            High_1 -> "고등 1학년"
            High_2 -> "고등 2학년"
            High_3 -> "고등 3학년"
            AfterHigh -> "N수생"
        }

    val nextGrade: Grade
        get() = when(this) {
            BeforeHigh -> High_1
            High_1 -> High_2
            High_2 -> High_3
            High_3 -> AfterHigh
            AfterHigh -> AfterHigh
        }

    val tabTitle: String
        get() = when (this) {
            BeforeHigh -> "중등 & 예비 고1"
            High_1 -> "고1"
            High_2 -> "고2"
            High_3 -> "고3"
            AfterHigh -> "N수생"
        }

    companion object {
        val list = listOf(BeforeHigh, High_1, High_2, High_3, AfterHigh)

        fun init(value: Int): Grade {
            return when (value) {
                0 -> BeforeHigh
                1 -> High_1
                2 -> High_2
                3 -> High_3
                4 -> AfterHigh
                else -> {
                    throw IllegalAccessException("grade case error")
                }
            }
        }
    }
}
