package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.OrderType
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component.NoteFilterFragment
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.dialogs.WrongManagementDialog
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Piece
import com.freewheelin.pulley.model.contents.PieceCategory
import com.freewheelin.pulley.revision2023.model.*
import com.freewheelin.pulley.revision2023.model.request.AdvancedLearningProblemRequest
import com.freewheelin.pulley.revision2023.model.request.NoteStudyAdvancedLearningRequest
import com.freewheelin.pulley.revision2023.model.response.NoteStudyAdvancedLearningResponse
import com.freewheelin.pulley.revision2023.repository.NotesRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate

class WrongNoteStudyViewModel(application: Application): BaseAndroidViewModel(application) {
    private val notesRepository = NotesRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val userRepository by lazy { UserRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    val showLockIcon = MutableLiveData<Boolean>()
    val headerTotalProblemCount = MutableLiveData<Int>(0)
    val headerClearedProblemCount = MutableLiveData<Int>(0)

    var selectedOrder = OrderType.recent
    var from: LocalDate = LocalDate.now().minusDays(6)
    var to: LocalDate = LocalDate.now()
    var datePickerType = DateRangePickerDialog.Type.RECENT7

    private val _noteWrapper = MutableLiveData<List<NoteStudyProblemWrapper>>()
    val noteWrapper: LiveData<List<NoteStudyProblemWrapper>> = _noteWrapper
    var originalNoteProblem = listOf<Problem>()

    val selectedProblem = MutableLiveData<List<Problem>>()

    private val _wrongProblem = MutableLiveData<List<Problem>>()
    val wrongProblem: LiveData<List<Problem>> = _wrongProblem

    private val _scrapProblem = MutableLiveData<List<Problem>>()
    val scrapProblem: LiveData<List<Problem>> = _scrapProblem

    var tabPosition = 0

//    var selectedFilterTypes = setOf<LearningFilterType>()

    var selectedFilterTypes : Set<LearningFilterType> = setOf(
            LearningFilterType.핀_포함, LearningFilterType.과목_전체, LearningFilterType.학습유형_전체, LearningFilterType.난이도_전체, LearningFilterType.보기설정_클리어_미포함
        )


    fun initMyPlanAdapterItem() {
        collectAllMyPlans()
    }
    fun init() {
        selectedProblem.postValue(listOf())
    }
    private fun collectAllMyPlans() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
//            val newMyPlans = fetchMyPlans() ?: return@launch
            _isLoading.postValue(false)
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    fun fetchWrongNotes(cb: () -> Unit = {}) {
        val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
        val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val wrongNotes = notesRepository.fetchNotes(startDate, endDate, "WRONG")
            originalNoteProblem = wrongNotes
            setGroupedProblem() {
                cb()
            }
        }
    }
    fun fetchScrapNotes(cb: () -> Unit = {}) {
        val startDate = DateTimeUtils.yyyy_MM_dd.format(from.toDate())
        val endDate = DateTimeUtils.yyyy_MM_dd.format(to.toDate())
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val scrapNotes = notesRepository.fetchNotes(startDate, endDate, "WRONG")
            originalNoteProblem = scrapNotes
            setGroupedProblem() {
                cb()
            }
        }
    }
    fun makeNotesWrappers (notes: List<Problem>, isWrongNote: Boolean, withSelectedClear: Boolean = true): List<NoteStudyProblemWrapper> {
        val header = NoteStudyProblemWrapper.getHeader(notes)
        headerTotalProblemCount.postValue(notes.size)
        headerClearedProblemCount.postValue(notes.filter { it.isClear }.size)

        val result = mutableListOf<NoteStudyProblemWrapper>(header)
        val newNotesResult1 = when(selectedOrder) {
            OrderType.recent -> notes.sortedByDescending { if(isWrongNote) it.updateDateTime else it.scrapDateTime }
            OrderType.old -> notes.sortedBy { if(isWrongNote) it.updateDateTime else it.scrapDateTime }
            OrderType.level -> notes.sortedBy { it.problemLevel }
            OrderType.subject -> notes.sortedBy { it.unitCode }
        }
        val newNotesResult2 = when(selectedOrder) {
            OrderType.recent -> newNotesResult1.groupBy { if (isWrongNote) it.updateDateTime_yyyyMMdd else it.scrapDateTime_yyyyMMdd }
            OrderType.old -> newNotesResult1.groupBy { if (isWrongNote) it.updateDateTime_yyyyMMdd else it.scrapDateTime_yyyyMMdd }
            OrderType.subject -> newNotesResult1.groupBy { it.getSubject().filterText }
            OrderType.level -> newNotesResult1.groupBy { it.getProblemLevel() }
        }

        newNotesResult2.forEach {
            val headerTxt = it.key
            val list = it.value

            val groupHeader = NoteStudyProblemWrapper.getGroupHeader(headerTxt)
            result.add(groupHeader)
            list.forEach {
                val card = NoteStudyProblemWrapper.getCard(it)
                result.add(card)
            }
        }

        if (withSelectedClear) selectedProblem.postValue(listOf())

        return result
    }

    fun updateNoteWrapper(notes: List<NoteStudyProblemWrapper>) {
//        _noteWrapper.postValue(listOf())
        _noteWrapper.postValue(notes)
    }




    fun onAllSelectedClicked(isChecked: Boolean) {
        _noteWrapper.value?.forEach { it.isSelected.set(isChecked) }
        _noteWrapper.value?.first()?.problems?.let { allProblems ->
            val problems = if (isChecked) {
                allProblems
            } else {
                listOf()
            }
            selectedProblem.postValue(problems)
        }
    }
    fun onOrderChanged(type: OrderType, cb: () -> Unit) {
        selectedOrder = type
//        setGroupedProblem(binding.tabLayout.selectedTabPosition)
        cb()
    }
    fun onCardCheckBoxClicked(isChecked: Boolean, item: NoteStudyProblemWrapper, cb: (Int) -> Unit) {
        val position = _noteWrapper.value?.indexOf(item) ?: -1
        _noteWrapper.value?.find { it == item }?.let { note ->
            selectedProblem.value?.let {
                if (isChecked) {
                    if (!it.contains(note.problem)) {
                        val addedProblem =
                            if (note.problem != null) listOf(note.problem) else listOf()
                        val result = it + addedProblem
                        selectedProblem.postValue(result)
                    }
                } else {
                    val result = it.filter { it != note.problem }
                    selectedProblem.postValue(result)
                }
                if (position != -1) {
                    cb(position)
                }
            }
        }
    }

    fun getNextProblem(problem: Problem): Problem? {
        noteWrapper.value?.let { wrapper ->
            wrapper.forEachIndexed { index, note ->
                if (note.problem == problem) {
                    val nextPosition = index + 1
                    return if (nextPosition < wrapper.size) {
                        val nextNote = wrapper[nextPosition]
                        val nextNoteType = nextNote.type == NoteStudyType.Card
                        if (nextNoteType) {
                            return nextNote.problem
                        }
                        val next2Position = index + 2
                        if (next2Position < wrapper.size) {
                            val next2Note = wrapper[next2Position]
                            next2Note.problem
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        }
        return null
    }

    fun getPrevProblem(problem: Problem): Problem? {
        noteWrapper.value?.let { wrapper ->
            wrapper.forEachIndexed { index, note ->
                if (note.problem == problem) {
                    val prevPosition = index - 1
                    return if (prevPosition >= 0) {
                        val prevNote = wrapper[prevPosition]
                        val prevNoteType = prevNote.type == NoteStudyType.Card
                        if (prevNoteType) {
                            return prevNote.problem
                        }
                        val prev2Position = index - 2
                        if (prev2Position >= 0) {
                            val prev2Note = wrapper[prev2Position]
                            prev2Note.problem
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        }
        return null
    }
    fun setGroupedProblem(withSelectedClear: Boolean = true, cb: () -> Unit) {
        if (selectedFilterTypes.isEmpty()) return

        val noteProblems = originalNoteProblem
        var notes = filterProblems(noteProblems, tabPosition)

        val result = makeNotesWrappers(notes, tabPosition == 0, withSelectedClear)
        updateNoteWrapper(result)
        cb()
    }

    private fun filterProblems(originalProblems: List<Problem>, tabPosition: Int): List<Problem> {
        var filteredProblem = originalProblems
        val filters = selectedFilterTypes

        if(tabPosition == 0) {
            filteredProblem = filteredProblem
                .filter { LocalDate(it.updateDateTime) in from..to }
                .filter {
                    var clearCondition = false
                    if(filters.contains(LearningFilterType.보기설정_클리어_미포함))
                        clearCondition = clearCondition || it.isClear == false

                    if(filters.contains(LearningFilterType.보기설정_클리어_포함))
                        clearCondition = true

                    clearCondition
                }
            //스크랩 필터는 초기화
//            scrapNoteFilterFragment.setFiltersStatus(filters) // TODO 이걸왜함?  동기화작업
        } else {
            filteredProblem = filteredProblem
                .filter { LocalDate(it.scrapDateTime) in from..to }
                .filter {
                    var correctCondition = false

                    if(filters.contains(LearningFilterType.보기설정_전체))
                        correctCondition = true

                    if(filters.contains(LearningFilterType.보기설정_맞은문제))
                        correctCondition = (correctCondition || it.getResultByScoring() == Result.correct)

                    if(filters.contains(LearningFilterType.보기설정_틀린문제))
                        correctCondition = (correctCondition || it.getResultByScoring() == Result.incorrect)

                    if(filters.contains(LearningFilterType.보기설정_안_푼_문제))
                        correctCondition = (correctCondition || it.getResultByScoring() == Result.yet)

                    correctCondition
                }
//            wrongNoteFilterFragment.setFiltersStatus(filters) // 동기화
        }

        filteredProblem = filteredProblem.filter {

            val subjectCondition = filterSubjectCondition(it, filters)
            val levelCondition = filterLevelCondition(it, filters)
            val pieceCategoryCondition = filterPieceCategoryCondition(it, filters)

            subjectCondition && levelCondition && pieceCategoryCondition
        }

        return filteredProblem
    }
    fun filterSubjectCondition(problem: Problem, filters: Set<LearningFilterType>): Boolean {

        if(filters.contains(LearningFilterType.과목_전체))
            return true

        val subject = problem.getSubject()
        var subjectCondition = false
        if(filters.contains(LearningFilterType.과목_수학_상)) subjectCondition = (subjectCondition || subject.isMathSang)
        if(filters.contains(LearningFilterType.과목_수학_하)) subjectCondition = (subjectCondition || subject.isMathHa)
        if(filters.contains(LearningFilterType.과목_수학1)) subjectCondition = (subjectCondition || subject.isMath1)
        if(filters.contains(LearningFilterType.과목_수학2)) subjectCondition = (subjectCondition || subject.isMath2)
        if(filters.contains(LearningFilterType.과목_확통)) subjectCondition = (subjectCondition || subject.isProbabilityAndStatistics)
        if(filters.contains(LearningFilterType.과목_미적분)) subjectCondition = (subjectCondition || subject.isCalculus)
        if(filters.contains(LearningFilterType.과목_기하)) subjectCondition = (subjectCondition || subject.isGeometry)
        if(filters.contains(LearningFilterType.과목_중1_1)) subjectCondition = (subjectCondition || subject.isMiddle1_1)
        if(filters.contains(LearningFilterType.과목_중1_2)) subjectCondition = (subjectCondition || subject.isMiddle1_2)
        if(filters.contains(LearningFilterType.과목_중2_1)) subjectCondition = (subjectCondition || subject.isMiddle2_1)
        if(filters.contains(LearningFilterType.과목_중2_2)) subjectCondition = (subjectCondition || subject.isMiddle2_2)
        if(filters.contains(LearningFilterType.과목_중3_1)) subjectCondition = (subjectCondition || subject.isMiddle3_1)
        if(filters.contains(LearningFilterType.과목_중3_2)) subjectCondition = (subjectCondition || subject.isMiddle3_2)

        return subjectCondition
    }
    fun filterLevelCondition(problem: Problem, filters: Set<LearningFilterType>): Boolean {
        if(filters.contains(LearningFilterType.난이도_전체))
            return true

        var levelCondition = false
        if(filters.contains(LearningFilterType.난이도_하)) levelCondition = (levelCondition || problem.problemLevel == 1)
        if(filters.contains(LearningFilterType.난이도_중하)) levelCondition = (levelCondition || problem.problemLevel == 2)
        if(filters.contains(LearningFilterType.난이도_중)) levelCondition = (levelCondition || problem.problemLevel == 3)
        if(filters.contains(LearningFilterType.난이도_상)) levelCondition = (levelCondition || problem.problemLevel == 4)
        if(filters.contains(LearningFilterType.난이도_최상)) levelCondition = (levelCondition || problem.problemLevel == 5)

        return levelCondition
    }
    fun filterPieceCategoryCondition(problem: Problem, filters: Set<LearningFilterType>): Boolean {
        if(filters.contains(LearningFilterType.학습유형_전체))
            return true

        var pieceCategoryCondition = false
        if (filters.contains(LearningFilterType.학습유형_유형학습))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.book))
        if (filters.contains(LearningFilterType.학습유형_워크북))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.workbook))
        if (filters.contains(LearningFilterType.학습유형_모의고사))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.mockExam))
        if (filters.contains(LearningFilterType.학습유형_오답학습))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.note) || problem.getPieceCategory().contains(PieceCategory.reference))
        if (filters.contains(LearningFilterType.학습유형_테스트))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.dailyTest))
        if (filters.contains(LearningFilterType.학습유형_추천학습))
            pieceCategoryCondition = (pieceCategoryCondition || problem.getPieceCategory().contains(
                PieceCategory.recommned))

        return pieceCategoryCondition
    }

    fun makeAdvancedLearning (problems: List<Problem>,
             similarStr: String, difficulty: String,
             requestProblemNumber: Int, isIncludeClearProblem: Boolean,
             noteType: String, cb: (Piece) -> Unit) {
        val req = NoteStudyAdvancedLearningRequest(
            sameOrSimilar = similarStr,
            studentID = user?.studentID!!,
            requestProblemNumber = requestProblemNumber,
            difficulty = difficulty,
            noteType = noteType,
            includeClearProblem = isIncludeClearProblem,
            problems = problems.map { AdvancedLearningProblemRequest.convertFromProblem(it) }
        )
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val res = notesRepository.makeAdvancedLearning(req)
            withContext(Dispatchers.Main) {
                cb(res)
            }
        }

    }
}
