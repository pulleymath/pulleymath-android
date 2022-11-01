package com.freewheelin.pulley.revision2021.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class StudyChapter: BaseDiffItem, Serializable {
    companion object {
        fun createHeader() = StudyChapter().apply {
            id = -1
            name = ""
            sequence = -1
            children = listOf()
        }
        fun createFooter() = StudyChapter().apply {
            id = -2
            name = ""
            sequence = -2
            children = listOf()
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

    val child1: StudyChapter?
        get() {
            children.let {
                if (it.isNotEmpty()) return it[0]
            }
            return null
        }
    val child2: StudyChapter?
        get() {
            children.let {
                if (it.size > 1) return it[1]
            }
            return null
        }
    val child3: StudyChapter?
        get() {
            children.let {
                if (it.size > 2) return it[2]
            }
            return null
        }
    val child4: StudyChapter?
        get() {
            children.let {
                if (it.size > 3) return it[3]
            }
            return null
        }
    val child5: StudyChapter?
        get() {
            children.let {
                if (it.size > 4) return it[4]
            }
            return null
        }

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
            val seq =  when (sequence) {
                1 -> { "Ⅰ" }
                2 -> { "Ⅱ" }
                3 -> { "Ⅲ" }
                else -> { "Ⅰ" }
            }
            return "$seq. ${name}"
        }

    val isHeader: Boolean
        get() {
            return id == -1
        }
    val isFooter: Boolean
        get() {
            return id == -2
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

    val progressTextLength: Int
        get() {
            progress?.let {
                val exerciseTextLength = "${it.exercise.userSolvedCount}/${it.exercise.totalQuizCount}".length
                val patternTextLength = "${it.pattern.userSolvedCount}/${it.pattern.totalQuizCount}".length
                return if (exerciseTextLength > patternTextLength) exerciseTextLength else patternTextLength
            }
            return 3
        }

    val exerciseProgressText: String
        get() {
            progress?.let {
                return "${it.exercise.userSolvedCount}/${it.exercise.totalQuizCount}"
            }
            return "0/0"
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

    val patternProgressText: String
        get() {
            progress?.let {
                return "${it.pattern.userSolvedCount}/${it.pattern.totalQuizCount}"
            }
            return "0/0"
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

    fun isNextItemExist(parent: StudyChapter): Boolean {
        val brotherLastIndex = parent.children.lastIndex
        val itemPosition = parent.children.indexOf(this)
        return brotherLastIndex - itemPosition > 0
    }

    var isNextItemExt: Boolean = false
    fun setNextItemExist(parent: StudyChapter?) {
        if (parent != null) {
            val brotherLastIndex = parent.children.lastIndex
            val itemPosition = parent.children.indexOf(this)
            isNextItemExt = brotherLastIndex - itemPosition > 0
        }
        children.forEach {
            it.setNextItemExist(this)
        }

    }
    fun getItemPosition(parent: StudyChapter): Int {
        return parent.children.indexOf(this) + 1
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
//        lateinit var correctRate: CorrectRate

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