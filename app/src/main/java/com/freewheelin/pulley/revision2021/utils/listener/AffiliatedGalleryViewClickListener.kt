package com.freewheelin.pulley.revision2021.utils.listener

import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem

fun interface AffiliatedGalleryViewClickListener {
    fun onGalleryViewClick(problem: AffiliatedTestProblem)
}