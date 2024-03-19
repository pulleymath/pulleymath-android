package com.freewheelin.pulley.revision2023.ui.viewholder

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.databinding.ItemMainPlannerBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.setMarginBottom
import com.freewheelin.pulley.legacy.utils.setMarginTop
import com.freewheelin.pulley.legacy.utils.setOnBasicCOrHigherClickListener
import com.freewheelin.pulley.legacy.utils.setOnBasicPOrHigherClickListener
import com.freewheelin.pulley.legacy.utils.setOnJoinedUserClickListener
import com.freewheelin.pulley.legacy.utils.setOnPremiumClickListener
import com.freewheelin.pulley.revision2023.model.MainUserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItemType.*
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanTag
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
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
    private fun setListener(item: MainUserPlannerItem, cb: (view: View) -> Unit, deniedCb: (view: View) -> Unit) {
        when (item.tag) {
            WeeklyPlanTag.CONCEPT -> {
                binding.rootView.setOnBasicCOrHigherClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.PRACTICE -> {
                binding.rootView.setOnBasicPOrHigherClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.PULLEY_WORKBOOK -> {
                binding.rootView.setOnBasicPOrHigherClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.MOCK -> {
                binding.rootView.setOnJoinedUserClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.CUSTOM_WORKBOOK, WeeklyPlanTag.COMMERCIAL_BOOK -> {
                binding.rootView.setOnPremiumClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.RECOMMEND, WeeklyPlanTag.NOTE -> {
                binding.rootView.setOnBasicPOrHigherClickListener(cb = cb, deniedCb = deniedCb)
            }
            WeeklyPlanTag.TEACHER -> {
                binding.rootView.setOnBasicPOrHigherClickListener(cb = cb, deniedCb = deniedCb)
            }
            else -> {}
        }
    }
    fun bind(item: MainUserPlannerItem, position: Int, lastIndex: Int) {
        binding.apply {
            this.item = item
            this.listener = clickListener
            this.isLastIndex = position == lastIndex

            setListener(item, cb = {
               clickListener.onPlanClick(item)
            }, deniedCb = {
                val context = binding.root.context
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "메인_플래너", "유도_다이얼로그", "is_guest=${user?.serviceType?.isGuestUser}")
                val dialog = if (user?.serviceType?.isGuestUser == true) {
                    JoinInduceForGuestDialog()
                } else {
                    PurchaseGuideDialog.newInstance()
                }

                if (context is AppCompatActivity && !context.isFinishing) {
                    val fm = context.supportFragmentManager
                    if (!fm.isDestroyed) {
                        try {
                            fm.let { dialog.show(it, "MainUserPlannerItem_InduceDialog") }
                        } catch (e: IllegalStateException) {
                            fm.beginTransaction().add(dialog, "MainUserPlannerItem_InduceDialog").commitAllowingStateLoss()
                        }
                    }
                }
            })


            when (item.itemType) {
                Header , NothingHeader -> {
//                    headerLl.setMarginTop(if (position == 0) 24 else 0)
                }
                Footer -> {
                    footerBorderView.setMarginBottom(if (position == lastIndex) 100 else 16)
                }
                else -> {}
            }
        }
    }
}