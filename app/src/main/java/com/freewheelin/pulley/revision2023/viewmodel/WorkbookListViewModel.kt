package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.repository.NotesRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class WorkbookListViewModel(application: Application): BaseAndroidViewModel(application) {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    private val notesRepository = NotesRepository(getApplication<Application>().applicationContext, viewModelScope)

    val schoolTypeInRepo = userRepository.schoolType

    val showEmptyContainer = MutableLiveData<Boolean>(false)
    val showRecyclerView = MutableLiveData<Boolean>(false)
    val showTotalLoadingView = MutableLiveData<Boolean>(false)
    val playTotalLoadingView = MutableLiveData<Boolean>(false)
    val showTotalPlanCover = MutableLiveData<Boolean>(false)
    val showDummyBottomView = MutableLiveData<Boolean>(false)
    val showStartChallengeStamp = MutableLiveData<Boolean>(false)

    private val _customBooks = MutableLiveData<List<Book>>()
    val customBooks: LiveData<List<Book>> = _customBooks
    lateinit var adapter: PatternStudyMyPlanAdapter

    private val _filterElements = MutableLiveData<List<BookFilterElement>>()
    val filterElements: LiveData<List<BookFilterElement>> = _filterElements

    private val _subjects = MutableLiveData<List<CurriculumSubject>>()
    val subjects: LiveData<List<CurriculumSubject>> = _subjects

    val joinedChallengeList = challengeRepository.joinedChallengeList

    val initialFilters: List<String> = listOf(
        "핀_포함", "과목_전체","유형_전체", "추천_전체", "추천레벨_전체"
    )
    val additionalFilters: List<String> = listOf(
        "유형_전체", "추천_전체", "추천레벨_전체"
    )

    var pinFilter: String = LearningFilterType.핀_포함.toString()
    fun fetchCustomBooks() {
        _filterElements.value?.let {
            fetchCustomBooksOnFilters(it, pinFilter)
        }
    }
    fun fetchCustomBooksOnFilters(filters: List<BookFilterElement>, additionalFilter: String? = pinFilter) {
        pinFilter = if (pinFilter == additionalFilter.toString()) pinFilter else additionalFilter.toString()
        val selectedFilters = filters
            .filter { it.isSelected.get() }
        val additionalFilterText = additionalFilters.joinToString(",")
        val filterString = selectedFilters
            .mapNotNull { it.value }
            .joinToString(",") + ",${pinFilter},${additionalFilterText}"

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val category = FilterCategory.CUSTOM_BOOK.text
            val order = FilterOrder.LAST.text
            patternStudyRepository.fetchAllBookList(filterString, order, category)?.let { newCustomBooks ->
                showTotalLoadingView.postValue(false)
                playTotalLoadingView.postValue(false)
                showRecyclerView.postValue(true)
                _customBooks.postValue(newCustomBooks)
            }
        }

    }
//    fun fetchCustomBook(filters: Set<LearningFilterType>) {
//
//
//        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
//            val category = FilterCategory.CUSTOM_BOOK.text
//            val filterString = filters.joinTo(StringBuilder(), separator = ",").toString()
//            val order = FilterOrder.LAST.text
//            patternStudyRepository.fetchAllBookList(filterString, order, category)?.let { newCustomBooks ->
//                showTotalLoadingView.postValue(false)
//                playTotalLoadingView.postValue(false)
//                showRecyclerView.postValue(true)
//                _customBooks.postValue(newCustomBooks)
//            }
//        }
//    }

    fun togglePin(pieceId: Int, isPinned: Boolean, callback: () -> Unit) {
        compositeDisposable += patternStudyRepository.setPin(pieceId, isPinned)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete { callback() }
            .doOnError {
                Log.e(javaClass.simpleName, "togglePin error=${it.localizedMessage}")
            }.subscribe()
    }
    fun removeFromMyPlan(book: Book) {
        val pieceId = if(book.assignID == null) book.pieceID else book.assignID!!
        compositeDisposable += patternStudyRepository.deleteFromMyBook(pieceId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete {

            }
            .doOnError {
                Log.e(javaClass.simpleName, "removeFromMyPlan error=${it.localizedMessage}")
            }.subscribe()
    }

    fun checkActionOfStartChallenge(cb: () -> Unit) {
        joinedChallengeList.value
            ?.filter { it.userStatus == ChallengeUserStatus.ING }
            ?.filter { it.startChallenge?.isWorkbooksInProgress == true }
            ?.forEach { _ -> cb() }
    }

    fun completedWorkbookChallenge(callback: (Challenge) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog()
            if (logResponse.isChallengeCourse.not()) return@launch
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            updateChallenge(startChallenge)
            callback(startChallenge)
        }
    }

    suspend fun postLog(): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "워크북",
            itemName = "생성",
            itemValue = null,
            itemNote = "유형학습",
        )
    }

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }
    fun fetchCurriculumSubjects() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val subjects = notesRepository.fetchCurriculumSubjects()
            _subjects.postValue(subjects)
        }
    }


    fun fetchBookFilter() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val filter = BookFilterParent.CUSTOM_WORKBOOK
            val elements = patternStudyRepository.fetchBookFilter(filter, BookFilterElement.getToggle())
            val syncedElements = syncSelectedFilterType(elements)
            _filterElements.postValue(syncedElements)
        }
    }
    fun switchCheckedContainPin(isChecked: Boolean) {
        val addFilter = if (isChecked) LearningFilterType.핀_미포함 else LearningFilterType.핀_포함
        fetchCustomBooksOnFilters(_filterElements.value ?: emptyList(), addFilter.toString())
    }
    fun onFilterItemClick(item: BookFilterElement) {
        val itemsWithSameParent = _filterElements.value?.filter { it.parentTitle == item.parentTitle } ?: emptyList()
        val nonAllItems = itemsWithSameParent.filter { it.name != "전체" }
        val allItem = itemsWithSameParent.find { it.name == "전체" }
        val hasAllItem = allItem != null

        handleRegularFilter(item, nonAllItems, allItem)

        // LiveData 업데이트하여 UI 갱신
        _filterElements.value = _filterElements.value
    }
    private fun handleRegularFilter(
        item: BookFilterElement,
        nonAllItems: List<BookFilterElement>,
        allItem: BookFilterElement?
    ) {
        if (item.name == "전체") {
            // "전체"가 이미 선택된 상태에서 다시 클릭된 경우 - 선택 해제하지 않음
            if (item.isSelected.get()) return

            // "전체" 선택 시 다른 항목들 선택 해제
            item.isSelected.set(true)
            nonAllItems.forEach { it.isSelected.set(false) }
        } else {
            // 현재 아이템의 선택 상태 토글
            item.isSelected.set(!item.isSelected.get())

            if (item.isSelected.get()) {
                // 개별 항목 선택 시 "전체" 선택 해제
                allItem?.isSelected?.set(false)

                // 모든 개별 항목이 선택되었으면 "전체"만 선택하고 나머지는 해제
                val allNonAllItemsSelected = nonAllItems.all { it.isSelected.get() }
                if (allNonAllItemsSelected) {
                    nonAllItems.forEach { it.isSelected.set(false) }
                    allItem?.isSelected?.set(true)
                }
            } else {
                // 항목이 선택 해제된 경우, 다른 항목이 하나도 선택되지 않았으면 "전체" 선택
                val anyItemSelected = nonAllItems.any { it.isSelected.get() }
                if (!anyItemSelected) {
                    allItem?.isSelected?.set(true)
                }
            }
        }
    }

    fun updateFilterTypes(filterType: String) {
        _filterElements.value?.find { it.name == filterType }?.let {
            // TODO
//            onFilterItemClick()
        }
    }
    private fun syncSelectedFilterType(filters: List<BookFilterElement>): List<BookFilterElement> {
        return filters.map {
            it.isSelected.set(initialFilters.contains(it.value))
            it
        }
    }
}