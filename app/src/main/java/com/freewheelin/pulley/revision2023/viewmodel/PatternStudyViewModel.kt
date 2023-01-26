package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.activities.learning.tabFragment.book.PlanListener
import com.freewheelin.pulley.activities.learning.tabFragment.book.RecommendBookList as RecommendBookListView
import com.freewheelin.pulley.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.service.PatternStudyApi
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.utils.show
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PatternStudyViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)

    lateinit var myPlanAdapter: PatternStudyMyPlanAdapter
    lateinit var recommendBookListViews: List<RecommendBookListView>
    lateinit var planListener: PlanListener

    private val _myPlans = MutableLiveData<MyBookList>()
    val myPlans: LiveData<MyBookList> = _myPlans

    val myPlanCount = MutableLiveData<String>("")
    val pinCount = MutableLiveData<String>("")
    val showMyPlanEmptyView = MutableLiveData<Boolean>(false)
    val showMyPlan = MutableLiveData<Boolean>(false)
    val tooltipText = "- 최근 30일 동안 학습하지 않은 문제집은 [나의문제집]에서 자동으로 빠집니다.\n" +
        "   그렇게 빠진 문제집은 [전체문제집]에서 다시 볼 수 있습니다.\n" +
        "\n" +
        "- 워크북 문제집의 경우,\n" +
        "   채점한 문제가 총 2문제 이하이고 최근 30일 동안 학습하지 않았다면 \n   영구 삭제됩니다.\n" +
        "\n" +
        "- 핀을 꽂아둔 모든 문제집은 빠지거나 삭제되지 않습니다."

    fun initMyPlanAdapterItem() {
//        patternStudyRepository.run {
//            flowMyPlans()
//                .onEach { books ->
//                    val myPlan = MyBookList(0, 0, books.toMutableList()).apply {
//                        publicSync()
//                    }
//
//                    _myPlans.value = myPlan
//                }
//                .launchIn(viewModelScope)
//        }
        collectAllMyPlans()
    }
    private fun collectAllMyPlans() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val newMyPlans = fetchMyPlans()
            _isLoading.postValue(false)

//            val oldBook = myPlanBooks.value?.filterNot { it in newMyPlans.myPieceStorageList }
//            oldBook?.forEach { deleteMyPlan(it) }
            upsertMyPlans(newMyPlans.myPieceStorageList)
//            adapterUpdateIfNewConceptSizeZero(newConcepts)

//            val planCount = "총 ${newMyPlans.myPieceStorageList.size}개 "
//            val pinText = "핀 설정 ${newMyPlans.pinBookPlanCount}개 "
//            myPlanCount.postValue(planCount)
//            pinCount.postValue(pinText)
//            showMyPlanEmptyView.postValue(newMyPlans.myPieceStorageList.isEmpty())
//            showMyPlan.postValue(!newMyPlans.myPieceStorageList.isEmpty())
        }
    }

    suspend fun fetchMyPlans(): MyBookList {
        return patternStudyRepository.fetchMyPlans()
    }
    private suspend fun deleteMyPlan(book: Book) {
        patternStudyRepository.deleteMyPlans(book)
    }
    private suspend fun upsertMyPlans(books: List<Book>) {
//        patternStudyRepository.upsertAllMyPlans(books)

        // ㅠㅠ 왜 room에 저장안되냐고
        val myPlan = MyBookList(0, 0, books.toMutableList()).apply {
            publicSync()
        }
        _myPlans.postValue(myPlan)

    }

    fun collectRecommendList(isInit: Boolean = true, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val newRecommendList = fetchRecommendList()
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
            cb()
        }
    }
    suspend fun fetchRecommendList(): List<RecommendBookList> {
        return patternStudyRepository.fetchRecommendBooks()
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
    fun removeFromMyPlan(book: Book, callback: () -> Unit) {
        val pieceId = if(book.assignID == null) book.pieceID else book.assignID!!
        compositeDisposable += patternStudyRepository.deleteFromMyBook(pieceId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete { callback() }
            .doOnError {
                Log.e(javaClass.simpleName, "removeFromMyPlan error=${it.localizedMessage}")
            }.subscribe()
    }
}