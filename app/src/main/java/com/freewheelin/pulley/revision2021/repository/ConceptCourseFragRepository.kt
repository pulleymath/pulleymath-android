package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.ConceptCourseApi
import com.freewheelin.pulley.revision2021.repository.remote.ConceptCourseService

class ConceptCourseFragRepository {
    private val courseService: ConceptCourseService by lazy { ConceptCourseApi.conceptCourseService() }

    fun getAvailableSubject() = courseService.getAvailableSubject()
    fun getChapterOnSubject(subjectId: Int, studentId: String) = courseService.getChapterOnSubject(subjectId, studentId)
    fun createLearningCourse(chapterId: Int, studentId: String) = courseService.createLearningCourse(chapterId, studentId)
    fun fetchCourseSummary(chapterId: Int, studentId: String) = courseService.fetchCourseSummary(chapterId, studentId)
}