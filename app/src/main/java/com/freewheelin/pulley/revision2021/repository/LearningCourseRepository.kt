package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseApi
import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseService

class LearningCourseRepository {
    private val courseService: LearningCourseService by lazy { LearningCourseApi.learningCourseService() }

    fun fetchCourseList(chapterId: Int) = courseService.fetchCourseList(chapterId)
}