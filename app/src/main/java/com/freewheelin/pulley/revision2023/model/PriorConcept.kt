package com.freewheelin.pulley.revision2023.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem

data class PriorConceptWrapper(
    val data: List<PriorConcept>,
    val error: String?,
    val message: String?,
    val current_time: String?
)
@Entity(tableName = "lc_prior_concept_table")
data class PriorConcept(
    @PrimaryKey(autoGenerate = false) val learningCoursePriorConceptId: Int,
    val subject: String,
    val name: String,
    val chapterId: Int,
    val priorConceptImageUrl: String,
    val priorConceptChapterId: Int,
    val priorConceptCookingId: Int,
    val priorConceptCookingName: String,
    val priorConceptSubjectName: String,
    val tags: List<String>,
): BaseDiffItem {
    override fun getId(): String {
        return "$learningCoursePriorConceptId"
    }
}