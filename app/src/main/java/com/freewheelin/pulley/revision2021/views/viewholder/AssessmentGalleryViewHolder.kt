package com.freewheelin.pulley.revision2021.views.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ViewAssessmentGalleryItemBinding
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.utils.listener.AssessmentGalleryViewClickListener

class AssessmentGalleryViewHolder(
    private val binding: ViewAssessmentGalleryItemBinding,
    private val clickListener: AssessmentGalleryViewClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(problem: AssessmentProblem) = with(binding) {
        this.problem = problem
        this.listener = clickListener
//        executePendingBindings()

    }

}