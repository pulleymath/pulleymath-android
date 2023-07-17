package com.freewheelin.pulley.revision2023.model.request

data class NoteReviewRequest(
    val studentID: String,
    val type: String,
    val studyIDs: List<Int>
) {
}