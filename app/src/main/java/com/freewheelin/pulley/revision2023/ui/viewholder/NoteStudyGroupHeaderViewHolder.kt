package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemNoteStudyGroupHeaderBinding
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.utils.listeners.NoteStudyClickListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteActViewModel
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteStudyViewModel

class NoteStudyGroupHeaderViewHolder(val binding: ItemNoteStudyGroupHeaderBinding, val viewModel: WrongNoteActViewModel, val listener: NoteStudyClickListener): RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoteStudyProblemWrapper) = with(binding) {
        groupTv.text = item.title
    }
}