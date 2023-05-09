package com.freewheelin.pulley.revision2023

enum class SchoolType {
    MIDDLE,
    HIGH,
    UNIVERSITY;

    val isHigh: Boolean
        get() = this == HIGH
    val isMiddle: Boolean
        get() = this == MIDDLE
    val isUniversity: Boolean
        get() = this == UNIVERSITY
    val inKorean: String
        get() {
            return when (this) {
                MIDDLE -> "중등"
                HIGH -> "고등"
                UNIVERSITY -> "대학"
            }
        }
    companion object {
        fun convertFromStr(name: String): SchoolType {
            return when (name) {
                MIDDLE.name -> MIDDLE
                HIGH.name -> HIGH
                UNIVERSITY.name -> UNIVERSITY
                else -> HIGH
            }
        }

    }
}