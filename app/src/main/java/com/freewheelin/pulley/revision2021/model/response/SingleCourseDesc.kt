package com.freewheelin.pulley.revision2021.model.response

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.CourseType
import java.io.Serializable


class SingleCourseDesc: BaseDiffItem, Serializable {
    var learningCourseType: String = "PRIOR_CONCEPT"
    var name: String? = null
    var learningCourseDetailId: Int = -1
    var targetConceptCookingId: Int? = null
    var targetChapterId: Int? = null
    var sequence: Int = -1

    val courseType: CourseType
        get() {
            return when(learningCourseType) {
                "PRIOR_CONCEPT" -> { CourseType.PriorConcept }
                "PRIOR_CONCEPT_MAP" -> { CourseType.PriorConceptMap }
                "CONCEPT" -> { CourseType.Cooking }
                "PATTERN_MAP" -> { CourseType.PatternMap }
                "PATTERN" -> { CourseType.Pattern }
                "WRONG_NOTE_MAP" -> { CourseType.WrongNoteMap }
                else -> { CourseType.PriorConceptMap }
            }
        }
    companion object {
        fun getReviewMap(): SingleCourseDesc {
            return SingleCourseDesc().apply {
                learningCourseType = "PRIOR_CONCEPT_MAP"
                name = "리뷰 리스트"
                learningCourseDetailId = -999
            }
        }
        fun getPatternMap(): SingleCourseDesc {
            return SingleCourseDesc().apply {
                learningCourseType = "PATTERN_MAP"
                name = "유형 리스트"
                learningCourseDetailId = -99
            }
        }
        fun getLastPatternMap(): SingleCourseDesc {
            return SingleCourseDesc().apply {
                learningCourseType = "PATTERN_MAP"
                name = "유형 리스트"
                learningCourseDetailId = -98
            }
        }
        fun getWrongNoteMap() : SingleCourseDesc {
            return SingleCourseDesc().apply {
                learningCourseType = "WRONG_NOTE_MAP"
                name = "오답 학습"
                learningCourseDetailId = -97
            }
        }
    }

    override fun getId(): String {
        return "${learningCourseType}_$learningCourseDetailId"
    }
}