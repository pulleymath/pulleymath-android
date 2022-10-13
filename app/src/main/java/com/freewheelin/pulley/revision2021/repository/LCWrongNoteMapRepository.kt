package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.LCWrongNoteMapApi
import com.freewheelin.pulley.revision2021.repository.remote.LCWrongNoteMapService

class LCWrongNoteMapRepository {

    private val wrongNoteMapService: LCWrongNoteMapService by lazy { LCWrongNoteMapApi.lcWrongNoteMapService() }

    fun fetchLcWrongNote(chapterId: Int, studentId: String, filter: String) = wrongNoteMapService.fetchLcWrongNote(chapterId, studentId, filter)
}
