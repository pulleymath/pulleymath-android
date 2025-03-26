package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.model.UserPlannerItemType
import com.freewheelin.pulley.revision2023.model.request.UserPlanRequest
import com.freewheelin.pulley.revision2023.model.response.StudyPlannerItem
import com.freewheelin.pulley.revision2023.repository.NotesRepository
import com.freewheelin.pulley.revision2023.repository.PlannerRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.StudyPlannerAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.UserPlannerAdapter
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

class PlannerActViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {

    private val userRepository by lazy { UserRepository.instance }
    private val plannerRepository by lazy { PlannerRepository.instance }
    private val notesRepository = NotesRepository(getApplication<Application>().applicationContext, viewModelScope)
    val user = userRepository.user
    val schoolType = userRepository.schoolType

    val selectedSubjectId = MutableLiveData<Int>()

    lateinit var plannerAdapter: UserPlannerAdapter
    lateinit var studyPlannerAdapter: StudyPlannerAdapter

    val selectedMondayOfTheWeek = MutableLiveData<LocalDate>(LocalDate.now().minusDays(1))
    val selectedSundayOfTheWeek = MutableLiveData<LocalDate>(LocalDate.now().plusDays(1))
    private val _userPlanItems = MutableLiveData<List<UserPlannerItem>>()
    val userPlanItems: LiveData<List<UserPlannerItem>> = _userPlanItems

    private val _curriculumSubjects = MutableLiveData<List<CurriculumSubject>>()
    val curriculumSubjects: LiveData<List<CurriculumSubject>> = _curriculumSubjects
    val schoolTypedSubjects = MutableLiveData<List<CurriculumSubject>>()

    //size 0 으로 초기화가 필요함
    private val _studyPlanItems = MutableLiveData<List<StudyPlannerItem>>(listOf())
    val studyPlanItems: LiveData<List<StudyPlannerItem>> = _studyPlanItems
    val selectedUserPlan = MutableLiveData<UserPlannerItem>()
    val showEmptyText = MutableLiveData<Boolean>(true)

    fun schoolRenew () {
        userRepository.renewSchoolType()
    }
    fun fetchCurriculumSubjects() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val subjects = notesRepository.fetchCurriculumSubjects()
            _curriculumSubjects.postValue(subjects)
        }
    }
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
            val currentList = plannerAdapter.currentList.toMutableList()

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

//    val isE3_1ItemExist = MutableLiveData<Boolean>()
//    val isE3_2ItemExist = MutableLiveData<Boolean>()
//    val isE4_1ItemExist = MutableLiveData<Boolean>()
//    val isE4_2ItemExist = MutableLiveData<Boolean>()
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    private fun fetchAvailableElementarySubjects() {
        compositeDisposable += studyRepository.getAvailableSubject()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchAvailableElementarySubjects =>${response.data}")
                response.data?.let {
                    val availableSubjectIds = it.map { it.subjectId }
//                    isE3_1ItemExist.postValue(availableSubjectIds.contains(SubjectV3.초3_1.id))
//                    isE3_2ItemExist.postValue(availableSubjectIds.contains(SubjectV3.초3_2.id))
//                    isE4_1ItemExist.postValue(availableSubjectIds.contains(SubjectV3.초4_1.id))
//                    isE4_2ItemExist.postValue(availableSubjectIds.contains(SubjectV3.초4_2.id))
                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchAvailableElementarySubjects fetch error=${error.localizedMessage}")
            })
    }

    fun fetchStudyPlanWorkbookList(subjectId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val items = plannerRepository.fetchStudyPlanOnSubject(subjectId)
            showEmptyText.postValue(items.isEmpty())
            _studyPlanItems.postValue(items)
            val subjectIds = items.map { it.subjectId }

        }
    }

    fun expandSelectedPlan(plan: UserPlannerItem) {
        println("expandSelectedPlan ------------------------------------------")
        println("expandSelectedPlan title: ${plan.title}")
        val chapter = plan.chapterInfo ?: return
        println("expandSelectedPlan subjectId: ${chapter.subjectId}")
        println("expandSelectedPlan chapterBigId: ${chapter.chapterBigId}")
        println("expandSelectedPlan chapterMiddleId: ${chapter.chapterMiddleId}")
        println("expandSelectedPlan chapterSmallId: ${chapter.chapterSmallId}")

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val workbookId = plan.workbookId
            val subjectId = chapter.subjectId
            val items = plannerRepository.fetchStudyPlanOnSubject(subjectId)
            val subjects = notesRepository.fetchCurriculumSubjects()


            studyPlannerAdapter.clearItems()
            studyPlannerAdapter.setItems(items)
            println("expandSelectedPlan item size : ${studyPlannerAdapter.mItems.size}")
            withContext(Dispatchers.Main) {
                val subjectIdToSchoolType = subjects.find { it.id == subjectId }?.schoolType
                val subjectSchoolType = SchoolType.convertFromStr(subjectIdToSchoolType ?: "")
                userRepository.updateSchoolType(subjectSchoolType)
                selectedSubjectId.postValue(subjectId)
                studyPlannerAdapter.notifyDataSetChanged()
                showEmptyText.postValue(items.isEmpty())
            }

            val chapterItems = studyPlannerAdapter.mItems

            var bigChapterId: Int? = null
            var middleChapterId: Int? = null
            var smallChapterId: Int? = null

            chapterItems.forEach {
                (it.item as? StudyPlannerItem)?.let { bigChapter ->
                    // 대단원
                    if (bigChapter.itemType.isDirectory) {
                        bigChapter.items?.forEach { middleAddedChapter ->
                            if (middleAddedChapter.itemType.isDirectory) {
                                middleAddedChapter.items?.forEach { smallAddedChapter ->
                                    if (smallAddedChapter.itemType.isDirectory) {
                                        smallAddedChapter.items?.forEach { item ->
                                            if (item.workbookId == workbookId) {
                                                bigChapterId = bigChapter.id
                                                middleChapterId = middleAddedChapter.id
                                                smallChapterId = smallAddedChapter.id
                                            }
                                        }
                                    } else {
                                        if (smallAddedChapter.workbookId == workbookId) {
                                            bigChapterId = bigChapter.id
                                            middleChapterId = middleAddedChapter.id
                                            smallChapterId = smallAddedChapter.id
                                        }
                                    }
                                }
                            } else {
                                if (middleAddedChapter.workbookId == workbookId) {
                                    bigChapterId = bigChapter.id
                                    middleChapterId = middleAddedChapter.id
                                }
                            }
                        }
                    }
                }
            }
            println("apapap : bci : ${bigChapterId}, mci : ${middleChapterId}, sci : ${smallChapterId}")

            val bigPlan = chapterItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.id == bigChapterId } ?: return@launch
            println("expandSelectedPlan big title : ${(bigPlan.item as StudyPlannerItem).title}")
            println("expandSelectedPlan big chapterName : ${(bigPlan.item as StudyPlannerItem).chapterName}")
            val bigPlanIndex = chapterItems.indexOf(bigPlan)
            println("expandSelectedPlan bigPlanIndex : ${bigPlanIndex}")

            CoroutineScope(Dispatchers.Main).launch {
                studyPlannerAdapter.forceClickListener(bigPlanIndex) { bigAddedItems ->
                    println("middleChapterId:${middleChapterId}")
                    println("bigAddedItems outer subjectId :${subjectId} , middleChapterId : ${middleChapterId}")

                    bigAddedItems.forEach {item ->
                        (item.item as? StudyPlannerItem)?.let {
                            println("bigAddedItems [${item.depth}] - subjectId:${it.subjectId}, id:${it.id}, ${it.chapterName}")
                        }
                    }

                    val middlePlan = bigAddedItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.id == middleChapterId } ?: return@forceClickListener
                    println("expandSelectedPlan middle title : ${(middlePlan.item as StudyPlannerItem).title}")
                    println("expandSelectedPlan middle chapterName : ${(middlePlan.item as StudyPlannerItem).chapterName}")

                    val middlePlanIndex = bigAddedItems.indexOf(middlePlan)
                    println("expandSelectedPlan middlePlanIndex : ${middlePlanIndex}")

                    studyPlannerAdapter.forceClickListener(middlePlanIndex) { middleAddedItems ->
                        println("smallChapterId:${smallChapterId}")

                        println("middleAddedItems outer subjectId :${subjectId} , smallChapterId : ${smallChapterId}")

                        middleAddedItems.forEach {item ->
                            (item.item as? StudyPlannerItem)?.let {
                                println("middleAddedItems [${item.depth}] - subjectId:${it.subjectId}, id:${it.id}, ${it.chapterName ?: it.title}")
                            }
                        }


                        val smallPlan = middleAddedItems.find { (it.item as? StudyPlannerItem)?.subjectId == subjectId && (it.item as? StudyPlannerItem)?.id == smallChapterId } ?: return@forceClickListener
                        println("expandSelectedPlan small title : ${(smallPlan.item as StudyPlannerItem).title}")
                        println("expandSelectedPlan small chapterName : ${(smallPlan.item as StudyPlannerItem).chapterName}")
                        val smallPlanIndex = middleAddedItems.indexOf(smallPlan)
                        println("expandSelectedPlan smallPlanIndex : ${smallPlanIndex}")

                        studyPlannerAdapter.forceClickListener(smallPlanIndex) { allItems ->

                            println("allItems outer subjectId :${subjectId} , workbookId : ${workbookId}")

                            allItems.forEach {item ->
                                (item.item as? StudyPlannerItem)?.let {
                                    println("allItems [${item.depth}] - subjectId:${it.subjectId}, id:${it.id}, ${it.chapterName ?: it.title}")
                                }
                            }

                            val thePlan = allItems.find { (it.item as? StudyPlannerItem)?.id == workbookId } ?: return@forceClickListener
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
        val subjectId = curriculumSubjects.value?.find { it.schoolType == type.name }?.id ?: 41
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