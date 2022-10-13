package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.LCWrongNoteApi
import com.freewheelin.pulley.revision2021.repository.remote.LCWrongNoteService
import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseApi
import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseService

class LCWrongNoteRepository {
    private val lcwrongNoteService: LCWrongNoteService by lazy { LCWrongNoteApi.lcWrongNoteService() }

    fun fetchCourseList(chapterId: Int) = lcwrongNoteService.fetchCourseList(chapterId)
}