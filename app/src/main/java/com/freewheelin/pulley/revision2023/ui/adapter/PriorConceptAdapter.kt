package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.ui.viewholder.PriorConceptViewHolder
import com.freewheelin.pulley.revision2023.utils.PriorConceptDiffCallback
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener

class PriorConceptAdapter(
    private val priorConceptClickListener: PriorConceptClickListener
): ListAdapter<PriorConcept, RecyclerView.ViewHolder>(PriorConceptDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPriorConceptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PriorConceptViewHolder(binding, priorConceptClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as PriorConceptViewHolder).bind(item)
        }
    }
}