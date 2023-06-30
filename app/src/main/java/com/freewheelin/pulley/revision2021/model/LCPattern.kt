package com.freewheelin.pulley.revision2021.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import java.io.Serializable

enum class QuizFormat {
    Single,
    Multi,
    Short
}

data class LCPatternQuizWrapper(
    val data: List<LCPatternQuiz>,
    val error: String?,
    val message: String?,
    val current_time: String?
)
@Entity(tableName = "lc_pattern_table")
data class LCPatternQuiz(
    @PrimaryKey(autoGenerate = false) val patternQuizId: Int,
    val patternId: Int,
    val quizType: String,
    val questionFormat: String,
    val quizImageUrl: String,
    val solutionImageUrl: String,
    val answer: String,
    var isCorrect: Boolean?,
    var isFirstTry: Boolean?,
    val userAnswer: String?,
    val correctAnswerRate: Float,
    val hintUsageCount: Int,
    val hints: List<LCPatternQuizHint>,
    val concepts: List<LCPatternConcept>,
    val studentId: String? = user?.studentID

): BaseDiffItem, Serializable {
    override fun getId(): String {
        return "$patternQuizId"
    }

    val patternType: PatternType
        get() {
            return when(quizType) {
                "main","Main","MAIN" -> PatternType.Main
                "quiz","Quiz","QUIZ" -> PatternType.Quiz
                else -> PatternType.Quiz
            }
        }

    enum class PatternType(val rawValue: Int) {
        Main(0),
        Quiz(1)
    }

    val correctAnswerRateLimit100: Int
        get() {
            return (correctAnswerRate * 100).toInt()
        }
    val quizFormat: QuizFormat
        get() {
            return when(questionFormat) {
                "SINGLE_SELECT" -> QuizFormat.Single
                "SHORT_ANSWER" -> QuizFormat.Short
                else -> QuizFormat.Multi
            }
        }
    fun scoring() {
        isCorrect = userAnswer == answer
    }

    fun answerWithIcon(): String {
        return when (quizFormat) {
            QuizFormat.Single -> {
                return when (answer) {
                    "1" -> { "①" }
                    "2" -> { "②" }
                    "3" -> { "③" }
                    "4" -> { "④" }
                    "5" -> { "⑤" }
                    else -> { answer }
                }
            }
            QuizFormat.Multi -> {
                answer.split(",")
                    .map { numberWithIcon(it) }
                    .joinToString(separator = ",") { it }
            }
            QuizFormat.Short -> {
                answer
            }
            else -> answer
        }
    }

    fun numberWithIcon(number: String): String {
        return when (number) {
            "1" -> { "①" }
            "2" -> { "②" }
            "3" -> { "③" }
            "4" -> { "④" }
            "5" -> { "⑤" }
            else -> number
        }
    }

    fun isShortFormat(): Boolean {
        return quizFormat == QuizFormat.Short
    }

}
data class LCPatternQuizHint(
    val sequence: Int,
    val hintImageUrl: String
): Serializable
data class LCPatternConcept(
    val conceptType: String,
    val sequence: Int,
    val conceptImageUrl: String,
    val type: ConceptType,
): Serializable {

    enum class ConceptType {
        base,
        related
    }

    val conceptTypeEnum: ConceptType
        get() {
            return when(conceptType) {
                "BASE" -> ConceptType.base
                "RELATED" -> ConceptType.related
                else -> ConceptType.base
            }
        }
}

data class LCPatternScoring(val patternQuizId: Int, val isCorrect :Boolean)
