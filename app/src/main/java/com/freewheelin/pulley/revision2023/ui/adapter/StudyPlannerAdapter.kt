package com.freewheelin.pulley.revision2023.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.freewheelin.pulley.databinding.ItemStudyPlannerBinding
import com.freewheelin.pulley.legacy.utils.extensionTouchArea
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItemType
import com.freewheelin.pulley.revision2023.ui.viewholder.StudyPlannerItemViewHolder
import com.freewheelin.pulley.revision2023.utils.listeners.StudyPlannerItemClickListener
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableAdapter
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableItem
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableItemSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyPlannerAdapter(
    val listener: StudyPlannerItemClickListener,
): ExpandableAdapter<StudyPlannerItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyPlannerItemViewHolder {
        val itemBinding = ItemStudyPlannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyPlannerItemViewHolder(itemBinding, listener)
    }
    override fun onBindViewHolder(holder: StudyPlannerItemViewHolder, position: Int, depth: Int) {
        val planItem = getItem(position) as StudyPlannerItem

        setClickListener(holder, position)

        holder.bind(planItem, position)
        holder.setDepth(depth)
//        holder.setBackground(depth, planItem.isSelected)
        holder.setExpanded(getExpanded(planItem, position))
        holder.setDivider(mItems.lastIndex != position)
    }

    private fun setClickListener (holder: StudyPlannerItemViewHolder, position: Int) {
        if (getItem(position) is ExpandableItem) {
            val item = getItem(position) as ExpandableItem
            val planItem = getItem(position) as StudyPlannerItem
            val _item = mItems[position]
            if (planItem.itemType == StudyPlannerItemType.DIRECTORY) {
                holder.itemView.setOnClickListener {
                    when (expanded(_item)) {
                        true -> removeItems(_item, item.children)
                        false -> addItems(_item, item.children)
                        else -> {}
                    }
                    if (position < mItems.size) {
                        listener.onStudyPlanClick(planItem)
                    } else {
                        Log.e("StudyPlannerAdapter", "IndexOutOfBoundsException: Index: ${position}, Size: ${mItems.size}")
                        Log.e("StudyPlannerAdapter", "item: Index: ${item}, Size: ${mItems.size}")
                    }
                }
            } else {
                holder.itemView.setOnClickListener(null)
                holder.binding.plusIv.extensionTouchArea(4.toPx())
                holder.binding.plusIv.setOnClickListener {
                    listener.onStudyPlanClick(planItem)
                }
            }
        }
    }
    fun forceClickListener(position : Int, cb: (MutableList<ExpandableItemSet>) -> Unit = {}) {
        val item = getItem(position) as ExpandableItem
        val _item = mItems[position]
        addItems(_item, item.children)
        cb(mItems)
    }
    private fun getExpanded(planItem: StudyPlannerItem, position: Int): Boolean? {
        return if (planItem.children.isEmpty()) {
            null
        } else {
            expanded(mItems[position])
        }
    }

    fun changePlanBgColor(position: Int) {
        val item = getItem(position) as StudyPlannerItem
        item.isSelected = true
        notifyItemChanged(position)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            item.isSelected = false
            notifyItemChanged(position)
        }
    }

}
