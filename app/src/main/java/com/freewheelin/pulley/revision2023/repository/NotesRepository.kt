package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterElement.Type
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.BookFilterSection
import com.freewheelin.pulley.revision2023.model.request.NoteStudyAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.response.NoteStudyAdvancedLearningResponse
import com.freewheelin.pulley.revision2023.model.response.NoteStudyDetailResponse
import com.freewheelin.pulley.revision2023.service.NotesApi
import com.freewheelin.pulley.revision2023.service.NotesService
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDao
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDatabase
import com.freewheelin.pulley.revision2023.service.PatternStudyApi
import com.freewheelin.pulley.revision2023.service.PatternStudyService
import com.google.gson.Gson
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope

class NotesRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val notesApi: NotesService by lazy { NotesApi.notesService() }

    suspend fun fetchNotes(startDate: String, endDate: String, mode: String): List<Problem> {
        return notesApi.getNotes(
            startDate = startDate,
            endDate = endDate,
            mode = mode
        ).data
    }

    suspend fun noteDetail(problemId: String): NoteStudyDetailResponse {
        return notesApi.getProblemDetail(problemId).data
    }
    suspend fun makeAdvancedLearning (req: NoteStudyAdvancedLearningRequest): Piece {
        return notesApi.makeAdvancedLearning(req).data

    }

}