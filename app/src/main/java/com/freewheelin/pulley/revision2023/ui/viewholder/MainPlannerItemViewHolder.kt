package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemMainPlannerBinding
import com.freewheelin.pulley.legacy.utils.setMarginBottom
import com.freewheelin.pulley.legacy.utils.setMarginTop
import com.freewheelin.pulley.revision2023.model.MainUserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItemType.*
import com.freewheelin.pulley.revision2023.utils.listeners.MainPlannerListItemClickListener

class MainPlannerItemViewHolder(
    private val binding: ItemMainPlannerBinding,
    private val clickListener: MainPlannerListItemClickListener
): RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: MainUserPlannerItem, position: Int, lastIndex: Int) {
        binding.apply {
            this.item = item
            this.listener = clickListener
            this.isLastIndex = position == lastIndex

            when (item.itemType) {
                Header , NothingHeader -> {
                    headerLl.setMarginTop(if (position == 0) 24 else 0)
                }
                Footer -> {
                    footerBorderView.setMarginBottom(if (position == lastIndex) 100 else 16)
                }
                else -> {}
            }
        }
    }
}