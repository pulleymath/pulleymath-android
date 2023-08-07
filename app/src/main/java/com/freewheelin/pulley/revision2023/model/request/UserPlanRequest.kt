package com.freewheelin.pulley.revision2023.model.request

import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import java.io.Serializable

class UserPlanRequest (
    val targetDate: String,
    val dailyPlanId: Int?,
    val studyPlanBookId: Int,
): Serializable {

    companion object {
        fun convertFromUserPlannerItem(dailyPlan: UserPlannerItem, item: StudyPlannerItem): UserPlanRequest? {
            return UserPlanRequest (
                targetDate = dailyPlan.date.toString("yyyy-MM-dd"),
                dailyPlanId = dailyPlan.dailyPlanId,
                studyPlanBookId = item.studyPlanBookId ?: return null,
            )
        }
    }
}