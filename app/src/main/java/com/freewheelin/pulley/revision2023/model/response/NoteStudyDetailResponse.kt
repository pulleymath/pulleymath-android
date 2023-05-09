package com.freewheelin.pulley.revision2023.model.response

import com.freewheelin.pulley.model.History
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemDetailInfo

data class NoteStudyDetailResponse(
    val problem: NoteStudyProblem,
    val history: List<History> = listOf()
) {
}

data class NoteStudyProblem (
    val studyID: Int,
    val unitCode: Int,
    val problemURL: String,
    val chapterBig: String,
    val chapterMiddle: String,
    val chapterLittle: String,
    val unitName: String,
    val curriculumNumber: Int,
    val category: String,
    val clear: Boolean,
    val scrap: Boolean,
    val solveDateTime: String,
    val updateDateTime: String,
    val problemErrorStatus: String
) {


}