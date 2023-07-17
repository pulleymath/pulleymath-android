package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2023.model.request.NoteReviewRequest
import com.freewheelin.pulley.revision2023.model.response.NoteReviewResponse
import com.freewheelin.pulley.revision2023.service.SolveApi
import com.freewheelin.pulley.revision2023.service.SolveService
import kotlinx.coroutines.CoroutineScope

class SolveActRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val solveApi: SolveService by lazy { SolveApi.solveService() }

    suspend fun getDailyTest(type: String): Test {
        return solveApi.getDailyTest(type).data
    }

    suspend fun getReviewProblems(type: String, studyIDs: List<Int>): Piece {
        val req = NoteReviewRequest(
            user?.studentID!!,
            type,
            studyIDs
        )
        return solveApi.getReviewProblems(req).data.let { req ->
            Piece.convertFromNoteReview(req)
        }
    }
}