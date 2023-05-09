package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.ui.viewholder.BookFilterCalendarViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.BookFilterHeaderViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.BookFilterItemViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.BookFilterToggleViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel

class BookFilterAdapter(
    private val itemListener: BookFilterItemListener,
    ): ListAdapter<BookFilterElement, RecyclerView.ViewHolder>(DiffCallback<BookFilterElement>()) {

    val typeToggle = 0
    val typeHeader = 1
    val typeItem = 2
    val typeCalendar = 3
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typeToggle -> {
                val binding = ItemBookFilterToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BookFilterToggleViewHolder(binding, itemListener)
            }
            typeHeader -> {
                val binding = ItemBookFilterHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BookFilterHeaderViewHolder(binding, itemListener)
            }
            typeCalendar -> {
                val binding = ItemBookFilterCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BookFilterCalendarViewHolder(binding, itemListener)
            }
            else -> {
                val binding = ItemBookFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BookFilterItemViewHolder(binding, itemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItem(position).type) {
            BookFilterElement.Type.Toggle -> {
                (holder as BookFilterToggleViewHolder).apply {
                    bind(getItem(position))
                }
            }
            BookFilterElement.Type.Header -> {
                (holder as BookFilterHeaderViewHolder).apply {
                    bind(getItem(position))
                }
            }
            BookFilterElement.Type.Item -> {
                (holder as BookFilterItemViewHolder).apply {
                    bind(getItem(position))
                }
            }
            BookFilterElement.Type.Calendar -> {
                (holder as BookFilterCalendarViewHolder).apply {
                    bind(getItem(position))
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).type) {
            BookFilterElement.Type.Toggle -> typeToggle
            BookFilterElement.Type.Header -> typeHeader
            BookFilterElement.Type.Item -> typeItem
            BookFilterElement.Type.Calendar -> typeCalendar
        }
    }
}