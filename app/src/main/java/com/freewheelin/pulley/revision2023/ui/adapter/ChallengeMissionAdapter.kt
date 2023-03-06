package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemChallengeDescriptionBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.ui.viewholder.ChallengeDescriptionItemViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeMissionClickListener

class ChallengeMissionAdapter(
    private val missionClickListener: ChallengeMissionClickListener
): ListAdapter<ChallengeCourse, RecyclerView.ViewHolder>(DiffCallback<ChallengeCourse>()) {
    private val typeHeader = 0
    private val typeGrid = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemChallengeDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeDescriptionItemViewHolder(binding, missionClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as ChallengeDescriptionItemViewHolder).bind(item, position, currentList.lastIndex)
        }

    }
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> typeHeader
            else -> typeGrid
        }
    }
}