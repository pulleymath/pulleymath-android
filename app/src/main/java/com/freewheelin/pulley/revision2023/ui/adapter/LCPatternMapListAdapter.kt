package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemLcPatternMapCardBinding
import com.freewheelin.pulley.databinding.ItemPatternMapHeaderBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.ui.viewholder.LCPatternMapCardHeaderViewHolder
import com.freewheelin.pulley.revision2023.ui.viewholder.LCPatternMapCardViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.LCPatternMapClickListener
import com.freewheelin.pulley.revision2023.viewmodel.PatternMapViewModel

class LCPatternMapListAdapter(
    private val viewModel: PatternMapViewModel,
    private val patternMapClickListener: LCPatternMapClickListener
): ListAdapter<LCPatternMap, RecyclerView.ViewHolder>(DiffCallback<LCPatternMap>()) {
    private val typeHeader = 0
    private val typeGridItem = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typeHeader -> LCPatternMapCardHeaderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pattern_map_header, parent, false))
            else -> LCPatternMapCardViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_lc_pattern_map_card, parent, false),
                viewModel,
                patternMapClickListener
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as? LCPatternMapCardHeaderViewHolder?)?.bind(item)
            (holder as? LCPatternMapCardViewHolder?)?.bind(item, holder.absoluteAdapterPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> typeHeader
            else -> typeGridItem
        }
    }
}