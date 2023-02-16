package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemChallengeHeaderBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.ui.viewholder.ChallengeListItemViewHolder
import com.freewheelin.pulley.revision2023.utils.callback.MainChallengeHeaderDiffCallback
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeMissionClickListener

class ChallengeHeaderListAdapter(
    private val itemClickListener: ChallengeClickListener,
): ListAdapter<MainChallengeHeaderItem, RecyclerView.ViewHolder>(MainChallengeHeaderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemChallengeHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeListItemViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as ChallengeListItemViewHolder).bind(item, position)
        }

    }
}