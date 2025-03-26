package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.PlanListenerV2
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.RecommendBookList as RecommendBookListView
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.BookFilterAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.legacy.utils.show
import com.google.gson.Gson
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class PulleyMathBooksViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    lateinit var recommendBookListViews: List<RecommendBookListView>
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    lateinit var planListener: PlanListenerV2

    val showEmptyContainer = MutableLiveData<Boolean>(false)
    val showRecyclerView = MutableLiveData<Boolean>(false)
    val showTotalLoadingView = MutableLiveData<Boolean>(false)
    val playTotalLoadingView = MutableLiveData<Boolean>(false)
    val showTotalPlanCover = MutableLiveData<Boolean>(false)
    val showDummyBottomView = MutableLiveData<Boolean>(false)
    val showRecommendBook = MutableLiveData<Boolean>(false)

    val scrollPositionTop = MutableLiveData<Unit>()
    private val _filterElements = MutableLiveData<List<BookFilterElement>>()
    val filterElements: LiveData<List<BookFilterElement>> = _filterElements

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val schoolTypeInRepo = userRepository.schoolType

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books
    val initPositionSettingFlag = MutableLiveData<Unit>()

    lateinit var planAdapter: PatternStudyMyPlanAdapter
    lateinit var filterAdapter: BookFilterAdapter

    val initialFilterType: List<String> = listOf(
        "핀_포함", "과목_전체", "유형_전체", "추천_전체", "추천레벨_전체"
    )

    fun fetchTotalBooksOnFilters(filters: List<BookFilterElement>, additionalFilter: LearningFilterType? = LearningFilterType.핀_포함) {
        val selectedFilters = filters
            .filter { it.isSelected.get() }
        val filterString = selectedFilters
            .mapNotNull { it.value }
            .joinToString(",") + ",${additionalFilter.toString()}"

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            patternStudyRepository.fetchAllBookList(filterString)?.let { newBookList ->
                showTotalLoadingView.postValue(false)
                playTotalLoadingView.postValue(false)
                showRecyclerView.postValue(true)
                _books.postValue(newBookList)
                delay(500)
                initPositionSettingFlag.postValue(Unit)
                scrollPositionTop.postValue(Unit)
            }
        }
    }

    fun switchCheckedContainPin(isChecked: Boolean) {
        val addFilter = if (isChecked) LearningFilterType.핀_미포함 else LearningFilterType.핀_포함
        fetchTotalBooksOnFilters(_filterElements.value ?: emptyList(), addFilter)
    }
    fun updateFilterTypes(filterType: String) {
        _filterElements.value?.find { it.name == filterType }?.let {
            // TODO
//            onFilterItemClick()
        }
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
    private fun syncSelectedFilterType(filters: List<BookFilterElement>): List<BookFilterElement> {
        return filters.map {
            it.isSelected.set(initialFilterType.contains(it.value))
            it
        }
    }

    fun fetchBookFilter() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val filter = BookFilterParent.PULLEY_WORKBOOK
            val elements = patternStudyRepository.fetchBookFilter(filter, BookFilterElement.getToggle())

            val syncedElements = syncSelectedFilterType(elements)
            _filterElements.postValue(syncedElements)
        }
    }

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
    fun collectRecommendList(isInit: Boolean = true, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val newRecommendList = fetchRecommendList() ?: return@launch
            showRecommendBook.postValue(newRecommendList.isNotEmpty())
            newRecommendList.forEachIndexed { index, book ->
                val title = book.title
                val bookList = book.targetBookPlanList
                val view = recommendBookListViews.getOrNull(index)
                withContext(Dispatchers.Main) {
                    if (isInit) {
                        view?.set(bookList.toMutableList(), title, index + 1, planListener)
                    } else {
                        view?.set(bookList)
                    }
                    view?.show { }
                }
            }
            _isLoading.postValue(false)
            cb()
        }
    }
    suspend fun fetchRecommendList(): List<RecommendBookList>? {
        return patternStudyRepository.fetchRecommendBooks()
    }
    fun checkActionOfStartChallenge(cb: () -> Unit) {
        joinedChallengeList.value
            ?.filter { it.userStatus == ChallengeUserStatus.ING }
            ?.filter { it.startChallenge?.isPulleyBooksCourseInProgress == true }
            ?.forEach { _ -> cb() }
    }
}