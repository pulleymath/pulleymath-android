package com.freewheelin.pulley.revision2023.ui.viewholder

import android.view.Gravity
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemMainPlannerBinding
import com.freewheelin.pulley.legacy.utils.setMarginTop
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.utils.visibleOrInvisibleIf
import com.freewheelin.pulley.revision2023.model.MainPlannerListItem
import com.freewheelin.pulley.revision2023.model.MainPlannerListItemPosition.*
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
    fun bind(item: MainPlannerListItem, position: Int, lastIndex: Int) {
        binding.apply {
            this.item = item
            this.listener = clickListener
            this.isLastIndex = position == lastIndex

            headerBorderView.visibleIf(false)
            footerBorderView.visibleIf(false)
            bodyLl.visibleIf(false)

            when (item.itemPosition) {
                Header -> {
                    if (position == 0) {
                        headerBorderView.setMarginTop(24)
                    }
                    headerBorderView.visibleIf(true)
                }
                Body -> {
                    bodyLl.visibleIf(true)

                    when(item.dateAppear) {
                        MainPlannerListItem.DateAppear.DayOfTheWeek -> {
                            dateLl.visibleIf(true)
                            dateLl.gravity = Gravity.BOTTOM
                            daysTv.visibleIf(false)
                            dayOfTheWeekTv.visibleIf(true)
                        }
                        MainPlannerListItem.DateAppear.DateNumber -> {
                            dateLl.visibleIf(true)
                            dateLl.gravity = Gravity.TOP
                            daysTv.visibleIf(true)
                            dayOfTheWeekTv.visibleIf(false)

                        }
                        MainPlannerListItem.DateAppear.All -> {
                            dateLl.visibleIf(true)
                            daysTv.visibleIf(true)
                            dayOfTheWeekTv.visibleIf(true)
                        }
                        MainPlannerListItem.DateAppear.None -> {
                            dateLl.visibleOrInvisibleIf(false)

                        }
                    }
                }
                Footer -> {
                    footerBorderView.visibleIf(true)
                }
            }
        }
    }
}