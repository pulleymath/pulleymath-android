package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.utils.PulleyEvent
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
    var latestFilters: Set<FilterType>? = null

    val joinedChallengeList = challengeRepository.joinedChallengeList

    fun fetchCustomBook(filters: Set<FilterType>) {

        latestFilters = filters

        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val category = FilterCategory.CUSTOM_BOOK.text
            val filterString = filters.joinTo(StringBuilder(), separator = ",").toString()
            val order = FilterOrder.LAST.text
            patternStudyRepository.fetchAllBookList(filterString, order, category)?.let { newCustomBooks ->
                showTotalLoadingView.postValue(false)
                playTotalLoadingView.postValue(false)
                showRecyclerView.postValue(true)
                _customBooks.postValue(newCustomBooks)
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
}