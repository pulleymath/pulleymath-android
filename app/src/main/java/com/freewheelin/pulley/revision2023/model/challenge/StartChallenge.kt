package com.freewheelin.pulley.revision2023.model.challenge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StartChallenge: Challenge() {



    val isConceptOfCourseInProgress: Boolean
        get() {
            val course = courses.find {
                it.challengeCourseId == ChallengeManager.CourseName.스타트챌린지_개념.id
            } ?: return false
            return course.status < ChallengeUserStatus.DONE
        }
    val isPatternOfCourseInProgress: Boolean
        get() {
            val course = courses.find {
                it.challengeCourseId == ChallengeManager.CourseName.스타트챌린지_유형.id
            } ?: return false
            return course.status < ChallengeUserStatus.DONE
        }
    val isBooksOfCourseInProgress: Boolean
        get() {
            val course = courses.find {
                it.challengeCourseId == ChallengeManager.CourseName.스타트챌린지_북스.id
            } ?: return false
            return course.status < ChallengeUserStatus.DONE
        }
    val isWorkbooksOfCourseInProgress: Boolean
        get() {
            val course = courses.find {
                it.challengeCourseId == ChallengeManager.CourseName.스타트챌린지_워크북.id
            } ?: return false
            return course.status < ChallengeUserStatus.DONE
        }


    companion object {
        fun convertChild(challenge: Challenge): StartChallenge {
            val challengeStr = Gson().toJson(challenge)
            val scType = object: TypeToken<StartChallenge>(){}.type
            return Gson().fromJson(challengeStr, scType)
        }
//        enum class Sequence(val seq: Int) {
//            개념(0), 유형(1), 북스(2), 워크북(3), 종료(4);
//        }

    }
}