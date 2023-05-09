package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemBookFilterBinding
import com.freewheelin.pulley.databinding.ItemBookFilterCalendarBinding
import com.freewheelin.pulley.databinding.ItemBookFilterHeaderBinding
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel

class BookFilterCalendarViewHolder(
    private val binding: ItemBookFilterCalendarBinding,
    private val itemListener: BookFilterItemListener
): RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: BookFilterElement) = with(binding) {
        this.item = item
        this.listener = itemListener
    }
}