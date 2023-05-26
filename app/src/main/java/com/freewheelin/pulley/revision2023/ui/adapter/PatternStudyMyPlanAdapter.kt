package com.freewheelin.pulley.revision2023.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.activities.learning.tabFragment.book.*
import com.freewheelin.pulley.databinding.ItemBookPlanV2Binding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel

class PatternStudyMyPlanAdapter(
    private val planListener: PlanListenerV2,
    private val actions: List<ActionType>,
    private val type: OriginType,
    private val isGridLayout: Boolean = true,
    private val viewModel: BaseAndroidViewModel? = null
    ): ListAdapter<Book, RecyclerView.ViewHolder>(DiffCallback<Book>()) {
    private val challengeRepository by lazy { ChallengeRepository.instance }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemBookPlanV2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookPlanV2Holder(binding, planListener, actions, isGridLayout, viewModel)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList[holder.absoluteAdapterPosition].also { item ->
            (holder as BookPlanV2Holder).apply {
                val showChallengeStamp = isShowChallengeStamp(item)
                bind(item, showChallengeStamp)
            }
        }
    }

    fun isShowChallengeStamp(item: Book): Boolean {
        val joinedChallengeList = challengeRepository.joinedChallengeList
        return when (type) {
            OriginType.PulleyMathTotal -> {
                val visible = joinedChallengeList.value?.find {
                    it.startChallenge?.isPulleyBooksCourseInProgress == true
                } != null

                visible && item.isStartChallengeBookPiece()
            }
            else -> false
        }
    }
    enum class OriginType {
        Recommend,
        Workbook,
        PulleyMathTotal,
        MyPlan

    }
}