package com.freewheelin.pulley.revision2023.model.response

import com.zoyi.channel.plugin.android.annotation.Failed
import org.joda.time.LocalDate

class WeeklyPlanResponse (
    val date: String,
    val dailyPlanId: Int?,
    val plans: List<WeeklyPlan>,
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
class WeeklyPlan(
    val itemId: Int,
    val tag: WeeklyPlanTag?,
    val title: String,
    val workbookId: Int, // 범용적인 학습지 id를 표현하고 싶었다고 한다.
    val isHomework: Boolean,
    val chapterInfo: PlanChapterInfo?,
) {

}

data class PlanChapterInfo (
    val subjectId: Int,
    val chapterBigId: Int?,
    val chapterMiddleId: Int?,
    val chapterSmallId: Int?,
)
enum class WeeklyPlanStatus {
    NONE, ING, DONE, FAILED;

    val isNone: Boolean
        get() {
            return this == NONE
        }
    val isIng: Boolean
        get() {
            return this == ING
        }
    val isDone: Boolean
        get() {
            return this == DONE
        }
    val isFailed: Boolean
        get() {
            return this == FAILED
        }
}

data class WeeklyPlanProgress(
    val totalProblemCount: Int,
    val solvedProblemCount: Int,
    val correctRate: Int?
) {

}

enum class WeeklyPlanTag {
    CONCEPT, PRACTICE, PULLEY_WORKBOOK, CUSTOM_WORKBOOK, MOCK, COMMERCIAL_BOOK, RECOMMEND, NOTE, TEACHER;

    val inKorean: String
        get() {
            return when (this) {
                CONCEPT -> "개념"
                PRACTICE -> "연습문제"
                PULLEY_WORKBOOK -> "풀리문제집"
                CUSTOM_WORKBOOK -> "워크북"
                MOCK -> "모의고사"
                COMMERCIAL_BOOK -> "풀리북스"
                RECOMMEND -> "추천학습"
                NOTE -> "오답노트"
                TEACHER -> "선생님"
            }
        }
}