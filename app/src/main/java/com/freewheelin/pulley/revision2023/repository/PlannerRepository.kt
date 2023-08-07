package com.freewheelin.pulley.revision2023.repository

import com.freewheelin.pulley.revision2023.model.MainUserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.model.request.UserPlanRequest
import com.freewheelin.pulley.revision2023.model.response.UserPlanResponse
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.service.PlannerApi
import com.freewheelin.pulley.revision2023.service.PlannerService
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

class PlannerRepository() {

    companion object {
        val instance: PlannerRepository by lazy { PlannerRepository() }
    }
    private val api: PlannerService by lazy { PlannerApi.plannerService() }

    suspend fun getWeeklyPlans(datePair: Pair<String, String>): List<MainUserPlannerItem> {
        println("getweeklyPlans 1")
        val monday = datePair.first
        val sunday = datePair.second
        println("getweeklyPlans 2")
        val list = api.getWeeklyPlans(monday, sunday).data?.let {
            println("getweeklyPlans 3")
            val mainPlannerList = mutableListOf<MainUserPlannerItem>()
            it.forEach {
                val list = MainUserPlannerItem.convertFromPlanRes(it)
                mainPlannerList.addAll(list)
            }

            mainPlannerList
        }

        println("getweeklyPlans 4")
        return list?.toList() ?: listOf()
    }

    suspend fun postUserPlan(req: UserPlanRequest): UserPlanResponse {
        return api.postUserPlan(req).data
    }
    suspend fun deleteUserPlan(item: UserPlannerItem) {
        val dailyPlanId = item.dailyPlanId
        val itemId = item.itemId
        api.deleteUserPlan(
            dailyPlanId = dailyPlanId ?: return,
            itemId = itemId ?: return
        )
    }

    suspend fun fetchUserPlanList(datePair: Pair<String, String>): List<UserPlannerItem> {
        val monday = datePair.first
        val sunday = datePair.second
        val list = api.fetchUserPlanList(monday, sunday).data.let {
            val planList = mutableListOf<UserPlannerItem>()
            it.forEach {
                val list = UserPlannerItem.convertFromPlanRes(it)
                planList.addAll(list)
            }
            planList
        }
        return list.toList()
    }

    suspend fun fetchStudyPlanOnSubject(subjectId: Int): List<StudyPlannerItem> {
        return api.fetchStudyPlanOnSubject(subjectId).data
//            .let {
//            it.forEach { it.isSelected2.set(false) }
//            it
//        }
    }

    fun initPlannerWeek(): Pair<String, String> {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val daysAgoAtMonday = when (dayOfWeek) {
            DateTimeConstants.MONDAY -> 0
            DateTimeConstants.TUESDAY -> 1
            DateTimeConstants.WEDNESDAY -> 2
            DateTimeConstants.THURSDAY -> 3
            DateTimeConstants.FRIDAY -> 4
            DateTimeConstants.SATURDAY -> 5
            DateTimeConstants.SUNDAY -> 6
            else -> 0
        }
        val daysLeftUntilSunday = when (dayOfWeek) {
            DateTimeConstants.MONDAY -> 6
            DateTimeConstants.TUESDAY -> 5
            DateTimeConstants.WEDNESDAY -> 4
            DateTimeConstants.THURSDAY -> 3
            DateTimeConstants.FRIDAY -> 2
            DateTimeConstants.SATURDAY -> 1
            DateTimeConstants.SUNDAY -> 0
            else -> 0
        }
        return Pair(
            today.minusDays(daysAgoAtMonday).toString("yyyy-MM-dd"),
            today.plusDays(daysLeftUntilSunday).toString("yyyy-MM-dd")
        )
    }
}