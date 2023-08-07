package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.UserPlannerItem

interface UserPlannerItemClickListener {
    fun onPlanClick(item: UserPlannerItem)
    fun onRemoveItemClick(item: UserPlannerItem)
}