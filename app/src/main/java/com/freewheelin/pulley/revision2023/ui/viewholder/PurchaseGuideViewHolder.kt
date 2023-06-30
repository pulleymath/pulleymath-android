package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.databinding.ItemPurchaseGuideBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideCompareClickListener
import com.freewheelin.pulley.legacy.utils.partialFontAndColored
import com.freewheelin.pulley.legacy.utils.partialUnderline

class PurchaseGuideViewHolder(
    private val binding: ItemPurchaseGuideBinding,
    private val guideClickListener: PurchaseGuideClickListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PurchaseGuideOffer) = with(binding) {
        lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
        this.item = item
        this.listener = guideClickListener

        binding.rootView.setOnClickListener {
            val position = this@PurchaseGuideViewHolder.absoluteAdapterPosition
            guideClickListener.onGuideClick(item, position)
        }
    }
}