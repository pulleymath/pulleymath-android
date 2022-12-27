package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener

class PriorConceptViewHolder(
    private val binding: ItemPriorConceptBinding,
    private val priorConceptClickListener: PriorConceptClickListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PriorConcept) = with(binding) {
        this.item = item
        this.listener = priorConceptClickListener
        item.tags.forEach {
            if (labelFlowLayout.childCount < item.tags.size) {
                val label = LabelFlowView(binding.root.context, it, "c0c0c0")
                label.load()
                labelFlowLayout.addView(label)
            }
        }
    }
}