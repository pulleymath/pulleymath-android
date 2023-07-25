package com.freewheelin.pulley.revision2023.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import org.joda.time.DateTimeConstants.FRIDAY
import org.joda.time.DateTimeConstants.MONDAY
import org.joda.time.DateTimeConstants.SATURDAY
import org.joda.time.DateTimeConstants.SUNDAY
import org.joda.time.DateTimeConstants.THURSDAY
import org.joda.time.DateTimeConstants.TUESDAY
import org.joda.time.DateTimeConstants.WEDNESDAY
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDate
import java.util.Random
import java.util.UUID


enum class MainPlannerListItemPosition {
    Header, Body, Footer
}

data class MainPlannerListItem (
//    val id: String,
    val isStudied: Boolean,
    val pieceCategory: String,
    val title: String,
    val triedProblemCount: Int,
    val totalProblemCount: Int,
    val problemCorrectRate: Int,
    val plannedDate: LocalDate, // LocalDate convert
    val itemPosition: MainPlannerListItemPosition,
    val dateAppear: DateAppear,
): BaseDiffItem {

    enum class DateAppear {
        None, DayOfTheWeek, DateNumber, All
    }

    val isToday: Boolean
        get() {
            val now = LocalDate.now()
            return now.isEqual(plannedDate)
        }

    val dayOfWeek: String
        get() {
            return when (plannedDate.dayOfWeek) {
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
            return plannedDate.dayOfMonth.toString()
        }

    companion object {
        fun makeDummyHeader(plannedDate: LocalDate): MainPlannerListItem {
            val pieceCategory = getPieceCategory()
            val randomNumber = Random().nextInt(1000)
            val tried = randomNumber % 10
            val total = 10
            val rate:Float = (tried.toFloat() / total) * 100
            println("aspasp tried : ${tried}, rate: ${rate}, pcr : ${rate.toInt()}")
            return MainPlannerListItem(
//                id = "${randomNumber}",
                isStudied = randomNumber % 2 == 0,
                pieceCategory = pieceCategory,
                title = "수학공부를 해보자",
                triedProblemCount = tried,
                totalProblemCount = total,
                problemCorrectRate = rate.toInt(),
                plannedDate = plannedDate,
                itemPosition = MainPlannerListItemPosition.Header,
                dateAppear = DateAppear.None
            )
        }
        fun makeDummyBody(plannedDate: LocalDate, dateAppear: DateAppear): MainPlannerListItem {
            val pieceCategory = getPieceCategory()
            val randomNumber = Random().nextInt(1000)
            val tried = randomNumber % 10
            val total = 10
            val rate:Float = (tried.toFloat() / total) * 100
            return MainPlannerListItem(
//                id = "${randomNumber}",
                isStudied = randomNumber % 2 == 0,
                pieceCategory = pieceCategory,
                title = "수학공 바디 부를 해보자  ${randomNumber}",
                triedProblemCount = tried,
                totalProblemCount = total,
                problemCorrectRate = rate.toInt(),
                plannedDate = plannedDate,
                itemPosition = MainPlannerListItemPosition.Body,
                dateAppear = dateAppear
            )
        }
        fun makeDummyFooter(plannedDate: LocalDate): MainPlannerListItem {
            val pieceCategory = getPieceCategory()
            val randomNumber = Random().nextInt(1000)
            val tried = randomNumber % 10
            val total = 10
            val rate:Float = (tried.toFloat() / total) * 100
            return MainPlannerListItem(
//                id = "${randomNumber}",
                isStudied = randomNumber % 2 == 0,
                pieceCategory = pieceCategory,
                title = "푸터를 해보자",
                triedProblemCount = tried,
                totalProblemCount = total,
                problemCorrectRate = rate.toInt(),
                plannedDate = plannedDate,
                itemPosition = MainPlannerListItemPosition.Footer,
                dateAppear = DateAppear.None
            )
        }
        fun getPieceCategory(): String {
            val randomNumber = Random().nextInt(1000)
            return  when(randomNumber % 6) {
                0 -> "개념"
                1 -> "문제집"
                2 -> "워크북"
                3 -> "모의고사"
                4 -> "풀리북스"
                5 -> "오답노트"
                else -> "추천학습"
            }
        }

        fun getItemList(): List<MainPlannerListItem> {
            return listOf(
                makeDummyHeader(LocalDate.now()),
                makeDummyBody(LocalDate.now(), DateAppear.None),
                makeDummyBody(LocalDate.now(), DateAppear.None),
                makeDummyBody(LocalDate.now(), DateAppear.DayOfTheWeek),
                makeDummyBody(LocalDate.now(), DateAppear.DateNumber),
                makeDummyBody(LocalDate.now(), DateAppear.None),
                makeDummyBody(LocalDate.now(), DateAppear.None),
                makeDummyFooter(LocalDate.now()),
                makeDummyHeader(LocalDate(2023, 7, 19)),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.All),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyBody(LocalDate(2023, 7, 19), DateAppear.None),
                makeDummyFooter(LocalDate(2023, 7, 19)),
            )
        }
    }

    override fun getId(): String {
        return UUID.randomUUID().toString()
    }
}