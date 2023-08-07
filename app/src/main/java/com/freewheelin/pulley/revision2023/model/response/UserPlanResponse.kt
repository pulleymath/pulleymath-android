package com.freewheelin.pulley.revision2023.model.response

import org.joda.time.LocalDate
import java.io.Serializable

class UserPlanResponse(
    val itemId: Int,
    val tag: WeeklyPlanTag,
    val title: String,
    val workbookId: Int,
    val dailyPlanId: Int,
    val targetDate: String,
    val chapterInfo: PlanChapterInfo
): Serializable {

    val date: LocalDate
        get() {
            return LocalDate(targetDate)
        }
}