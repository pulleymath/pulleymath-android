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

    val isCourseCompleted: Boolean
        get () {
            val exerciseCompleted = exerciseProgress.rate == 1f
            val initialPatternScoreCompleted = patternProgress.correctRate.initialRate == 1f
            val patternStudyCompleted = patternProgress.correctRate.finalRate == 1f
            val wrongnoteCompleted = patternProgress.totalQuizCount == patternProgress.userSolvedCount

            if (exerciseCompleted && initialPatternScoreCompleted) return true
            if (exerciseCompleted && patternStudyCompleted && wrongnoteCompleted) return true
            return false
        }

    val mainMessage: String
        get () {
            return when (mainMessageStatus) {
                MainMessageStatus.예제학습_미완료 -> "앗, 아직 예제를 모두 풀지 않았어요!\n남은 예제를 풀고 오는게 어떨까요?"
                MainMessageStatus.예제완료하고_유형학습_전혀_풀지않음 -> "앗, 아직 유형을 학습하지 않았어요!\n조금 더 공부해보는 게 어떨까요?"
                MainMessageStatus.예제완료하고_유형학습_다_풀지는_않음 -> "앗, 아직 풀지 않은 유형이 남아있어요!\n조금 더 공부해보는 게 어떨까요?"
                MainMessageStatus.예제완료하고_유형완료_오답학습_미완료 -> "앗, 아직 오답을 모두 학습하지 않았어요!\n틀린 문제를 다시 풀어볼까요?"
                MainMessageStatus.예제완료_유형완료_오답완료 -> "짝짝짝! 개념 학습을 모두 완료했어요.\n이제 연습 문제로 실력을 다져볼까요?"
            }
        }

    val subMessage: String
        get () {
            val initialScore = (patternProgress.correctRate.initialRate * 100).toInt()
            val finalScore = (patternProgress.correctRate.finalRate * 100).toInt()

            return when(subMessageStatus) {
                SubMessageStatus.처음부터_다_맞아_학습완료 -> "유형 정답률이 100% 에요!"
                SubMessageStatus.오답학습을_통해_학습완료 -> "유형 정답률이 ${initialScore}%에서 100%로 향상됐어요!"
                SubMessageStatus.유형학습을_한문제도_풀지않음 -> "유형을 학습해 정답률을 확인해보세요!"
                SubMessageStatus.유형학습은_조금_오답학습은_전혀_풀지않음 -> "유형 정답률이 ${initialScore}%에요. 유형을 학습해 정답률을 올려보세요!"
                SubMessageStatus.유형학습은_조금_오답학습도_다_풀지는않음 -> "유형 정답률이 ${initialScore}%에서 ${finalScore}%로 향상됐어요!"
                SubMessageStatus.유형학습은_전부_오답학습은_전혀_풀지않음 -> "유형 정답률이 ${initialScore}%에요. 유형을 학습해 정답률을 올려보세요!"
                SubMessageStatus.유형학습은_전부_오답학습은_다_풀지는않음 -> "유형 정답률이 ${initialScore}%에서 ${finalScore}%로 향상됐어요!"
            }
        }
    val studyMessage: String
        get () {
            return when(mainMessageStatus) {
                MainMessageStatus.예제학습_미완료 -> "예제 풀러 가기"
                MainMessageStatus.예제완료하고_유형학습_전혀_풀지않음 -> "유형 학습하기"
                MainMessageStatus.예제완료하고_유형학습_다_풀지는_않음 -> "유형 학습하기"
                MainMessageStatus.예제완료하고_유형완료_오답학습_미완료 -> "오답 학습하기"
                MainMessageStatus.예제완료_유형완료_오답완료 -> "연습 문제 풀기"
            }
        }
    inner class ExerciseProgress: Serializable {
        var totalQuizCount: Int = -1
        var userSolvedCount: Int = -1

        val rate: Float
            get() {
                return if (totalQuizCount == -1) {
                    0f
                } else {
                    userSolvedCount.toFloat() / totalQuizCount.toFloat()
                }
            }
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
                return if (correctRate.initialRate == 1f || userSolvedCount == 0) {
                    "-%"
                } else {
                    val result = (correctRate.finalRate * 100).toInt().toString()
                    "${result}%"
                }
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

    enum class MainMessageStatus {
        예제학습_미완료,
        예제완료하고_유형학습_전혀_풀지않음,
        예제완료하고_유형학습_다_풀지는_않음,
        예제완료하고_유형완료_오답학습_미완료,
        예제완료_유형완료_오답완료,

    }
    val mainMessageStatus: MainMessageStatus
        get() {
            return if (exerciseProgress.rate < 1f) {
                MainMessageStatus.예제학습_미완료
            } else if (patternProgress.correctRate.finalRate == 1f) {
                MainMessageStatus.예제완료_유형완료_오답완료
            } else if (patternProgress.userSolvedCount == 0) {
                MainMessageStatus.예제완료하고_유형학습_전혀_풀지않음
            } else if (patternProgress.totalQuizCount != patternProgress.userSolvedCount) {
                MainMessageStatus.예제완료하고_유형학습_다_풀지는_않음
            } else {
                MainMessageStatus.예제완료하고_유형완료_오답학습_미완료
            }
        }


    enum class SubMessageStatus {
        처음부터_다_맞아_학습완료,
        오답학습을_통해_학습완료,
        유형학습을_한문제도_풀지않음,
        유형학습은_조금_오답학습은_전혀_풀지않음,
        유형학습은_조금_오답학습도_다_풀지는않음,
        유형학습은_전부_오답학습은_전혀_풀지않음,
        유형학습은_전부_오답학습은_다_풀지는않음
    }
    val subMessageStatus: SubMessageStatus
        get() {
            val initialScore = (patternProgress.correctRate.initialRate * 100).toInt()
            val finalScore = (patternProgress.correctRate.finalRate * 100).toInt()

            return if (patternProgress.correctRate.initialRate == 1f) { // 유형학습 처음부터 다맞았음
                SubMessageStatus.처음부터_다_맞아_학습완료
            } else if (patternProgress.correctRate.finalRate == 1f) { // 유형학습을 오답학습을 통해 완료
                SubMessageStatus.오답학습을_통해_학습완료
            } else if (patternProgress.userSolvedCount == 0) { // 유형을 안풀었을때
                SubMessageStatus.유형학습을_한문제도_풀지않음
            } else if (patternProgress.totalQuizCount != patternProgress.userSolvedCount) { // 다 풀지는 않았음
                if (initialScore == finalScore) { // 오답학습을 안한 상황
                    SubMessageStatus.유형학습은_조금_오답학습은_전혀_풀지않음
                } else { // 오답학습을 한 상황
                    SubMessageStatus.유형학습은_조금_오답학습도_다_풀지는않음
                }
            } else { // 유형을 다 풀었음
                if (initialScore == finalScore) { // 오답학습을 다 하지는 않았음
                    SubMessageStatus.유형학습은_전부_오답학습은_전혀_풀지않음
                } else { // 오답학습을 하는중
                    SubMessageStatus.유형학습은_전부_오답학습은_다_풀지는않음
                }
            }
        }
}