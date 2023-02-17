package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.activities.learning.tabFragment.book.MyPlanHolder
import com.freewheelin.pulley.activities.learning.tabFragment.book.PlanListener
import com.freewheelin.pulley.databinding.ItemBookMyPlanBinding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback

class PatternStudyMyPlanAdapter(
    private val planListener: PlanListener
): ListAdapter<Book, RecyclerView.ViewHolder>(DiffCallback<Book>()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemBookMyPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyPlanHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->

            (holder as MyPlanHolder).apply {
                listener = planListener
                set(item)
            }
        }
    }
}