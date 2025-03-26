package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.repository.NotesRepository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.PriorConceptRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.repository.impl.PriorConceptRepositoryImpl
import com.freewheelin.pulley.revision2023.ui.adapter.PriorConceptAdapter
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

class WrongNoteFragViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val notesRepository = NotesRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }
    val user = userRepository.user

//    var afterFetch = false

//    private val _priorConcepts = MutableLiveData<List<PriorConcept>>()
//    val priorConcepts: LiveData<List<PriorConcept>> = _priorConcepts
    private val _filterElements = MutableLiveData<List<BookFilterElement>>()
    val filterElements: LiveData<List<BookFilterElement>> = _filterElements

//    private val _wrongProblem = MutableLiveData<List<Problem>>()
//    val wrongProblem: LiveData<List<Problem>> = _wrongProblem
//
//    private val _scrapProblem = MutableLiveData<List<Problem>>()
//    val scrapProblem: LiveData<List<Problem>> = _scrapProblem
//    val updateNotes = MutableLiveData<Unit>()

//    val showLockIcon = MutableLiveData<Boolean>()

    var from: LocalDate = LocalDate.now().minusDays(6)
    var to: LocalDate = LocalDate.now()
    var datePickerType = DateRangePickerDialog.Type.RECENT7
//    var selectedFilterTabPosition = 0

    lateinit var bookFilterParent: BookFilterParent
    val selectedFilterTypes : HashSet<LearningFilterType> by lazy {
        hashSetOf(
            LearningFilterType.핀_포함, LearningFilterType.과목_전체, LearningFilterType.학습유형_전체, LearningFilterType.난이도_전체,
            if (bookFilterParent == BookFilterParent.WRONG_NOTE) LearningFilterType.보기설정_클리어_미포함 else LearningFilterType.보기설정_전체
        )
    }

//    fun fetchWrongNotes(cb: () -> Unit = {}) {
//        val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
//        val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
//        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
//            val wrongNotes = notesRepository.fetchNotes(startDate, endDate, "WRONG")
//            _wrongProblem.postValue(wrongNotes)
//            cb()
//        }
//    }
//    fun fetchScrapNotes(cb: () -> Unit = {}) {
//        val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
//        val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
//        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
//            val scrapNotes = notesRepository.fetchNotes(startDate, endDate, "WRONG")
//            Problem()
//            _scrapProblem.postValue(scrapNotes)
//            cb()
//        }
//    }
    fun fetchBookFilter() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val dateText = datePickerType.text ?: (DateTimeUtils.yyyyMMddFormat.format(from.toDate()) + " - " + DateTimeUtils.yyyyMMddFormat.format(to.toDate()))
            val elements = patternStudyRepository.fetchBookFilter(bookFilterParent, BookFilterElement.getCalendar(dateText))
            val syncedElements = syncSelectedFilterType(elements)
            _filterElements.postValue(syncedElements)
        }
    }
    fun updateCalendarElement() {
        val dateText = datePickerType.text ?: (DateTimeUtils.yyyyMMddFormat.format(from.toDate()) + " - " + DateTimeUtils.yyyyMMddFormat.format(to.toDate()))
        filterElements.value?.first()?.also {
            it.calendarValue.set(dateText)
            it.isSelected.set(it.isSelected.get().not())
        }
    }

    private fun syncSelectedFilterType(filters: List<BookFilterElement>): List<BookFilterElement> {
        return filters.map {
            it.isSelected.set(selectedFilterTypes.contains(it.filterType))
            it
        }
    }

    fun onFilterItemClick(item: BookFilterElement, cb: () -> Unit) {
        println("qwpqwp item : ${item}")

        // 같은 parentTitle을 가진 항목들 찾기
        val itemsWithSameParent = _filterElements.value?.filter { it.parentTitle == item.parentTitle } ?: emptyList()
        val nonAllItems = itemsWithSameParent.filter { it.name != "전체" }
        val allItem = itemsWithSameParent.find { it.name == "전체" }
        val hasAllItem = allItem != null

        // "보기 설정" 특별 처리
        if (item.parentTitle == "보기 설정") {
            handleViewSettingsFilter(item, itemsWithSameParent, hasAllItem)
            _filterElements.value = _filterElements.value
            return cb()
        }

        // 일반 필터 처리
        handleRegularFilter(item, nonAllItems, allItem)

        // LiveData 업데이트하여 UI 갱신
        _filterElements.value = _filterElements.value
        cb()
    }

    // "보기 설정" 필터 처리 함수
    private fun handleViewSettingsFilter(
        item: BookFilterElement,
        itemsWithSameParent: List<BookFilterElement>,
        hasAllItem: Boolean
    ) {
        if (hasAllItem) {
            if (item.name == "전체") {
                // "전체"가 이미 선택된 상태에서 다시 클릭된 경우 - 선택 해제하지 않음
                if (item.isSelected.get()) return

                // "전체" 선택 시 다른 항목들 선택 해제
                item.isSelected.set(true)
                itemsWithSameParent.filter { it.name != "전체" }.forEach { it.isSelected.set(false) }
            } else {
                // "전체"가 아닌 항목 토글
                item.isSelected.set(!item.isSelected.get())

                if (item.isSelected.get()) {
                    // 개별 항목 선택 시 "전체" 선택 해제
                    itemsWithSameParent.find { it.name == "전체" }?.isSelected?.set(false)
                } else {
                    // 모든 항목이 선택 해제되었으면 "전체" 선택
                    val anyItemSelected = itemsWithSameParent.filter { it.name != "전체" }.any { it.isSelected.get() }
                    if (!anyItemSelected) {
                        itemsWithSameParent.find { it.name == "전체" }?.isSelected?.set(true)
                    }
                }
            }
        } else {
            // "전체" 항목이 없는 경우 - 라디오 버튼 로직 적용
            _filterElements.value?.forEach { element ->
                if (element.parentTitle == "보기 설정") {
                    element.isSelected.set(element == item)
                }
            }
        }
    }

    // 일반 필터 처리 함수
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


//    fun updateFilterTypes(type: LearningFilterType) {
//        selectedFilterTypes.add(type)
//        selectedFilterTypes.removeAll(type.exclusiveSet)
//    }
//    private fun checkFiltersWhenRemoveSelfs(type: LearningFilterType) {
//        val sectionListWithoutSelected = type.sectionList.filter { it != type }
//        for (item in sectionListWithoutSelected) {
//            if (selectedFilterTypes.contains(item)) {
//                return
//            }
//        }
//        selectedFilterTypes.add(type)
//    }
//    fun updateNotes(filters: Set<LearningFilterType>) {
//        updateNotes.postValue(Unit)
//    }
//    fun getSelectedFilterProblem(): List<Problem>? {
//        return if (selectedFilterTabPosition == 0) {
//            wrongProblem.value
//        } else {
//            scrapProblem.value
//        }
//    }
}