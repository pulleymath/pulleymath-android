package com.freewheelin.pulley.revision2023.model.response

import org.joda.time.LocalDate
import java.io.Serializable

data class MainWeeklyPlanResponse (
    val date: String,
    val dailyPlanId: Int?,
    val plans: List<MainWeeklyPlan>,
    val status: WeeklyPlanStatus,
) {
    val isToday: Boolean
        get() {
            val now = LocalDate.now()
            val localDate = LocalDate(date)
            return now.isEqual(localDate)
        }
    val localDate: LocalDate
        get() {
            return LocalDate(date)
        }
}
class MainWeeklyPlan(
    val itemId: Int,
    val tag: WeeklyPlanTag?,
    val title: String,
    val workbookId: Int, // 범용적인 학습지 id를 표현하고 싶었다고 한다.
    val status: WeeklyPlanStatus,
    val studyPlanBookId: Int?,
    val isHomework: Boolean,
    val progress: WeeklyPlanProgress?,
): Serializable {

}