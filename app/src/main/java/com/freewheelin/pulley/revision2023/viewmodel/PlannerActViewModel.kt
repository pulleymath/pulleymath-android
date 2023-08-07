package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItemType
import com.freewheelin.pulley.revision2023.model.request.UserPlanRequest
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.repository.PlannerRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.StudyPlannerAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.UserPlannerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate

class PlannerActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {

    private val userRepository by lazy { UserRepository.instance }
    private val plannerRepository by lazy { PlannerRepository.instance }
    val user = userRepository.user
    val schoolType = userRepository.schoolType

    val selectedSubjectId = MutableLiveData<Int>()

    lateinit var plannerAdapter: UserPlannerAdapter
    lateinit var studyPlannerAdapter: StudyPlannerAdapter

    val selectedMondayOfTheWeek = MutableLiveData<LocalDate>(LocalDate.now().minusDays(1))
    val selectedSundayOfTheWeek = MutableLiveData<LocalDate>(LocalDate.now().plusDays(1))
    private val _userPlanItems = MutableLiveData<List<UserPlannerItem>>()
    val userPlanItems: LiveData<List<UserPlannerItem>> = _userPlanItems

    //size 0 으로 초기화가 필요함
    private val _studyPlanItems = MutableLiveData<List<StudyPlannerItem>>(listOf())
    val studyPlanItems: LiveData<List<StudyPlannerItem>> = _studyPlanItems
    val selectedUserPlan = MutableLiveData<UserPlannerItem>()
    val showEmptyText = MutableLiveData<Boolean>(false)

    fun updateUserPlanList(monday: String? = null, sunday: String? = null) {
        val datePair = if (monday != null && sunday != null) {
            Pair(monday, sunday)
        } else {
            plannerRepository.initPlannerWeek()
        }
        selectedMondayOfTheWeek.postValue(LocalDate(datePair.first))
        selectedSundayOfTheWeek.postValue(LocalDate(datePair.second))

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val items = plannerRepository.fetchUserPlanList(datePair)
            _userPlanItems.postValue(items)
            val todayPlan = items.find { it.isToday || ( !it.isThisWeek && it.isMonday ) }
            selectedUserPlan.postValue(todayPlan ?: return@launch)
        }
    }

    fun postDailyPlan(userPlan: UserPlannerItem, studyPlan: StudyPlannerItem) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            UserPlanRequest.convertFromUserPlannerItem(userPlan, studyPlan)?.let { req ->
                val plan = plannerRepository.postUserPlan(req)
                var currentList = plannerAdapter.currentList.toMutableList()

                val lastPlanOfDate = currentList.findLast {
                    it.date.isEqual(plan.date) && (it.itemType.isBody || it.itemType.isNothingHeader)
                } ?: return@launch

                currentList = currentList.map {
                    if (it.date.isEqual(plan.date)) {
                        when (it.itemType) {
                            UserPlannerItemType.NothingHeader -> {
                                it.updateItemType(UserPlannerItemType.Header)
                                it.dailyPlanId = plan.dailyPlanId
                            }
                            UserPlannerItemType.Footer -> {
                                it.dailyPlanId = plan.dailyPlanId
                            }
                            else -> {}
                        }
                    }
                    it
                }.toMutableList()



                val userPlannerItem = UserPlannerItem.convertDailyPlanRes(plan, lastPlanOfDate.date)
                userPlannerItem.isSelectedDate.set(true)
                val lastIndexOfSameDate = currentList.indexOf(lastPlanOfDate)

                currentList.add(lastIndexOfSameDate + 1, userPlannerItem)
                _userPlanItems.postValue(currentList)

            }
        }
    }
    fun deleteUserPlan(item: UserPlannerItem) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            plannerRepository.deleteUserPlan(item)
            var currentList = plannerAdapter.currentList.toMutableList()

            val itemIndex = currentList.indexOf(item)
            currentList.removeAt(itemIndex)

            val newGroups = currentList.filterNot { it.itemType.isFooter }.groupBy { it.dailyPlanId }

            for (key in newGroups.keys) {
                newGroups[key]?.let { group ->
                    val lastItem = group.last()
                    if (lastItem.itemType.isHeader) {
                        currentList.find { it == lastItem }?.updateItemType(UserPlannerItemType.NothingHeader)
                    }
                }
            }
            _userPlanItems.postValue(currentList)

        }
    }

    fun fetchStudyPlanWorkbookList(subjectId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val items = plannerRepository.fetchStudyPlanOnSubject(subjectId)
            showEmptyText.postValue(items.isNotEmpty())
            _studyPlanItems.postValue(items)
        }
    }

    fun expandSelectedPlan(plan: UserPlannerItem) {
        val chapter = plan.chapterInfo ?: return

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val workbookId = plan.workbookId
            val subjectId = chapter.subjectId
            val items = plannerRepository.fetchStudyPlanOnSubject(subjectId)

            val bigChapterId = chapter.chapterBigId ?: -1
            val middleChapterId = chapter.chapterMiddleId
            val smallChapterId = chapter.chapterSmallId

            studyPlannerAdapter.clearItems()
            studyPlannerAdapter.setItems(items)
            withContext(Dispatchers.Main) {
                val schoolType1 = SubjectV3.codeToSchoolType(subjectId)
                userRepository.updateSchoolType(schoolType1)
                selectedSubjectId.postValue(subjectId)
                studyPlannerAdapter.notifyDataSetChanged()
                showEmptyText.postValue(items.isNotEmpty())
            }


            println("bigChapterId:${bigChapterId}")
            val chapterItems = studyPlannerAdapter.mItems
            val bigPlan = chapterItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.chapterId == bigChapterId } ?: return@launch
            val bigPlanIndex = chapterItems.indexOf(bigPlan)

            CoroutineScope(Dispatchers.Main).launch {
                studyPlannerAdapter.forceClickListener(bigPlanIndex) { bigAddedItems ->
                    println("middleChapterId:${middleChapterId}")
                    val middlePlan = bigAddedItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.chapterId == middleChapterId } ?: return@forceClickListener
                    val middlePlanIndex = bigAddedItems.indexOf(middlePlan)

                    studyPlannerAdapter.forceClickListener(middlePlanIndex) { middleAddedItems ->
                        println("smallChapterId:${smallChapterId}")
                        val smallPlan = bigAddedItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.chapterId == smallChapterId } ?: return@forceClickListener
                        val smallPlanIndex = bigAddedItems.indexOf(smallPlan)

                        studyPlannerAdapter.forceClickListener(smallPlanIndex) { allItems ->
                            val thePlan = allItems.find { (it.item as? StudyPlannerItem)?.workbookId == workbookId } ?: return@forceClickListener
                            val planIndex = allItems.indexOf(thePlan)
                            studyPlannerAdapter.changePlanBgColor(planIndex)
                        }
                    }
                }
            }
        }
    }

    fun initSchoolType(type: SchoolType) {
        userRepository.updateSchoolType(type)
    }
    fun updateSchoolType(type: SchoolType) {
        userRepository.updateSchoolType(type)
        val subjectId = if (type.isMiddle) {
            SubjectV3.중1_1.id
        } else {
            SubjectV3.수학_상.id
        }
        onHeaderSubjectBtnClick(subjectId)
    }
    fun onHeaderSubjectBtnClick(subjectId: Int) {
        if (this.selectedSubjectId.value == subjectId) return
        this.selectedSubjectId.postValue(subjectId)
        // observe로 넣지 않은이유 : expandSelectedPlan할때 fetch가 두번되지않게하기위함이다.
        fetchStudyPlanWorkbookList(subjectId)
    }

    fun moveNextPlannerWeek() {
        val presentedMonday = selectedMondayOfTheWeek.value ?: return
        val presentedSunday = selectedSundayOfTheWeek.value ?: return
        val nextMonday = presentedMonday.plusWeeks(1)
        val nextSunday = presentedSunday.plusWeeks(1)
        selectedMondayOfTheWeek.postValue(nextMonday)
        selectedSundayOfTheWeek.postValue(nextSunday)

        updateUserPlanList(nextMonday.toString("yyyy-MM-dd"), nextSunday.toString("yyyy-MM-dd"))
    }
    fun movePrevPlannerWeek() {
        val presentedMonday = selectedMondayOfTheWeek.value ?: return
        val presentedSunday = selectedSundayOfTheWeek.value ?: return
        val prevMonday = presentedMonday.minusWeeks(1)
        val prevSunday = presentedSunday.minusWeeks(1)
        selectedMondayOfTheWeek.postValue(prevMonday)
        selectedSundayOfTheWeek.postValue(prevSunday)

        updateUserPlanList(prevMonday.toString("yyyy-MM-dd"), prevSunday.toString("yyyy-MM-dd"))
    }
}