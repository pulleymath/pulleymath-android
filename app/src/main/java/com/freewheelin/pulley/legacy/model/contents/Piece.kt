package com.freewheelin.pulley.legacy.model.contents

import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.revision2023.model.response.NoteReviewResponse

class Piece : Content {
    constructor()
    constructor(content: Content): super(content)

    companion object {
        fun convertFromNoteReview(noteReview: NoteReviewResponse): Piece {
            return Piece().apply {
                assignID = noteReview.assignID
                subject = noteReview.subject ?: ""
                schoolType = noteReview.schoolType
                problems = noteReview.problems.map {
                    Problem.convertFromNoteReviewProblem(it)
                }
            }
        }
    }
}

