package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.activities.learning.tabFragment.book.PlanListenerV2
import com.freewheelin.pulley.activities.learning.tabFragment.book.RecommendBookList as RecommendBookListView
import com.freewheelin.pulley.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.utils.show
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class PulleyMathBooksViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    lateinit var recommendBookListViews: List<RecommendBookListView>
    private val challengeRepository by lazy { ChallengeRepository.instance }
    lateinit var planListener: PlanListenerV2

    val showEmptyContainer = MutableLiveData<Boolean>(false)
    val showRecyclerView = MutableLiveData<Boolean>(false)
    val showTotalLoadingView = MutableLiveData<Boolean>(false)
    val playTotalLoadingView = MutableLiveData<Boolean>(false)
    val showTotalPlanCover = MutableLiveData<Boolean>(false)
    val showDummyBottomView = MutableLiveData<Boolean>(false)
    val showRecommendBook = MutableLiveData<Boolean>(false)

    val joinedChallengeList = challengeRepository.joinedChallengeList

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books
    val initPositionSettingFlag = MutableLiveData<Unit>()

    lateinit var planAdapter: PatternStudyMyPlanAdapter

    var latestFilters: Set<FilterType>? = null
    fun fetchTotalBooks(filters: Set<FilterType>) {
        latestFilters = filters
        val filterString = filters.joinTo(StringBuilder(), separator = ",").toString()
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            patternStudyRepository.fetchAllBookList(filterString)?.let { newBookList ->
                showTotalLoadingView.postValue(false)
                playTotalLoadingView.postValue(false)
                showRecyclerView.postValue(true)
                _books.postValue(newBookList)
                delay(500)
                initPositionSettingFlag.postValue(Unit)
            }
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