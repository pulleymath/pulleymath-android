package com.freewheelin.pulley.revision2021.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.utils.Preferences
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class StudyChapter: BaseDiffItem, Serializable {
    companion object {
        val TUTORIAL_SEQUENCE = -999
        fun createHeader() = StudyChapter().apply {
            id = -1
            name = ""
            sequence = -1
            children = listOf()
        }
        fun createFooter() = StudyChapter().apply {
            id = 999999
            name = ""
            sequence = 999999
            children = listOf()
        }
        fun createTutorial(isCourseInProgress: Boolean?) = StudyChapter().apply {
            id = -4
            name = ""
            sequence = TUTORIAL_SEQUENCE
            parentSequence = -1
            isParentChapterLast = true
            children = listOf(createTutorialChild(isCourseInProgress))
            isFirstMiddleChapter = false
        }
        fun createTutorialChild(isCourseInProgress: Boolean?) = StudyChapter().apply {
            id = -5
            name = "개념학습 튜토리얼"
            sequence = TUTORIAL_SEQUENCE
            isFirstSmallItem = true
            progress = Progress().apply {
                val tutorialPassed = Preferences.isConceptLearningTutorialPassed.get()

                exercise = Exercise().apply {
                    totalQuizCount = 1
                    userSolvedCount = if (tutorialPassed) 1 else 0
                }
                pattern = Pattern().apply {
                    totalQuizCount = 1
                    userSolvedCount = if (tutorialPassed) 1 else 0
                    userWrongCount = 0
                }
            }
            showChallengeCourseFlag = isCourseInProgress == true
        }
    }
    override fun getId(): String {
        return "${id}"
    }

    var id: Int = 0
    lateinit var name: String
    var sequence: Int = 0
    var progress: Progress? = null
    var lastStudiedAt: String? = null
    var children: List<StudyChapter> = listOf()

    var parentName: String = ""
    var isFirstMiddleChapter: Boolean = false
    var isLastMiddleChapter: Boolean = false
    var hasNextItem: Boolean = false
    var isFirstSmallItem: Boolean = false
    var isLastSmallItem: Boolean = false
    var isParentChapterLast: Boolean = false
    var parentSequence: Int = 0

    var showChallengeCourseFlag = false
    var isLocked = false
    val lastStudiedFormatting: String
        get() {
            if (lastStudiedAt == null) return "학습을 시작해보세요!"
            val sdf by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA) }
            val studiedDate = sdf.parse(lastStudiedAt)
            val dateStringWithYear = sdf.format(studiedDate.time)
            val dateStringWithoutYear = dateStringWithYear.substring(5)
            return dateStringWithoutYear
        }

    val largeTitle: String
        get() {
            val seq =  when (parentSequence) {
                1 -> { "Ⅰ" }
                2 -> { "Ⅱ" }
                3 -> { "Ⅲ" }
                else -> { "" }
            }
            return "$seq. ${parentName}"
        }

    val middleTitle: String
        get() {
            return if (sequence < 0) ""
                else "$sequence. $name"
        }
    val isChapterDone: Boolean
        get() {
            progress?.let {
                val isExerciseDone = it.exercise.isDone
                val isPatternDone = it.pattern.isDone
                return isExerciseDone && isPatternDone
            }
            return false
        }
    val isTutorial: Boolean
        get() {
            return sequence == -999
        }

    val progressTextLength: Int
        get() {
            progress?.let {
                val exerciseTextLength = "${it.exercise.userSolvedCount}/${it.exercise.totalQuizCount}".length
                val patternTextLength = "${it.pattern.userSolvedCount}/${it.pattern.totalQuizCount}".length
                return if (exerciseTextLength > patternTextLength) exerciseTextLength else patternTextLength
            }
            return 3
        }

    val exerciseSolvedText: String
        get() {
            progress?.let {
                return "${it.exercise.userSolvedCount}"
            }
            return "0"
        }
    val exerciseTotalText: String
        get() {
            progress?.let {
                return "/${it.exercise.totalQuizCount}"
            }
            return "/0"
        }
    val exerciseProgressRate: Double
        get() {
            progress?.let {
                return it.exercise.progressRate
            }
            return 0.0
        }

    val patternSolvedText: String
        get() {
            progress?.let {
                return "${it.pattern.userSolvedCount}"
            }
            return "0"
        }

    val patternTotalText: String
        get() {
            progress?.let {
                return "/${it.pattern.totalQuizCount}"
            }
            return "/0"
        }
    val patternCorrectProgressRate: Double
        get() {
            progress?.let {
                return it.pattern.correctProgressRate
            }
            return 0.0
        }
    val patternWrongProgressRate: Double
        get() {
            progress?.let {
                return it.pattern.wrongProgressRate
            }
            return 0.0
        }

    fun isChildExist(index: Int): Boolean {
        return children.size > index
    }
    fun setNextItemExist(parent: StudyChapter) {
        val brotherLastIndex = parent.children.lastIndex
        val itemPosition = parent.children.indexOf(this)
        hasNextItem = brotherLastIndex - itemPosition > 0
    }
    fun checkBothEndsItem(parent: StudyChapter) {
        val lastIndex = parent.children.lastIndex
        val itemPosition = parent.children.indexOf(this)

        isFirstSmallItem = itemPosition == 0
        isLastSmallItem = lastIndex == itemPosition
    }

    inner class Progress: Serializable {
        lateinit var exercise: Exercise
        lateinit var pattern: Pattern
    }
    inner class Exercise: Serializable {
        var totalQuizCount: Int = -1
        var userSolvedCount: Int = -2

        val isDone: Boolean
            get() {
                return totalQuizCount == userSolvedCount
            }
        val progressRate: Double
            get() {
                return userSolvedCount.toDouble() / totalQuizCount.toDouble()
            }
    }

    inner class Pattern: Serializable {
        var totalQuizCount: Int = -1
        var userSolvedCount: Int = -2
        var userWrongCount: Int = -3

        val isDone: Boolean
            get() {
                return userWrongCount == 0 && totalQuizCount == userSolvedCount
            }

        val correctProgressRate: Double
            get() {
                return (userSolvedCount - userWrongCount).toDouble() / totalQuizCount.toDouble()
            }

        // wrongProgressRate는 푼문제 만큼 표시하고 correctrate로 덮는형식으로 해보자
        val wrongProgressRate: Double
            get() {
                return userSolvedCount.toDouble() / totalQuizCount.toDouble()
            }

        // ConceptCourseFragment에서는 사용하지 않고있음 -20220913
        inner class CorrectRate: Serializable {
            var initialRate: Float = 0f
            var finalRate: Float = 0f
        }
    }
}