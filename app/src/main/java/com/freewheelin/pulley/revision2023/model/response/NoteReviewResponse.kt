package com.freewheelin.pulley.revision2023.model.response

import com.freewheelin.pulley.legacy.model.ProblemErrorStatus
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.StudyCategoryEnum
import java.io.Serializable
import java.time.LocalDateTime

data class NoteReviewResponse(
    val assignID: Int?,
    val subject: String?,
    val problems: List<NoteReviewProblem>,
    val schoolType: SchoolType?,
) {
}

data class NoteReviewProblem(
    val studyID: Int,
    val unitCode: Int,
    val answerData: String?,
    val userAnswer: String?,
    val problemID: Int,
    val problemLevel: Int,
    val problemType: String,
    val problemURL: String?,
    val unit: String?,
    val problemNum: Int?,
    val category: StudyCategoryEnum,
    val totalTimes: Int,
    val correctTimes: Int,
    val result: Int?,
    val clear: Boolean,
    val scrap: Boolean,
    val updateDateTime: String?,
    val problemErrorStatus: ProblemErrorStatus
): Serializable {

}