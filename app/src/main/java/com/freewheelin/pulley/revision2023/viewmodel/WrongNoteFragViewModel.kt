package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.model.Problem
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
import com.freewheelin.pulley.utils.DateTimeUtils
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

    var afterFetch = false

    private val _priorConcepts = MutableLiveData<List<PriorConcept>>()
    val priorConcepts: LiveData<List<PriorConcept>> = _priorConcepts
    private val _filterElements = MutableLiveData<List<BookFilterElement>>()
    val filterElements: LiveData<List<BookFilterElement>> = _filterElements

    private val _wrongProblem = MutableLiveData<List<Problem>>()
    val wrongProblem: LiveData<List<Problem>> = _wrongProblem

    private val _scrapProblem = MutableLiveData<List<Problem>>()
    val scrapProblem: LiveData<List<Problem>> = _scrapProblem
    val updateNotes = MutableLiveData<Unit>()

    val showLockIcon = MutableLiveData<Boolean>()

    var from: LocalDate = LocalDate.now().minusDays(6)
    var to: LocalDate = LocalDate.now()
    var datePickerType = DateRangePickerDialog.Type.RECENT7
    var selectedFilterTabPosition = 0

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
//            println("qwoqwo wrongNote size: ${wrongNotes.size}")
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
            elements.forEach { println("asoaso ${it.name}, value: ${it.value} ${it.filterType}") }
            val syncedElements = syncSelectedFilterType(elements)
            syncedElements.forEach { println("asoaso - ${it.name}, ${it.filterType}, - ${it.isSelected.get()}") }
            println("asoaso - - - selectedFilterTypes : ${selectedFilterTypes}")
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
    fun syncSelectedFilterType() {
        filterElements.value?.forEach {
            it.isSelected.set(selectedFilterTypes.contains(it.filterType))
        }
    }
    private fun syncSelectedFilterType(filters: List<BookFilterElement>): List<BookFilterElement> {
        return filters.map {
            it.isSelected.set(selectedFilterTypes.contains(it.filterType))
            it
        }
    }
    fun onFilterItemClick(item: BookFilterElement, cb: (Set<LearningFilterType>) -> Unit) {
        val type = item.filterType
        val isContained = selectedFilterTypes.contains(type)
        if (isContained) {
            selectedFilterTypes.remove(type)
        } else {
            updateFilterTypes(type)
        }
        checkFiltersWhenRemoveSelfs(type)
        syncSelectedFilterType()

        val filters = selectedFilterTypes.toSet()
        cb(filters)
//        updateNotes(filters)

    }
    fun updateFilterTypes(type: LearningFilterType) {
        selectedFilterTypes.add(type)
        selectedFilterTypes.removeAll(type.exclusiveSet)
    }
    private fun checkFiltersWhenRemoveSelfs(type: LearningFilterType) {
        val sectionListWithoutSelected = type.sectionList.filter { it != type }
        for (item in sectionListWithoutSelected) {
            if (selectedFilterTypes.contains(item)) {
                return
            }
        }
        selectedFilterTypes.add(type)
    }
    fun updateNotes(filters: Set<LearningFilterType>) {
        updateNotes.postValue(Unit)
    }
    fun getSelectedFilterProblem(): List<Problem>? {
        return if (selectedFilterTabPosition == 0) {
            wrongProblem.value
        } else {
            scrapProblem.value
        }
    }
}