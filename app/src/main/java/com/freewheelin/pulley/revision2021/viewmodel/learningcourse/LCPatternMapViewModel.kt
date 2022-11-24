package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternCard
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.repository.LCPatternMapRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LCPatternMapViewModel: BaseViewModel(), LifecycleObserver {
    private val patternMapRepository by lazy { LCPatternMapRepository() }

    val patternCardList by lazy { MutableLiveData<List<LCPatternCard>>() }
    val showProgress by lazy { MutableLiveData<Boolean>(false) }
    val showNextStepBtn by lazy { MutableLiveData<Boolean>(false) }
    var patternCardLastIndex: Int = -1

    val chapter by lazy { MutableLiveData<StudyChapter>() }
    var solvedPatternCount = 0
    var isFirstFetch = true

    fun fetchPatternMap(chapterId: Int) {
        val studentId = user?.studentID ?: return
        compositeDisposable += patternMapRepository.fetchPatternMapInfo(chapterId, studentId)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress.postValue(true) }
            .doOnError { showProgress.postValue(false) }
            .doOnComplete { showProgress.postValue(false) }
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchPatternMap =>${response.data}")

                response.data?.let {
                    val header = LCPatternCard.getHeader()

                    val cardList = listOf(header) + it
                    patternCardList.postValue(cardList)
                    patternCardLastIndex = cardList.lastIndex
                    val showStepBtn = it.map { it.isCompleteCard }.reduce { prev, next -> prev || next }
                    solvedPatternCount = it.map { it.isCompleteCard }.filter { it }.size
                    showNextStepBtn.postValue(showStepBtn)
                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchPatternMap error=${error.localizedMessage}")
            })
    }

//    fun setChapterName(chapter: StudyChapter?) {
//        chapter?.let {
//            val name = it.name
//            val seq = it.sequence
//            val chapterName = "Part ${seq}. ${name} 학습 완료!"
//            smallChapterName.postValue(chapterName)
//        }
//
//    }

}