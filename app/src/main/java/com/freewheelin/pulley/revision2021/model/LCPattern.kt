package com.freewheelin.pulley.revision2021.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable

enum class QuizFormat {
    Single,
    Multi,
    Short
}

class LCPatternCard: BaseDiffItem, Serializable {
    var name: String = "유형 n: 다항식의 덧셈과 뺼셈"
    var imageUrl: String = "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/1.png"
//    var progressRate: Float = 0f // 풀리로 이전하면서 변경
    var progress: List<Progress> = listOf()
    var patternId: Int = 0

//    var studyHistoryId: Int = hashCode() // ?? 이게뭐엿을까

    val isCompleteCard: Boolean
        get() {
            if (progress.isEmpty()) return false
            return progress.all { it.isCorrect != null }
        }

    override fun getId() = "${patternId}"

    companion object {
        fun getHeader (): LCPatternCard {
            return LCPatternCard().apply {
                patternId = -999
            }
        }
    }

    inner class Progress {
        var patternQuizId: Int = -1
        var isCorrect: Boolean? = null
        var isFirstTry: Boolean? = null
    }
}


open class LCPatternQuiz: BaseDiffItem, Serializable {
    override fun getId(): String {
        return "$patternQuizId"
    }

    var patternQuizId: Int = hashCode() // 이름을 quizId로 바꿔야할거같은데?
    var quizType: String = "MAIN" // main or quiz
    var questionFormat: String = "SINGLE_SELECT" // 객관식 주관식 선다형
    var quizImageUrl: String = "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/test/quiz_problem.png"
    var solutionImageUrl: String = ""
    var answer: String = "1" // 문제의 정답
    var isCorrect: Boolean? = null // 맞은 여부 , 미입력시 null
    var isFirstTry: Boolean = true // 유형맵에서 채점 이미지 결정
    var userAnswer: String? = null // 유저가 입력한 정답
    var correctAnswerRate: Float = 0f  // 정답률
    var hintUsageCount: Int = 0 //힌트 사용수
    var hints: List<LCPatternQuizHint> = listOf()
    var concepts: List<LCPatternConcept> = listOf()


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

    fun getUserAnswerWithList(): List<String> {
        userAnswer?.let {
            return when (quizFormat) {
                QuizFormat.Single -> {
                    listOf(it)
                }
                QuizFormat.Multi -> {
                    it.split(",")
                }
                QuizFormat.Short -> {
                    listOf(it)
                }
            }
        }
        return listOf()

    }

    fun isShortFormat(): Boolean {
        return quizFormat == QuizFormat.Short
    }
}

class LCPatternQuizHint: Serializable {
    var sequence: Int = -1
    var hintImageUrl: String = ""
}
class LCPatternConcept: Serializable {
    var conceptType: String = "BASE" // RELEATED , BASE
    var sequence: Int = -1
    var conceptImageUrl: String = "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/test/releated_concept.png"
//    "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/test/quiz_concept.png"

    var type: ConceptType = ConceptType.base

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
