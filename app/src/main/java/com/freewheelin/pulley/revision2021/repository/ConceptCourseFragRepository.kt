package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2021.repository.remote.ConceptCourseApi
import com.freewheelin.pulley.revision2021.repository.remote.ConceptCourseService

class ConceptCourseFragRepository {
    private val courseService: ConceptCourseService by lazy { ConceptCourseApi.conceptCourseService() }

    fun getAvailableSubject() = courseService.getAvailableSubject()
    fun getChapterOnSubject(subjectId: Int, studentId: String) = courseService.getChapterOnSubject(subjectId, studentId)
    fun createLearningCourse(chapterId: Int, studentId: String) = courseService.createLearningCourse(chapterId, studentId)
    suspend fun suspendCreateLearningCourse(chapterId: Int, studentId: String) = courseService.suspendCreateLearningCourse(chapterId, studentId)
    fun fetchCourseSummary(chapterId: Int, studentId: String) = courseService.fetchCourseSummary(chapterId, studentId)

    val highSchoolTutorialImages = listOf(
        R.drawable.android_concept_learning_high_tutorial_1,
        R.drawable.android_concept_learning_high_tutorial_2,
        R.drawable.android_concept_learning_high_tutorial_3,
        R.drawable.android_concept_learning_high_tutorial_4,
        R.drawable.android_concept_learning_high_tutorial_5,
        R.drawable.android_concept_learning_high_tutorial_6,
        R.drawable.android_concept_learning_high_tutorial_7,
        R.drawable.android_concept_learning_high_tutorial_8,
        R.drawable.android_concept_learning_high_tutorial_9,
        R.drawable.android_concept_learning_high_tutorial_10,
        R.drawable.android_concept_learning_high_tutorial_11, // 이거 클릭했을때 예제채점
        R.drawable.android_concept_learning_high_tutorial_12,
        R.drawable.android_concept_learning_high_tutorial_13,
        R.drawable.android_concept_learning_high_tutorial_14,
        R.drawable.android_concept_learning_high_tutorial_15,
        R.drawable.android_concept_learning_high_tutorial_16, // 여기서 클릭했을때 유형학습 채점
        R.drawable.android_concept_learning_high_tutorial_17,
        R.drawable.android_concept_learning_high_tutorial_18,
        R.drawable.android_concept_learning_high_tutorial_19,
        R.drawable.android_concept_learning_high_tutorial_20,
    )
    val middleSchoolTutorialImages = listOf(
        R.drawable.android_concept_learning_middle_tutorial_1,
        R.drawable.android_concept_learning_middle_tutorial_2,
        R.drawable.android_concept_learning_middle_tutorial_3,
        R.drawable.android_concept_learning_middle_tutorial_4,
        R.drawable.android_concept_learning_middle_tutorial_5,
        R.drawable.android_concept_learning_middle_tutorial_6,
        R.drawable.android_concept_learning_middle_tutorial_7,
        R.drawable.android_concept_learning_middle_tutorial_8,
        R.drawable.android_concept_learning_middle_tutorial_9,
        R.drawable.android_concept_learning_middle_tutorial_10,
        R.drawable.android_concept_learning_middle_tutorial_11, // 이거 클릭했을때 예제채점
        R.drawable.android_concept_learning_middle_tutorial_12,
        R.drawable.android_concept_learning_middle_tutorial_13,
        R.drawable.android_concept_learning_middle_tutorial_14,
        R.drawable.android_concept_learning_middle_tutorial_15,
        R.drawable.android_concept_learning_middle_tutorial_16, // 여기서 클릭했을때 유형학습 채점
        R.drawable.android_concept_learning_middle_tutorial_17,
        R.drawable.android_concept_learning_middle_tutorial_18,
        R.drawable.android_concept_learning_middle_tutorial_19,
        R.drawable.android_concept_learning_middle_tutorial_20,
    )

    val elementarySchoolTutorialImages = listOf(
        R.drawable.android_concept_learning_elementary_tutorial_1, // TODO Elementary change
        R.drawable.android_concept_learning_elementary_tutorial_2,
        R.drawable.android_concept_learning_elementary_tutorial_3,
        R.drawable.android_concept_learning_elementary_tutorial_4,
        R.drawable.android_concept_learning_elementary_tutorial_5,
        R.drawable.android_concept_learning_elementary_tutorial_6,
        R.drawable.android_concept_learning_elementary_tutorial_7,
        R.drawable.android_concept_learning_elementary_tutorial_8,
        R.drawable.android_concept_learning_elementary_tutorial_9,
        R.drawable.android_concept_learning_elementary_tutorial_10,
        R.drawable.android_concept_learning_elementary_tutorial_11, // 이거 클릭했을때 예제채점
        R.drawable.android_concept_learning_elementary_tutorial_12,
        R.drawable.android_concept_learning_elementary_tutorial_13,
        R.drawable.android_concept_learning_elementary_tutorial_14,
        R.drawable.android_concept_learning_elementary_tutorial_15,
        R.drawable.android_concept_learning_elementary_tutorial_16, // 여기서 클릭했을때 유형학습 채점
        R.drawable.android_concept_learning_elementary_tutorial_17,
        R.drawable.android_concept_learning_elementary_tutorial_18,
        R.drawable.android_concept_learning_elementary_tutorial_19,
        R.drawable.android_concept_learning_elementary_tutorial_20,
    )
}