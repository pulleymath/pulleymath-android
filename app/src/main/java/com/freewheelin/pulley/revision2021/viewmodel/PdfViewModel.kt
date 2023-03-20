package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.EventBook
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.model.response.PdfLinkAnswerItem
import com.freewheelin.pulley.revision2021.repository.PdfRepository
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.utils.PulleyEvent
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PdfViewModel(application: Application): BaseAndroidViewModel(application) {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val pdfRepository: PdfRepository by lazy { PdfRepository() }
    private val challengeRepository by lazy { ChallengeRepository.instance }

    val pdfOrgList by lazy { MutableLiveData<List<Pdf>>() }
    val pdfList by lazy { MutableLiveData<List<Pdf>>() }
    val showEmpty by lazy { MutableLiveData(false) }

    var categoryFilter: String? = ""
    var subjectFilter: String? = ""
    var searchTextFilter: String = ""
    var openableBookFilter: Boolean = false
    var searchText = MutableLiveData("")
    var currSearchText = MutableLiveData("")

    val stickyAppBarShow by lazy { MutableLiveData(false) }
    val stickyAppBarAlpha by lazy { MutableLiveData(0f) }
    val scrollShadowShow by lazy { MutableLiveData(false) }

    val subjectItems by lazy { ArrayList(PdfListFilter.subjectList) }
    val categoryItems by lazy { ArrayList(PdfListFilter.categoryList) }

    val categorySelectedPosition = MutableLiveData(0)
    val subjectSelectedPosition = MutableLiveData(0)
    val isOpenableBookSelected = MutableLiveData(false)

    val pdfListLength = MutableLiveData("0")
    val joinedChallengeList = challengeRepository.joinedChallengeList

    var ySum: Int = 0

    fun listPdf(title: String = "", page: Int = 0, size: Int = 1000, subjectCode: String = "", category: String = "", callback: () -> Unit) {
        compositeDisposable += pdfRepository.pdfList(title, page, size, subjectCode, category)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "listPdf list=>${response.data.content}")
                // 원본
                pdfOrgList.postValue(response.data.content)
                // 필터된거

                val headerPdf = Pdf()
                val finalPdfList = listOf(headerPdf) + response.data.content;
                pdfList.postValue(finalPdfList)
                showEmpty.postValue(response.data.content.isEmpty())
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "listPdf error=${error.localizedMessage}")
            })
    }

    fun filter() {
        pdfOrgList.value?.let { orgList ->
            var result = orgList

            Log.d(javaClass.simpleName, "Filter subject=$subjectFilter")
            Log.d(javaClass.simpleName, "Filter category=$categoryFilter")
            Log.d(javaClass.simpleName, "Filter searchText=$searchTextFilter")

            if(subjectFilter?.isNotEmpty() == true) {
                Log.d(javaClass.simpleName, "Filter subject=isNotEmpty")
                result = result.filter { it.subject_code == subjectFilter }
            }

            if(categoryFilter?.isNotEmpty() == true) {
                Log.d(javaClass.simpleName, "Filter category=isNotEmpty")
                result = result.filter { it.category == categoryFilter }
            }

            if(searchTextFilter.isNotEmpty()) {
                Log.d(javaClass.simpleName, "Filter searchText=isNotEmpty")
                result = result.filter {
                    val upperTitle = it.title.uppercase()
                    val upperSearchText = searchTextFilter.uppercase()
                    upperTitle.contains(upperSearchText)
                }
            }

            if(openableBookFilter) {
//                result = result.filter { it.is_purchased || it.is_event_book }
                result = result.filter { !it.isLocked }
            }

            showEmpty.postValue(result.isEmpty())

            pdfListLength.postValue(result.size.toString())
            val headerPdf = Pdf()
            val finalResult = listOf(headerPdf) + result

            pdfList.postValue(finalResult)
        }
    }

    fun answer(cmBookId:Int, callback:(List<PdfLinkAnswerItem>?)->Unit) {
        compositeDisposable += pdfRepository.answer(cmBookId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                response.data?.let { callback(it) }
            }, { error ->
                callback(null)
                Log.e(javaClass.simpleName, "answer=${error.localizedMessage}")
            })
    }

    fun eventBookCheck(cmBookId:Int, callback: () -> Unit = {}) {
        val eventBook = EventBook(cmBookId)
        compositeDisposable += pdfRepository.eventBookCheck(eventBook)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                callback()
            }, { error ->
                callback()
                Log.e(javaClass.simpleName, "eventBookCheck=${error.localizedMessage}")
            })
    }

    val searchViewTextQueryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (query != null) {
                searchTextFilter = query
                searchText.value = query
            }
            filter()
            ySum = 0
            return false
        }
        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let {
                currSearchText.value = newText
                if (newText == "") {
                    searchTextFilter = newText
                    searchText.value = newText
                    filter()
                    ySum = 0
                    return true
                }
            }
            return false
        }
    }

    val searchViewCloseListener = object: SearchView.OnCloseListener {
        override fun onClose(): Boolean {
            isSearchViewIconified.postValue(true)
            return false
        }
    }
    val headerSearchViewCloseListener = object: SearchView.OnCloseListener {
        override fun onClose(): Boolean {
            isHeaderSearchViewIconified.postValue(true)
            return false
        }
    }

    val isSearchViewIconified by lazy { MutableLiveData(true) }

    val isHeaderSearchViewIconified by lazy { MutableLiveData(true) }
    val searchViewQueryTextFocusChangeListener = object : View.OnFocusChangeListener {
        override fun onFocusChange(v: View?, hasFocus: Boolean) {
            if (hasFocus) {
                isSearchViewIconified.postValue(!hasFocus)
            }
        }
    }

    val headerSearchViewQueryTextFocusChangeListener = object : View.OnFocusChangeListener {
        override fun onFocusChange(v: View?, hasFocus: Boolean) {
            if (hasFocus) {
                isHeaderSearchViewIconified.postValue(!hasFocus)
            }
        }
    }

    val onScrollListener = object: RecyclerView.OnScrollListener() {
        val threshold: Int = 158
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (ySum < 0) ySum = 0
            ySum += dy
            if (ySum >= threshold && stickyAppBarShow.value == false ) {
                stickyAppBarAlpha.postValue(0f)
                stickyAppBarShow.postValue(true)
                scrollShadowShow.postValue(true)
            } else if (ySum < threshold && stickyAppBarShow.value == true) {
                stickyAppBarShow.postValue(false)
                scrollShadowShow.postValue(false)
            }
        }
    }
    // 구매한 책 보기 에서 열람할수있는 책 보기 로 바뀜.
    // 무료책에 대해서 구매안해도 볼수있어야 하기 때문.
    fun openableBookListener(isChecked: Boolean) {
        openableBookFilter = isChecked
        isOpenableBookSelected.postValue(isChecked)
        filter()
        ySum = 0
    }

    fun sendLog(bookId: Int?, isOpened: Boolean, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog(bookId, isOpened)
            if (logResponse.isChallengeCourse.not()) return@launch
            callback()
        }
    }

    suspend fun postLog(bookId: Int?, isOpened: Boolean): V2LogUserResponse {
        val name = if(isOpened) "열기" else "닫기"
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "풀리북스",
            itemName = name,
            itemValue = "bookId=${bookId}",
            itemNote = null,
        )
    }
    fun checkActionOfStartChallenge(cb: () -> Unit) {
        joinedChallengeList.value
            ?.filter { it.userStatus == ChallengeUserStatus.ING }
            ?.filter { it.startChallenge?.isCommercialBooksInProgress == true }
            ?.forEach { _ -> cb() }
    }
    fun openTutorialPdfBook(bookId: Int, callback: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog(bookId, true)
            if (logResponse.isChallengeCourse.not()) return@launch
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            updateChallenge(startChallenge)
            callback()
        }
    }
    fun closeTutorialPdfBook(bookId: Int, callback: (Challenge) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog(bookId, false)
            println("asoaso logRes : ${logResponse.isChallengeCourse}")
            if (logResponse.isChallengeCourse.not()) return@launch
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            updateChallenge(startChallenge)
            callback(startChallenge)
        }
    }

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }
}

object PdfListFilter {

    val subject = mapOf<String, String>(
        "" to "과목 전체",
        "41" to "수학(상)",
        "42" to "수학(하)",
        "43" to "수학1",
        "44" to "수학2",
        "45" to "확률과 통계",
        "46" to "미적분",
        "47" to "기하"
    )

    val subjectList = subject.values.toList()

    val category = mapOf<String, String>(
        "" to "학습 유형 전체",
        "고등예비" to "고등예비",
        "개념서" to "개념서",
        "유형서" to "유형서",
        "심화서" to "심화서",
        "내신서" to "내신서",
        "기출서" to "기출서",
        "기출서" to "기출서",
        "실전모의고사" to "실전모의고사",
        "공식집" to "공식집",
        // TODO:       "연산서" to "연산서", 소정쌤이 빼라고 함
        )

    val categoryList = category.values.toList()
}