package com.freewheelin.pulley.revision2023.utils

import androidx.recyclerview.widget.DiffUtil
import com.freewheelin.pulley.revision2023.model.PriorConcept

class PriorConceptDiffCallback: DiffUtil.ItemCallback<PriorConcept>() {
    override fun areItemsTheSame(oldItem: PriorConcept, newItem: PriorConcept): Boolean {
        return oldItem.learningCoursePriorConceptId == newItem.learningCoursePriorConceptId
    }

    override fun areContentsTheSame(oldItem: PriorConcept, newItem: PriorConcept): Boolean {
        return oldItem == newItem
    }
}