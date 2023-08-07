package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemUserPlannerBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.ui.viewholder.UserPlannerItemViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.UserPlannerItemClickListener

class UserPlannerAdapter(
    private val clickListener: UserPlannerItemClickListener
): ListAdapter<UserPlannerItem, RecyclerView.ViewHolder>(DiffCallback<UserPlannerItem>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemUserPlannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPlannerItemViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as UserPlannerItemViewHolder).bind(item, position, currentList.lastIndex)
        }
    }
}