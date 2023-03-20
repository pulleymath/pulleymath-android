package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.databinding.ItemPurchaseGuideBinding
import com.freewheelin.pulley.databinding.ItemPurchaseGuideCompareBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.viewholder.PriorConceptViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.PurchaseGuideCompareTextViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.PurchaseGuideViewHolder
import com.freewheelin.pulley.revision2023.utils.PriorConceptDiffCallback
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.PurchaseGuideCompareClickListener

class PurchaseGuideAdapter(
    private val guideImageClickListener: PurchaseGuideClickListener,
    private val compareTextClickListener: PurchaseGuideCompareClickListener,
): ListAdapter<PurchaseGuideOffer, RecyclerView.ViewHolder>(DiffCallback<PurchaseGuideOffer>()) {
    private val typeHeader = 0
    private val typeBody = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == typeHeader) {
            val binding = ItemPurchaseGuideCompareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PurchaseGuideCompareTextViewHolder(binding, compareTextClickListener)
        } else {
            val binding = ItemPurchaseGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PurchaseGuideViewHolder(binding, guideImageClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PurchaseGuideCompareTextViewHolder) {
            holder.bind()
        } else if (holder is PurchaseGuideViewHolder) {
//            val indexExcludingHeader = position - 1
            currentList[holder.absoluteAdapterPosition].also { item ->
                holder.bind(item)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            typeHeader
        } else {
            typeBody
        }
    }
}