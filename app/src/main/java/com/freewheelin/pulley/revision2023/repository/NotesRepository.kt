package com.freewheelin.pulley.revision2023.repository

//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDao
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDatabase
import android.content.Context
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.revision2023.model.request.NoteStudyAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.response.NoteStudyDetailResponse
import com.freewheelin.pulley.revision2023.service.NotesApi
import com.freewheelin.pulley.revision2023.service.NotesService
import kotlinx.coroutines.CoroutineScope

class NotesRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val notesApi: NotesService by lazy { NotesApi.notesService() }

    suspend fun fetchCurriculumSubjects(): List<CurriculumSubject> {
        return notesApi.getSubjects().data
    }
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