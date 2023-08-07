package com.freewheelin.pulley.revision2023.ui.viewholder

import android.animation.ObjectAnimator
import android.content.Context
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemStudyPlannerBinding
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItemType
import com.freewheelin.pulley.revision2023.utils.listeners.StudyPlannerItemClickListener

class StudyPlannerItemViewHolder(
    val binding: ItemStudyPlannerBinding,
    private val listener: StudyPlannerItemClickListener
): RecyclerView.ViewHolder(binding.root) {
    private val viewContext: Context = binding.root.context

    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: StudyPlannerItem, position: Int) {
        binding.apply {
            this.item = item
            this.listener = listener
            directoryLl.visibleIf(item.itemType == StudyPlannerItemType.DIRECTORY)
            workbookLl.visibleIf(item.itemType == StudyPlannerItemType.WORKBOOK)
            
        }
    }
    fun setDepth(depth: Int) {
        binding.depth = depth
        showDepthView(depth)
    }
    fun setBackground(depth: Int, isSelected: Boolean) {
        if(depth == 0) {
            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.white))
        } else if (isSelected) {
            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.purple_gray_150))
        } else {
            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.gray_100))

        }
    }
    private fun showDepthView(depth: Int) {
        binding.invisibleStartPadding1.visibleIf(depth > 0)
        binding.invisibleStartPadding2.visibleIf(depth > 1)
        binding.invisibleContentPadding1.visibleIf(depth > 0)
        binding.invisibleContentPadding2.visibleIf(depth > 1)
        binding.invisibleContentPadding3.visibleIf(depth > 2)
    }

    fun setExpanded(expanded: Boolean?) {
        binding.apply {
            when (expanded) {
                true -> {
                    val rotateAnim = ObjectAnimator.ofFloat(expandableIndicator, "rotation", 0f, 90f)
                    rotateAnim.duration = 300
                    rotateAnim.start()

                    rotateAnim.doOnEnd {
                        expandableIndicator.rotation = 90f
                    }
                }
                false -> {
                    val rotateAnim = ObjectAnimator.ofFloat(expandableIndicator, "rotation", 90f, 0f)
                    rotateAnim.duration = 300
                    rotateAnim.start()

                    rotateAnim.doOnEnd {
                        expandableIndicator.rotation = 0f
                    }
                }
                else -> {
                }
            }
        }
    }

    fun setDivider(show: Boolean) {
        binding.bottomDivider.visibleIf(show)
    }
}