package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.databinding.ItemPurchaseGuideBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideClickListener

class PurchaseGuideViewHolder(
    private val binding: ItemPurchaseGuideBinding,
    private val guideImageClickListener: PurchaseGuideClickListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PurchaseGuideOffer) = with(binding) {
        lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
        this.item = item
        this.listener = guideImageClickListener
    }
}