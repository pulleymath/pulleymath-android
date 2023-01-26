package com.freewheelin.pulley.revision2021.model.response

import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.LCPatternConcept
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternQuizHint
import com.freewheelin.pulley.revision2021.model.QuizFormat
import java.io.Serializable

class LCWrongNoteMapCardWrapper: Serializable {
    var conceptLearningStatus: String = "DONE"
    var patternLearningStatus: String = "DONE"
    var wrongQuizzes: List<LCWrongNoteMapCard> = listOf()

    val patternStatus: PatternLearningStatus
        get() {
            return when (patternLearningStatus) {
                "NONE" -> PatternLearningStatus.NONE // 개념을 다 푼후 유형을 풀지 않았을 때
                "ING" -> PatternLearningStatus.ING // 개념을 다 푼후 유형을 푸는 중일 때
                "DONE" -> {
                    if (wrongQuizzes.isEmpty()) {
                        PatternLearningStatus.PERFECT // 처음부터 모든문제를 다 맞춘 경우
                    } else {
                        PatternLearningStatus.DONE
                    }
                }
                else -> PatternLearningStatus.NONE
            }
        }
    val conceptStatus: ConceptLearningStatus
        get() {
            return when (conceptLearningStatus) {
                "ING" -> ConceptLearningStatus.ING
                "DONE" -> ConceptLearningStatus.DONE
                else -> ConceptLearningStatus.DONE
            }
        }
    enum class PatternLearningStatus {
        NONE, ING, DONE, PERFECT,
    }
    enum class ConceptLearningStatus {
        ING, DONE
    }
}
class LCWrongNoteMapCard: BaseDiffItem, Serializable {
    var userQuizSolvingHistoryId: Int = -1
    var refPatternId: Int = -1 // TODO 새로생긴값인데 LCPatternQuiz랑 호환해야함
    var refPatternQuizId: Int = 26
    var patternName: String = "유형 04. 명제의 참, 거짓과 진리집합"
    var quizType: String = "QUIZ"
    var questionFormat: String = "SHORT_ANSWER"
    var quizImageUrl: String = "https://pulleycooking.s3.ap-northeast-2.amazonaws.com/pulley_cooking/15/312/76/pattern/4_2q.png"
    var solutionImageUrl: String = "https://pulleycooking.s3.ap-northeast-2.amazonaws.com/pulley_cooking/15/312/76/pattern/4_2s.png"
    var answer: String = "-15"
    var isCorrect: Boolean? = null
    var userAnswer: String? = null
    var correctAnswerRate: Float = 0.51f
    var hintUsageCount: Int = 0
    var hints: List<LCPatternQuizHint> = listOf()
    var concepts: List<LCPatternConcept> = listOf()
    var sequence: Int = 0

    var cardType: CardType = CardType.Card

    var isAnswerSubmitted: ObservableBoolean = ObservableBoolean(false)

    val quizFormat: QuizFormat
        get() {
            return when(questionFormat) {
                "SINGLE_SELECT" -> QuizFormat.Single
                "SHORT_ANSWER" -> QuizFormat.Short
                else -> QuizFormat.Multi
            }
        }
    enum class CardType {
        Header, Card, Footer
    }

    fun isIncomplete(): Boolean {
        return isCorrect == null
    }
    fun isComplete(): Boolean {
        return isCorrect != null
    }

    override fun getId(): String {
        return "${userQuizSolvingHistoryId}"
    }

    companion object {
        fun getHeader (): LCWrongNoteMapCard {
            return LCWrongNoteMapCard().apply {
                refPatternQuizId = -999
                cardType = CardType.Header
            }
        }
        fun getFooter (): LCWrongNoteMapCard {
            return LCWrongNoteMapCard().apply {
                refPatternQuizId = -998
                cardType = CardType.Footer
            }
        }
    }

    fun isShortFormat(): Boolean {
        return quizFormat == QuizFormat.Short
    }
    fun toLCPatternQuiz(): LCPatternQuiz {
        return LCPatternQuiz(
            patternQuizId = refPatternQuizId,
            patternId = refPatternId,
            quizType = quizType,
            questionFormat = questionFormat,
            quizImageUrl = quizImageUrl,
            solutionImageUrl = solutionImageUrl,
            answer = answer,
            isCorrect = isCorrect,
            isFirstTry = true,
            userAnswer = userAnswer,
            correctAnswerRate = correctAnswerRate,
            hintUsageCount = hintUsageCount,
            hints = hints,
            concepts = concepts
        )
    }


}