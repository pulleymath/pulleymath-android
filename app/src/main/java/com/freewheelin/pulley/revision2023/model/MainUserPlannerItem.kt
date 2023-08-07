package com.freewheelin.pulley.revision2023.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2023.model.response.MainWeeklyPlanResponse
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanStatus
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanTag
import org.joda.time.LocalDate

data class MainUserPlannerItem (
    override var dailyPlanId: Int?,
    override val itemId: Int?,
    override val title: String,
    override val date: LocalDate,
    override var itemType: UserPlannerItemType,
    override val workbookId: Int,
    override val tag: WeeklyPlanTag?,
    val statusOfDay: WeeklyPlanStatus,
    val statusOfPlan: WeeklyPlanStatus,
    val totalProblemCount: Int?,
    val solvedProblemCount: Int?,
    val correctRate: Int?,
): UserPlannerItem(dailyPlanId, itemId, title, date, itemType, tag, workbookId), BaseDiffItem {

    val isAllPlanOfDayCompleted: Boolean
        get() {
            return statusOfDay == WeeklyPlanStatus.DONE
        }

    override fun getId(): String {
        return "${itemType.name}_${dailyPlanId}"
    }
    companion object {
        fun convertFromPlanRes(res: MainWeeklyPlanResponse): List<MainUserPlannerItem> {
            return if (res.plans.isEmpty()) {
                val nothingHeader = listOf(convertNothingHeader(res))
                val footer = listOf(convertFooter(res))
                nothingHeader + footer
            } else {
                val header = listOf(convertHeader(res))
                val body = convertBody(res)
                val footer = listOf(convertFooter(res))

                header + body + footer
            }

        }
        private fun convertHeader(plan: MainWeeklyPlanResponse): MainUserPlannerItem {
            return MainUserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = if(plan.isToday) "오늘의 일정이 없네요. 아래 버튼을 눌러 추가해보세요!" else "일정이 없어요.",
                date = plan.localDate,
                itemType = UserPlannerItemType.Header,
                statusOfDay = plan.status,
                statusOfPlan = WeeklyPlanStatus.NONE,
                tag = null,
                totalProblemCount = 0,
                solvedProblemCount = null,
                correctRate = null,
                workbookId = -1,
            )
        }

        private fun convertNothingHeader(plan: MainWeeklyPlanResponse): MainUserPlannerItem {
            return MainUserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = if(plan.isToday) "오늘의 일정이 없네요. 아래 버튼을 눌러 추가해보세요!" else "일정이 없어요.",
                date = plan.localDate,
                itemType = UserPlannerItemType.NothingHeader,
                statusOfDay = plan.status,
                statusOfPlan = WeeklyPlanStatus.NONE,
                tag = null,
                totalProblemCount = 0,
                solvedProblemCount = null,
                correctRate = null,
                workbookId = -1,
            )
        }
        private fun convertBody(plan: MainWeeklyPlanResponse): List<MainUserPlannerItem> {
            val planLastIndex = plan.plans.lastIndex
            return plan.plans.mapIndexed { index, weeklyPlan ->
                MainUserPlannerItem(
                    dailyPlanId = plan.dailyPlanId,
                    itemId = weeklyPlan.itemId,
                    title = weeklyPlan.title,
                    date = plan.localDate,
                    itemType = UserPlannerItemType.Body,
                    statusOfDay = plan.status,
                    statusOfPlan = weeklyPlan.status,
                    tag = weeklyPlan.tag,
                    totalProblemCount = weeklyPlan.progress?.totalProblemCount,
                    solvedProblemCount = weeklyPlan.progress?.solvedProblemCount,
                    correctRate = weeklyPlan.progress?.correctRate,
                    workbookId = weeklyPlan.workbookId,
                )
            }
        }

        private fun convertFooter(plan: MainWeeklyPlanResponse): MainUserPlannerItem {
            return MainUserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = "",
                date = plan.localDate,
                itemType = UserPlannerItemType.Footer,
                statusOfDay = plan.status,
                statusOfPlan = WeeklyPlanStatus.NONE,
                tag = null,
                totalProblemCount = null,
                solvedProblemCount = null,
                correctRate = null,
                workbookId = -1,
            )
        }
    }

}