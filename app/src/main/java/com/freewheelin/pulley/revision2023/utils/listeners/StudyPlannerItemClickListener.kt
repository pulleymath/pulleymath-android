package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem

fun interface StudyPlannerItemClickListener {
    fun onStudyPlanClick(item: StudyPlannerItem)
}