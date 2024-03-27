package com.freewheelin.pulley.revision2021.utils.listener

import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem

fun interface AssessmentGalleryViewClickListener {
    fun onGalleryViewClick(problem: AssessmentProblem)
}