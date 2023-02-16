package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemChallengeHeaderBinding
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener

class ChallengeListItemViewHolder(
    private val binding: ItemChallengeHeaderBinding,
    private val itemClickListener: ChallengeClickListener,
): RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.doOnAttach {
            binding.lifecycleOwner = itemView.findViewTreeLifecycleOwner()
        }
        itemView.doOnDetach {
            binding.lifecycleOwner = null
        }
    }

    fun bind(item: MainChallengeHeaderItem, position: Int) = with(binding) {
        this.item = item
        this.listener = itemClickListener
        this.position = position
    }
}