package com.freewheelin.pulley.revision2023.model.challenge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StartChallenge: Challenge() {



    val isConceptCourseInProgress: Boolean
        get() { return isCourseInProgress(ChallengeManager.CourseName.스타트챌린지_개념) }
    val isPulleyBooksCourseInProgress: Boolean
        get() { return isCourseInProgress(ChallengeManager.CourseName.스타트챌린지_유형) }

    val isPulleyBooksCourseFinished: Boolean
        get() { return isCourseFinished(ChallengeManager.CourseName.스타트챌린지_유형) }

    val isCommercialBooksInProgress: Boolean
        get() { return isCourseInProgress(ChallengeManager.CourseName.스타트챌린지_북스) }
    val isCommercialBooksCourseFinished: Boolean
        get() { return isCourseFinished(ChallengeManager.CourseName.스타트챌린지_북스) }

    val isWorkbooksInProgress: Boolean
        get() { return isCourseInProgress(ChallengeManager.CourseName.스타트챌린지_워크북) }

    fun isCourseInProgress(name: ChallengeManager.CourseName): Boolean {
        val course = courses.find {
            it.challengeCourseId == name.id
        } ?: return false
        return course.status < ChallengeUserStatus.DONE
    }
    fun isCourseFinished(name: ChallengeManager.CourseName): Boolean {
        val course = courses.find {
            it.challengeCourseId == name.id
        } ?: return false
        return course.status == ChallengeUserStatus.DONE
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