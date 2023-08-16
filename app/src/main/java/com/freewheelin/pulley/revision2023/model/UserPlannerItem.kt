package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2023.model.response.UserPlanResponse
import com.freewheelin.pulley.revision2023.model.response.PlanChapterInfo
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanResponse
import com.freewheelin.pulley.revision2023.model.response.WeeklyPlanTag
import org.joda.time.DateTimeConstants.FRIDAY
import org.joda.time.DateTimeConstants.MONDAY
import org.joda.time.DateTimeConstants.SATURDAY
import org.joda.time.DateTimeConstants.SUNDAY
import org.joda.time.DateTimeConstants.THURSDAY
import org.joda.time.DateTimeConstants.TUESDAY
import org.joda.time.DateTimeConstants.WEDNESDAY
import org.joda.time.LocalDate


enum class UserPlannerItemType {
    Header, Body, Footer, NothingHeader;

    val isHeader: Boolean
        get() {
            return this == Header
        }

    val isNothingHeader: Boolean
        get() {
            return this == NothingHeader
        }
    val isHeaderPart: Boolean
        get() {
            return this == Header || this == NothingHeader
        }
    val isFooter: Boolean
        get() {
            return this == Footer
        }
    val isBody: Boolean
        get() {
            return this == Body
        }
}

open class UserPlannerItem (
    open var dailyPlanId: Int?,
    open val itemId: Int?,
    open val title: String,
    open val date: LocalDate,
    open var itemType: UserPlannerItemType,
    open val tag: WeeklyPlanTag?,
    open val workbookId: Int,
    val chapterInfo: PlanChapterInfo? = null
): BaseDiffItem {
    override fun getId(): String {
        return "${itemType.name}_${dailyPlanId}_${itemId}"
    }
    val userPlanId: String
        get() {
            return "${date}_${dailyPlanId}"
        }

    var isSelectedDate: ObservableBoolean = ObservableBoolean(false)
    var isSelectedUserPlan: ObservableBoolean = ObservableBoolean(false)
    fun updateItemType(type: UserPlannerItemType) {
        itemType = type
        plannerItemTypeOb.set(type)
    }
    var plannerItemTypeOb: ObservableField<UserPlannerItemType> = ObservableField(itemType)


    val isToday: Boolean
        get() {
            val now = LocalDate.now()
            return now.isEqual(date)
        }
    val isPast: Boolean
        get() {
            val now = LocalDate.now()
            return now.isAfter(date)
        }
    val isFuture: Boolean
        get() {
            val now = LocalDate.now()
            return now.isBefore(date)
        }

    val isThisWeek: Boolean
        get() {
            val nowWeekOfWeekYear = LocalDate.now().weekOfWeekyear
            val dateWeekOfWeekYear = date.weekOfWeekyear
            return nowWeekOfWeekYear == dateWeekOfWeekYear
        }
    val isMonday: Boolean
        get() {
            return date.dayOfWeek == MONDAY
        }

    val tagStr: String
        get() {
            return tag?.inKorean ?: ""
        }

    val dayOfWeek: String
        get() {
            return when (date.dayOfWeek) {
                MONDAY -> "월"
                TUESDAY -> "화"
                WEDNESDAY -> "수"
                THURSDAY -> "목"
                FRIDAY -> "금"
                SATURDAY -> "토"
                SUNDAY -> "일"
                else -> "일"
            }
        }
    val dayOfMonth: String
        get() {
            return date.dayOfMonth.toString()
        }

    companion object {
        fun convertFromPlanRes(res: WeeklyPlanResponse): List<UserPlannerItem> {
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
        private fun convertHeader(plan: WeeklyPlanResponse): UserPlannerItem {
            return UserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = if (plan.isToday) "오늘의 일정이 없네요. 새로 추가해보세요!" else "일정이 없어요.",
                date = plan.localDate,
                itemType = UserPlannerItemType.Header,
                tag = null,
                workbookId = -1,
            )
        }
        private fun convertBody(plan: WeeklyPlanResponse): List<UserPlannerItem> {
//            val planLastIndex = plan.plans.lastIndex
            return plan.plans.mapIndexed { index, weeklyPlan ->
                UserPlannerItem(
                    dailyPlanId = plan.dailyPlanId,
                    itemId = weeklyPlan.itemId,
                    title = weeklyPlan.title,
                    date = plan.localDate,
                    itemType = UserPlannerItemType.Body,
                    tag = weeklyPlan.tag,
                    workbookId = weeklyPlan.workbookId,
                    chapterInfo = weeklyPlan.chapterInfo
                )
            }
        }
        private fun convertNothingHeader(plan: WeeklyPlanResponse): UserPlannerItem {
            return UserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = if (plan.isToday) "오늘의 일정이 없네요. 새로 추가해보세요!" else "일정이 없어요.",
                date = plan.localDate,
                itemType = UserPlannerItemType.NothingHeader,
                tag = null,
                workbookId = -1,
            )
        }
        private fun convertFooter(plan: WeeklyPlanResponse): UserPlannerItem {
            return UserPlannerItem(
                dailyPlanId = plan.dailyPlanId,
                itemId = null,
                title = "",
                date = plan.localDate,
                itemType = UserPlannerItemType.Footer,
                tag = null,
                workbookId = -1,
            )
        }

        fun convertDailyPlanRes(res: UserPlanResponse, date: LocalDate): UserPlannerItem {
            return UserPlannerItem(
                dailyPlanId = res.dailyPlanId,
                itemId = res.itemId,
                title = res.title,
                date = date,
                itemType = UserPlannerItemType.Body,
                tag = res.tag,
                workbookId = res.workbookId,
                chapterInfo = res.chapterInfo
            )
        }
    }

}