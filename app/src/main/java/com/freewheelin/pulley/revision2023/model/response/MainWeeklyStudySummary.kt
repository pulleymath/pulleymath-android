package com.freewheelin.pulley.revision2023.model.response

data class MainWeeklyStudySummary (
    val studentId: String,
    val solvedProblemCount: Int,
    val studyTime: Int,
    val correctRate: Int,
) {
    val presentedStudyTime: String
        get() {
            val hour = studyTime / 3600
            val min = (studyTime - (hour * 3600)) / 60
            val sec = studyTime % 60
            return "${String.format("%02d", hour)}:${String.format("%02d", min)}"
        }
}