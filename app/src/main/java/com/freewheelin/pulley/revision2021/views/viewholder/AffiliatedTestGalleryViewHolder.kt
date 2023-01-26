package com.freewheelin.pulley.revision2021.views.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ViewAffiliatedTestGalleryItemBinding
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.utils.listener.AffiliatedGalleryViewClickListener

class AffiliatedTestGalleryViewHolder(
    private val binding: ViewAffiliatedTestGalleryItemBinding,
    private val clickListener: AffiliatedGalleryViewClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(problem: AffiliatedTestProblem) = with(binding) {
        this.problem = problem
        this.listener = clickListener
//        executePendingBindings()

    }

}