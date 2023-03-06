package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.databinding.ItemPurchaseGuideBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.viewholder.PriorConceptViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.PurchaseGuideViewHolder
import com.freewheelin.pulley.revision2023.utils.PriorConceptDiffCallback
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideClickListener

class PurchaseGuideAdapter(
    private val guideImageClickListener: PurchaseGuideClickListener
): ListAdapter<PurchaseGuideOffer, RecyclerView.ViewHolder>(DiffCallback<PurchaseGuideOffer>()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPurchaseGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PurchaseGuideViewHolder(binding, guideImageClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as PurchaseGuideViewHolder).bind(item)
        }
    }
}