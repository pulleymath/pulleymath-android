package com.freewheelin.pulley.revision2021.model.response

import java.io.Serializable

class CourseSummary: Serializable {

    lateinit var exerciseProgress: ExerciseProgress
    lateinit var patternProgress: PatternProgress
    lateinit var wrongNoteProgress: WrongNoteProgress
    var studyTime: String? = null

    val studyTimeOnHourMin: String
        get() {
            return if (studyTime == null) "00:00" else studyTime!!
        }

    val mainMessage: String
        get () {
            return if (patternProgress.correctRate.finalRate == 1f) {
                "짝짝짝! 개념 학습을 모두 완료했어요.\n이제 연습 문제로 실력을 다져볼까요?"
            } else if (patternProgress.userSolvedCount == 0) {
                "앗, 아직 유형을 학습하지 않았어요!\n조금 더 공부해보는 게 어떨까요?"
            } else if (patternProgress.totalQuizCount != patternProgress.userSolvedCount) {
                "앗, 아직 풀지 않은 유형이 남아있어요!\n조금 더 공부해보는 게 어떨까요?"
            } else {
                "앗, 아직 오답을 모두 학습하지 않았어요!\n틀린 문제를 다시 풀어볼까요?"
            }
        }

    val subMessage: String
        get () {
            val initialScore = (patternProgress.correctRate.initialRate * 100).toInt()
            val finalScore = (patternProgress.correctRate.finalRate * 100).toInt()

            return if (patternProgress.correctRate.initialRate == 1f) {
                "유형 정답률이 100% 에요!"
            } else if (patternProgress.correctRate.finalRate == 1f) {
                "유형 정답률이 ${initialScore}%에서 100%로 향상됐어요!"
            } else if (patternProgress.userSolvedCount == 0) {
                "유형을 학습해 정답률을 확인해보세요!"
            } else if (patternProgress.totalQuizCount != patternProgress.userSolvedCount) {
                if (initialScore == finalScore) {
                    "유형을 학습해 정답률을 확인해보세요!"
                } else {
                    "유형 정답률이 ${initialScore}%에서 ${finalScore}%로 향상됐어요!"
                }
            } else {
                if (initialScore == finalScore) {
                    "오답을 학습해 정답률을 올려보세요!"
                } else {
                    "유형 정답률이 ${initialScore}%에서 ${finalScore}%로 향상됐어요!"
                }
            }
        }
    inner class ExerciseProgress: Serializable {
        var totalQuizCount: Int = -1
        var userSolvedCount: Int = -1
    }

    inner class CorrectRate: Serializable {
        var initialRate: Float = -1f
        var finalRate: Float = -1f

    }

    inner class PatternProgress: Serializable {
        var totalQuizCount: Int = -1
        var userSolvedCount: Int = -1
        var userWrongCount: Int = -1
        lateinit var correctRate: CorrectRate

        val finalRateStr: String
            get() {
                val result = (correctRate.finalRate * 100).toInt().toString()
                return "${result}%"
            }
        val rateDiffStr: String
            get() {
                return if (correctRate.initialRate == 1f || userSolvedCount == 0) {
                    "-%"
                } else {
                    val result = ((correctRate.finalRate - correctRate.initialRate) * 100).toInt().toString()
                    "${result}%"
                }
            }
        val isRateDiffExist: Boolean
            get () {
                return correctRate.initialRate != 1f && userSolvedCount != 0
            }
    }

    inner class WrongNoteProgress: Serializable {
        var remainQuizCount: Int = -1
    }
}