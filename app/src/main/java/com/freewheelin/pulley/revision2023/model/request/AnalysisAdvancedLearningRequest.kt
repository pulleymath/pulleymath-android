package com.freewheelin.pulley.revision2023.model.request

import com.freewheelin.pulley.model.History
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemDetailInfo

data class AnalysisAdvancedLearningRequest(
    val sameOrSimilar: String?,
    val studentID: String,
    val requestProblemNumber: Int,
    val difficulty: String,
    val noteType: String?,
    val includeClearProblem: Boolean = false,
    val chapterLittles: List<Int>,
    val startDate: String?,
    val endDate: String?
) {
}