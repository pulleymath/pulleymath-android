package com.freewheelin.pulley.revision2023.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ItemChallengeDescriptionBinding
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeMissionClickListener
import com.freewheelin.pulley.utils.partialFontAndColored
import com.pulleymath.android.pdf.utils.onDebounceClick
import com.pulleymath.android.pdf.utils.onThrottleClick

class ChallengeDescriptionItemViewHolder(
    private val binding: ItemChallengeDescriptionBinding,
    private val missionClickListener: ChallengeMissionClickListener
): RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: ChallengeCourse, position: Int, lastIndex: Int) = with(binding) {
        val isDescription = position == 0
        this.item = item
        this.listener = missionClickListener

        if (isDescription) {
            descriptionCl.visibility = View.VISIBLE
            missionCl.visibility = View.GONE
            challengeDescTv.text = item.parentDetailItem?.description
            item.parentDetailItem?.reward?.name?.let { coloredText ->
                challengeDescTv.text = challengeDescTv.text
                    .partialFontAndColored(
                        Theme.bold(binding.root.context),
                        ContextCompat.getColor(binding.root.context, R.color.purple_6D6DFF),
                        coloredText
                    )
            }

        } else {
            descriptionCl.visibility = View.GONE
            missionCl.visibility = View.VISIBLE
            missionCl.onThrottleClick {
                missionClickListener.onMissionClick(item)
            }
        }
    }
}