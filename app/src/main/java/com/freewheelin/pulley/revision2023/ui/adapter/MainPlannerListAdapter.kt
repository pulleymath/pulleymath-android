package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemMainPlannerBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.MainUserPlannerItem
import com.freewheelin.pulley.revision2023.ui.viewholder.MainPlannerItemViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.MainPlannerListItemClickListener

class MainPlannerListAdapter(
    private val clickListener: MainPlannerListItemClickListener
): ListAdapter<MainUserPlannerItem, RecyclerView.ViewHolder>(DiffCallback<MainUserPlannerItem>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemMainPlannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainPlannerItemViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as MainPlannerItemViewHolder).bind(item, position, currentList.lastIndex)
        }
    }
}