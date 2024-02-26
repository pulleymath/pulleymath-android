package com.freewheelin.pulley.revision2023

enum class SchoolType {
    ELEMENTARY,
    MIDDLE,
    HIGH,
    UNIVERSITY;

    val isElementary: Boolean
        get() = this == ELEMENTARY
    val isHigh: Boolean
        get() = this == HIGH
    val isMiddle: Boolean
        get() = this == MIDDLE
    val isUniversity: Boolean
        get() = this == UNIVERSITY
    val inKorean: String
        get() {
            return when (this) {
                ELEMENTARY -> "초등"
                MIDDLE -> "중등"
                HIGH -> "고등"
                UNIVERSITY -> "대학"
            }
        }
    val mainSpinnerPosition: Int
        get() {
            return when (this) {
                ELEMENTARY -> 0
                MIDDLE -> 1
                else -> 2
            }
        }
    companion object {
        fun convertFromStr(name: String): SchoolType {
            return when (name) {
                ELEMENTARY.name -> ELEMENTARY
                MIDDLE.name -> MIDDLE
                HIGH.name -> HIGH
                UNIVERSITY.name -> UNIVERSITY
                else -> HIGH
            }
        }

        fun convertSwitchPositionToType(position: Int): SchoolType {
            return when (position) {
                0 -> ELEMENTARY
                1 -> MIDDLE
                2 -> HIGH
                else -> HIGH
            }
        }

    }
}