package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem

fun interface ChallengeClickListener {
    fun onChallengeHeaderClick(item: MainChallengeHeaderItem) // TODO model
}

fun interface ChallengeMissionClickListener {
    fun onMissionClick(item: ChallengeCourse) // TODO model
}