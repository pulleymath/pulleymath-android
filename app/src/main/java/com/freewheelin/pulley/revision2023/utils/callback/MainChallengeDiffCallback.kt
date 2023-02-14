package com.freewheelin.pulley.revision2023.utils.callback

import androidx.recyclerview.widget.DiffUtil
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem

class MainChallengeHeaderDiffCallback : DiffUtil.ItemCallback<MainChallengeHeaderItem>() {
    override fun areItemsTheSame(oldItem: MainChallengeHeaderItem, newItem: MainChallengeHeaderItem): Boolean {
//        return oldItem.id == newItem.id && oldItem.isSelected == newItem.isSelected
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MainChallengeHeaderItem, newItem: MainChallengeHeaderItem): Boolean {
        return oldItem == newItem
    }
}