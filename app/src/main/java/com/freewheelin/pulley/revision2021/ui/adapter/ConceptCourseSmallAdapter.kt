package com.freewheelin.pulley.revision2021.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.ui.viewholder.ConceptCourseSmallChapterViewHolder
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel

class ConceptCourseSmallAdapter(val viewModel: ConceptCourseViewModel, val getResult: ActivityResultLauncher<Intent>): ListAdapter<StudyChapter, RecyclerView.ViewHolder>(DiffCallback<StudyChapter>()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ConceptCourseSmallChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_small_chapter, parent, false), viewModel, getResult)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ConceptCourseSmallChapterViewHolder).bind(getItem(position))
    }
}