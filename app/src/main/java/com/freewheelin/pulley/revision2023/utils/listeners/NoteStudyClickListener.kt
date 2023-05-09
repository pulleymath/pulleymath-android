package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.OrderType
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.model.PriorConcept

interface NoteStudyClickListener {
    fun onAllSelectedClicked(isChecked: Boolean)
    fun onCardCheckBoxClicked(isChecked: Boolean, item: NoteStudyProblemWrapper)
    fun onCardItemDetail(item: NoteStudyProblemWrapper)
    fun onOrderChanged(type: OrderType)
}