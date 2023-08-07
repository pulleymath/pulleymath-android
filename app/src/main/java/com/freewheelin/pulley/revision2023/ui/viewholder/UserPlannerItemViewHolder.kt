package com.freewheelin.pulley.revision2023.ui.viewholder

import androidx.core.view.doOnAttach
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemUserPlannerBinding
import com.freewheelin.pulley.legacy.utils.setMarginBottom
import com.freewheelin.pulley.legacy.utils.setMarginTop
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItemType.*
import com.freewheelin.pulley.revision2023.utils.listeners.UserPlannerItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserPlannerItemViewHolder(
    private val binding: ItemUserPlannerBinding,
    private val clickListener: UserPlannerItemClickListener
): RecyclerView.ViewHolder(binding.root) {
    companion object {
        var delayClick: Boolean = false
    }
    init {
        itemView.doOnAttach {
            itemView.findViewTreeLifecycleOwner()?.let {
                binding.lifecycleOwner = it
            }
        }
    }
    fun bind(item: UserPlannerItem, position: Int, lastIndex: Int) {
        binding.apply {
            this.item = item
            this.listener = clickListener
            this.isLastIndex = position == lastIndex

            rootView.setOnClickListener {
                if (!delayClick) {
                    delayClick = true

                    clickListener.onPlanClick(item)
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        binding.bodyLl.setBackgroundResource(R.color.gray_150)
                        delay(1)
                        delayClick = false
                    }
                }
            }
            item.isSelectedUserPlan.addOnPropertyChangedCallback(object :
                Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    (sender as? ObservableBoolean)?.get()?.let { value ->
                        if (!value) {
                            bodyLl.setBackgroundResource(R.drawable.bg_white_ripple)
                        }
                    }
                }
            })
            when (item.itemType) {
                Header, NothingHeader -> {
                    headerLl.setMarginTop(if (position == 0) 10 else 0)
                }
                Footer -> {
                    footerBorderView.setMarginBottom(if (position == lastIndex) 100 else 16)
                }
                else -> {}
            }
        }
    }
}