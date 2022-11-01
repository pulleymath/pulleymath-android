package com.freewheelin.pulley.revision2021.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable

enum class CourseType(val rawValue: Int) {
    PriorConcept(0),
    PriorConceptMap(1),
    Cooking(2),
    PatternMap(3),
    Pattern(4),
    WrongNoteMap(5)
}
