package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.PatternStudyRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PatternStudyViewModel(application: Application): BaseAndroidViewModel(application) {
    private val patternStudyRepository = PatternStudyRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    lateinit var myPlanAdapter: PatternStudyMyPlanAdapter
    val joinedChallengeList = challengeRepository.joinedChallengeList
    val schoolType = userRepository.schoolType

    private val _myPlans = MutableLiveData<MyBookList>()
    val myPlans: LiveData<MyBookList> = _myPlans

    val myPlanCount = MutableLiveData<String>("")
    val pinCount = MutableLiveData<String>("")
    val showMyPlanEmptyView = MutableLiveData<Boolean>(false)
    val showMyPlan = MutableLiveData<Boolean>(false)
    val myPlanAdapterItemListener = MutableLiveData<Unit>()

    val tooltipText = "- 최근 30일 동안 학습하지 않은 문제집은 [나의문제집]에서 자동으로 빠집니다.\n" +
        "   그렇게 빠진 문제집은 [전체문제집]에서 다시 볼 수 있습니다.\n" +
        "\n" +
        "- 워크북 문제집의 경우,\n" +
        "   채점한 문제가 총 2문제 이하이고 최근 30일 동안 학습하지 않았다면 \n   영구 삭제됩니다.\n" +
        "\n" +
        "- 핀을 꽂아둔 모든 문제집은 빠지거나 삭제되지 않습니다."

    val showPulleyMathChallengeStamp = MutableLiveData<Boolean>(false)
    val showCommercialBooksChallengeStamp = MutableLiveData<Boolean>(false)
    val showWorkbooksChallengeStamp = MutableLiveData<Boolean>(false)

    fun collectAllMyPlans() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val newMyPlans = fetchMyPlans() ?: return@launch
            _isLoading.postValue(false)
            upsertMyPlans(newMyPlans.myPieceStorageList)
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    suspend fun fetchMyPlans(): MyBookList? {
        return patternStudyRepository.fetchMyPlans()
    }

    suspend fun fetchMyHistory(): List<Book> {
        return patternStudyRepository.fetchHistory()
    }

    private suspend fun upsertMyPlans(books: List<Book>) {
//        patternStudyRepository.upsertAllMyPlans(books)

        // ㅠㅠ 왜 room에 저장안되냐고
        val myPlan = MyBookList(0, 0, books.toMutableList()).apply {
            publicSync()
        }
        _myPlans.postValue(myPlan)

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