package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemLcPatternMapCardBinding
import com.freewheelin.pulley.databinding.ItemPatternMapCardBinding
import com.freewheelin.pulley.databinding.ItemPatternMapHeaderBinding
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.utils.listeners.LCPatternMapClickListener
import com.freewheelin.pulley.revision2023.viewmodel.PatternMapViewModel
import com.freewheelin.pulley.utils.BoongthEffect

class LCPatternMapCardHeaderViewHolder(
    private val binding: ItemPatternMapHeaderBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LCPatternMap) { }
}

class LCPatternMapCardViewHolder(
    private val binding: ItemLcPatternMapCardBinding,
    private val viewModel: PatternMapViewModel,
    private val clickListener: LCPatternMapClickListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LCPatternMap, position: Int) {
        binding.apply {
            listener = clickListener
            this.item = item
            vm = viewModel

            cardNumber = "${position}"
            isFinalCard = position == viewModel.lcPatternMaps.value?.lastIndex
            isTopPosition = position < 4
            isLeftPosition = position % 3 == 1
            isRightPosition = position % 3 == 0
            showNextStepButton = viewModel.lcPatternMaps.value?.map {
                it.isCompleteCard
            }?.reduce { prev, next ->
                prev || next
            }

            viewModel.lcPatternMaps.value?.lastIndex?.let { lastIndex ->
                if (position + 2 < lastIndex) {
                    isBottomPosition = false
                    return@let
                }

                when (lastIndex % 3) {
                    0 -> { isBottomPosition = position in (lastIndex - 2 .. lastIndex) }
                    1 -> { isBottomPosition = position == lastIndex }
                    2 -> { isBottomPosition = position in (lastIndex - 1 .. lastIndex) }
                }
            }

            cardRootCl.setOnTouchListener(BoongthEffect())
        }
    }
}