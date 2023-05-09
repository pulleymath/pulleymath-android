package com.freewheelin.pulley.revision2023.model.request

import com.freewheelin.pulley.model.History
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemDetailInfo

data class NoteStudyAdvancedLearningRequest(
    val sameOrSimilar: String?,
    val studentID: String,
    val requestProblemNumber: Int,
    val difficulty: String,
    val noteType: String?,
    val includeClearProblem: Boolean = false,
    val problems: List<AdvancedLearningProblemRequest>
) {
}

data class AdvancedLearningProblemRequest (
    val problemID: Int,
    val unitCode: Int,
    val problemLevel: Int
) {

    companion object {
        fun convertFromProblem(problem: Problem): AdvancedLearningProblemRequest {
            return AdvancedLearningProblemRequest(
                problemID = problem.id,
                unitCode = problem.unitCode,
                problemLevel = problem.problemLevel
            )

        }

    }

}