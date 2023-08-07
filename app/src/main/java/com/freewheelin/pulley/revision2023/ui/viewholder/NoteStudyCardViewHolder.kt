package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemNoteStudyCardBinding
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.utils.listeners.NoteStudyClickListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteActViewModel
import com.squareup.picasso.Picasso

class NoteStudyCardViewHolder(val binding: ItemNoteStudyCardBinding, val viewModel: WrongNoteActViewModel, val listener: NoteStudyClickListener): RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.doOnAttach {
            binding.lifecycleOwner = itemView.findViewTreeLifecycleOwner()
        }
        itemView.doOnDetach {
            binding.lifecycleOwner = null
        }
    }
    fun bind(item: NoteStudyProblemWrapper) = with(binding) {
        this.item = item

        val problem = item.problem
        Picasso.get()
            .load(problem?.getProblemUrl())
            .fit()
            .centerInside()
            .into(problemSdv)

        itemView.setOnClickListener {
            val isChecked = !checkBox.isChecked
            checkBox.isChecked = isChecked
            item.isSelected.set(isChecked)
//            listener.onCardCheckBoxClicked(isChecked, item)
        }
//        item.isSelected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
//            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                val isChecked = (sender as ObservableBoolean).get()
//                println("zxozxo ischecked changed : ${isChecked}")
//            }
//
//        })
        checkBox.setOnCheckedChangeListener { button, isChecked ->
            item.isSelected.set(isChecked)
            listener.onCardCheckBoxClicked(isChecked, item)
        }
        detailBtn.setOnClickListener {
            listener.onCardItemDetail(item)
        }
    }

}