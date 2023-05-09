package com.freewheelin.pulley.revision2023.model.response

import com.freewheelin.pulley.model.History
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemDetailInfo
import com.freewheelin.pulley.model.contents.BookType
import java.time.LocalDateTime

data class NoteStudyAdvancedLearningResponse(
    var maker: String,
    var schoolType: String,
    var grade: Int?,
    var pieceCategoryTag: BookType,
    var pieceCategory: Set<String>,
    var pieceDerived: String?,
    var subjectTag: String,
    var subject: String,
    var chapter: String,
    var pdfFile: String?,
    var studentID: String,
    var assignID: Int,
    var dateTime: LocalDateTime,
    var updateDateTime: LocalDateTime,
    var problems: List<Problem>
) {
}
