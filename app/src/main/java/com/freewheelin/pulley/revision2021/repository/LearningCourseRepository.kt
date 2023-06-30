package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseApi
import com.freewheelin.pulley.revision2021.repository.remote.LearningCourseService

class LearningCourseRepository {
    private val courseService: LearningCourseService by lazy { LearningCourseApi.learningCourseService() }

    fun fetchCourseList(chapterId: Int) = courseService.fetchCourseList(chapterId)
    fun postUserConceptLearningTime(studentId: String, chapterId: Int, param: Parameter) = courseService.postUserConceptLearningTime(studentId, chapterId, param)
}