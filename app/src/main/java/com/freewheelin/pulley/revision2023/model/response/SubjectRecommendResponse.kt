package com.freewheelin.pulley.revision2023.model.response

data class RecommendSubjectResponse (
    val commonSubjects: List<RecommendSubject>,
    val optionalSubjects: List<RecommendSubject>

)


data class RecommendSubject(
    val subjectId: Int,
    val subjectName: String,
    val chapters: List<SubjectChapter>

)
data class SubjectChapter(
    val chapterId: Int,
    val chapterName: String,
    val isSelected: Boolean
)