package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemNoteStudyCardBinding
import com.freewheelin.pulley.databinding.ItemNoteStudyGroupHeaderBinding
import com.freewheelin.pulley.databinding.ItemNoteStudyHeaderBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.model.NoteStudyType
import com.freewheelin.pulley.revision2023.ui.viewholder.NoteStudyCardViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.NoteStudyGroupHeaderViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.NoteStudyHeaderViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.NoteStudyClickListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteActViewModel

class NoteStudyCardAdapter(
    val viewModel: WrongNoteActViewModel,
    val listener: NoteStudyClickListener
): ListAdapter<NoteStudyProblemWrapper, RecyclerView.ViewHolder>(DiffCallback<NoteStudyProblemWrapper>()) {
    private val typeHeader = 0
    private val typeGroupHeader = 1
    private val typeItem = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typeHeader -> {
                val binding = ItemNoteStudyHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoteStudyHeaderViewHolder(binding, viewModel, listener)
            }
            typeGroupHeader -> {
                val binding = ItemNoteStudyGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoteStudyGroupHeaderViewHolder(binding, viewModel, listener)
            }
            else -> {
                val binding = ItemNoteStudyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoteStudyCardViewHolder(binding, viewModel, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NoteStudyHeaderViewHolder) {
            currentList[holder.absoluteAdapterPosition].also { item ->
                holder.bind(item)
            }
        } else if (holder is NoteStudyGroupHeaderViewHolder) {
            currentList[holder.absoluteAdapterPosition].also { item ->
                holder.bind(item)
            }
        } else if (holder is NoteStudyCardViewHolder) {
            currentList[holder.absoluteAdapterPosition].also { item ->
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).type) {
            NoteStudyType.Header -> typeHeader
            NoteStudyType.GroupHeader -> typeGroupHeader
            NoteStudyType.Card -> typeItem
        }
    }
}