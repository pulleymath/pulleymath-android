package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.BookFilterElement

interface BookFilterItemListener {
    fun onToggle(isChecked: Boolean)
    fun onFilterItemClick(item: BookFilterElement)
    fun onCalendar()
}