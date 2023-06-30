package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper.PatternLearningStatus
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCardWrapper.ConceptLearningStatus
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteMapRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LCWrongNoteMapViewModel : BaseViewModel(), LifecycleObserver {
    private val wrongNoteRepository: LCWrongNoteMapRepository by lazy { LCWrongNoteMapRepository() }

    val selectedChapter by lazy { MutableLiveData<StudyChapter>() }

    // 두개가 같으면서 사용처가 약간 다르다
    val isIncludedCompleteWrongNote by lazy { MutableLiveData<Boolean>(false) }
    var noteFilterFlag = false

    val isNoteCardCount0 by lazy { MutableLiveData<Boolean>(false) }

    val originalNoteCardWrapper by lazy { MutableLiveData<LCWrongNoteMapCardWrapper>() }
    val filteredNoteCardList by lazy { MutableLiveData<List<LCWrongNoteMapCard>>() }

    val noteCardCount by lazy { MutableLiveData(0) }
    var headerTitle: String = ""
    val bottomBtnText by lazy { MutableLiveData<String>("") }

    val showProgress by lazy { MutableLiveData<Boolean>(true) }
    val emptyText by lazy { MutableLiveData<String>("") }

    var isFirstFetch = true

    fun fetchLCWrongNoteInfo(currChapterId: Int?) {
        val chapterId = currChapterId ?: return
        val studentId = user?.studentID ?: return
        val filter = "ALL" // 또는 ALL
        compositeDisposable += wrongNoteRepository.fetchLcWrongNote(chapterId, studentId, filter)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress.postValue(true) }
            .doOnError { showProgress.postValue(false) }
            .doOnComplete { showProgress.postValue(false) }
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchLCWrongNoteInfo =>${response.data}")
                response.data?.let { cardWrapper ->
                    val cardList = cardWrapper.wrongQuizzes

                    val headerCard = LCWrongNoteMapCard.getHeader()
                    val footerCard = LCWrongNoteMapCard.getFooter()
                    val result = if (noteFilterFlag) { cardList }
                    else { cardList.filter { it.isIncomplete() } }

                    val filteredCardList = listOf(headerCard) + result + listOf(footerCard)

                    checkLearningStatus(cardWrapper)

                    noteCardCount.postValue(result.size)
                    isNoteCardCount0.postValue(result.isEmpty())
                    filteredNoteCardList.postValue(filteredCardList)
                    originalNoteCardWrapper.postValue(cardWrapper)

                    isIncludedCompleteWrongNote.value?.let { flag -> noteFilterFlag = flag }

                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchLCWrongNoteInfo error=${error.localizedMessage}")
            })
    }

    private fun checkLearningStatus(item: LCWrongNoteMapCardWrapper) {

        when (item.patternStatus) {
            PatternLearningStatus.NONE -> {
                bottomBtnText.postValue("$headerTitle 학습 종료!")
                emptyText.postValue("아직 유형 학습 내역이 없네요!\n유형을 풀고 오답을 학습해보세요")
            }
            PatternLearningStatus.ING -> {
                bottomBtnText.postValue("$headerTitle 학습 종료!")
                emptyText.postValue("앗, 아직 풀지 않은 유형이 있어요!\n남은 유형을 풀고 오답을 학습하세요")
            }
            PatternLearningStatus.DONE -> {
                bottomBtnText.postValue("$headerTitle 학습 완료!")
                emptyText.postValue("와! 모든 오답을 학습했어요 :)\n아래의 버튼을 클릭해 학습을 완료하세요!")
            }
            PatternLearningStatus.PERFECT -> {
                bottomBtnText.postValue("$headerTitle 학습 완료!")
                emptyText.postValue("와! 틀린 문제가 없네요 :)\n아래의 버튼을 클릭해 학습을 완료하세요!")
            }
        }

    }

    fun noteFilterChangeListener(isChecked: Boolean) {
        noteFilterFlag = isChecked
        filter()
    }
    fun filter() {
        val headers = filteredNoteCardList.value?.filter { it.cardType == LCWrongNoteMapCard.CardType.Header }
        val footers = filteredNoteCardList.value?.filter { it.cardType == LCWrongNoteMapCard.CardType.Footer }

        val headerCard = if (headers?.isNotEmpty() == true) headers[0] else LCWrongNoteMapCard.getHeader()
        val footerCard = if (footers?.isNotEmpty() == true) footers[0] else LCWrongNoteMapCard.getFooter()

        originalNoteCardWrapper.value?.wrongQuizzes?.let { orgList ->
            var result = orgList

            if (!noteFilterFlag) {
                result = result.filter { it.isIncomplete() }
            }


//            result = result.sortedByDescending { it.isIncomplete() }
            result = result.sortedWith (compareBy({ it.isIncomplete() }, { it.sequence }) )

            noteCardCount.postValue(result.size)
            isNoteCardCount0.postValue(result.isEmpty())
            val filteredCardList = listOf(headerCard) + result + listOf(footerCard)
            filteredNoteCardList.postValue(filteredCardList)
        }
    }

    fun setChapterHeaderTitle(title: String?) {
        title?.let { headerTitle = it }
    }
}


