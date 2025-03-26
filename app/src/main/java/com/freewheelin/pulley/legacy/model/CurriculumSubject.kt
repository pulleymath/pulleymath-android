package com.freewheelin.pulley.legacy.model

data class CurriculumSubject(
    val id: Int,
    val name: String,
    val schoolType: String,
    val type: String,
    val curriculumNumber: Int,
    val grade: Int,
    val seq: Int,
    val isActive: Boolean,
    val isDeleted: Boolean,
    val chapterList: List<CurriculumChapter>
)

data class CurriculumChapter (
    val id: Int,
    val name: String,
    val seq: Int,
    val curriculumNumber: Int,
)