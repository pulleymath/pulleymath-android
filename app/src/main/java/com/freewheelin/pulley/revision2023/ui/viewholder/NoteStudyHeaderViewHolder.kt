package com.freewheelin.pulley.revision2023.ui.viewholder

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.revision2023.ui.fragment.OrderType
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.databinding.ItemNoteStudyHeaderBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.utils.listeners.NoteStudyClickListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteStudyViewModel
import com.freewheelin.pulley.legacy.utils.visibleIf

class NoteStudyHeaderViewHolder(val binding: ItemNoteStudyHeaderBinding, val viewModel: WrongNoteStudyViewModel, val listener: NoteStudyClickListener): RecyclerView.ViewHolder(binding.root) {
    var orderBtns = listOf<Button>()

    init {
        binding.apply {
            orderBtns = listOf(recentOrder, oldOrder, subjectOrder, levelOrder)

            itemView.doOnAttach {
                lifecycleOwner = itemView.findViewTreeLifecycleOwner()
            }
            itemView.doOnDetach {
                lifecycleOwner = null
            }
        }
    }
    fun bind(item: NoteStudyProblemWrapper) = with(binding) {
        this.item = item
        this.vm = viewModel
        val problems = item.problems
        selectedOrder = viewModel.selectedOrder

//        clearGuideTv.text = "전체 ${problems.size}문제 중 ${problems.filter { it.isClear }.size}개 클리어"
//        problemCntTv.text = "${problems.size}개의 문제가 있습니다."

        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = problems.isNotEmpty() && viewModel.selectedProblem.value?.containsAll(problems) == true
        checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
            checkBox.isChecked = isChecked
            listener.onAllSelectedClicked(isChecked)
        }
//        guideView.visibleIf(problems.isEmpty())
        guideTv.text = if (viewModel.tabPosition == 0) {
            "오답문제가 이곳에 모여요!\n간편한 오답학습을 경험해보세요 :)"
        } else {
            "즐겨찾기한 문제가 이곳에 모여요!\n다시 보고 싶거나, 중요하다고 생각한 문제를 모아보세요 :)"
        }

        orderBtns.forEach {
            it.setOnClickListener {
                selectedOrder = when (it) {
                    recentOrder -> OrderType.recent
                    oldOrder -> OrderType.old
                    subjectOrder -> OrderType.subject
                    levelOrder -> OrderType.level
                    else -> OrderType.recent
                }
                listener.onOrderChanged(selectedOrder)
            }
        }

    }

    var selectedOrder: OrderType = OrderType.recent
        set(value) {
            field = value
            orderBtns.forEach { setOrderBtnUnselected(it) }
            setOrderBtnSelected(orderBtns[value.rawValue])
        }
    private fun setOrderBtnUnselected(button: Button) {
        val view = binding.root
        button.typeface = Theme.regular(view.context)
    }
    private fun setOrderBtnSelected(button: Button) {
        val view = binding.root
        button.typeface = Theme.bold(view.context)
    }
}