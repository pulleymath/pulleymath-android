package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.view.doOnAttach
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemBookFilterBinding
import com.freewheelin.pulley.databinding.ItemPriorConceptBinding
import com.freewheelin.pulley.revision2021.views.LabelFlowView
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel

class BookFilterItemViewHolder(
    private val binding: ItemBookFilterBinding,
    private val itemListener: BookFilterItemListener
): RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: BookFilterElement) = with(binding) {
        this.item = item
        this.listener = itemListener

//        item.isSelected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
//            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                println("aspasp item.${item.name}, (sender as ObservableBoolean).get() ${(sender as ObservableBoolean).get()}")
//            }
//        })
        filterBtn.setOnClickListener {
//            item.isSelected.set(item.isSelected.get().not())
            itemListener.onFilterItemClick(item)
        }
    }
}