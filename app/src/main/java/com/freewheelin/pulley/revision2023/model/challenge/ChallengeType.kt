package com.freewheelin.pulley.revision2023.model.challenge

import android.content.Intent

object ChallengeManager {
    const val MAIN_SCREEN_TAB_MOVE_EVENT = "CHALLENGE_MAIN_SCREEN_TAB_MOVE_EVENT"
    const val PATTERN_STUDY_MOVE_EVENT = "PATTERN_STUDY_MOVE_EVENT"
    const val TYPE = "TYPE"
    const val SEQUENCE = "SEQUENCE"
    const val COURSE_ID = "COURSE_ID"

    fun getMainTabMoveIntent(course: ChallengeCourse?): Intent {
        return Intent(MAIN_SCREEN_TAB_MOVE_EVENT).apply {
            val courseId = getCourseId(course?.challengeCourseId)
            putExtra(COURSE_ID, courseId)
        }
    }
    fun getPatternStudyMoveIntent(course: ChallengeCourse?): Intent {
        return Intent(PATTERN_STUDY_MOVE_EVENT).apply {
            val courseId = getCourseId(course?.challengeCourseId)
            putExtra(COURSE_ID, courseId)
        }
    }
    fun getPatternStudyMoveIntent(courseId: Int?): Intent {
        return Intent(PATTERN_STUDY_MOVE_EVENT).apply {
            val courseId = getCourseId(courseId)
            putExtra(COURSE_ID, courseId)
        }
    }
    // course 클릭했을때나 끝났을떄 learningTab의 broadcast에서 받아주는역할을 정한다
    fun getCourseId(id: Int?): Int {
        return when (id) {
            CourseName.스타트챌린지_개념.id -> { 1 }
            CourseName.스타트챌린지_유형.id -> { 2 }
            CourseName.스타트챌린지_북스.id -> { 3 }
            CourseName.스타트챌린지_워크북.id -> { 4 }
//            CourseName.위클리챌린지_Day1.id -> { 0 }
//            CourseName.위클리챌린지_Day2.id -> { 0 }
//            CourseName.위클리챌린지_Day3.id -> { 0 }
            else -> 0
        }
    }

    enum class CourseName(val id: Int) {
        스타트챌린지_개념(1),
        스타트챌린지_유형(2),
        스타트챌린지_북스(3),
        스타트챌린지_워크북(4),
        위클리챌린지_Day1(4),
        위클리챌린지_Day2(5),
        위클리챌린지_Day3(6),
    }
}

//enum class ChallengeType {
//    Start,
//    Weekly,
//    ;
//
//
//    companion object {
//        fun strToType(type: String?): ChallengeType {
//            return when (type) {
//                Start.toString() -> Start
//                Weekly.toString() -> Weekly
//                else -> Start
//            }
//        }
//    }
//}