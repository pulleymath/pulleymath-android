package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2021.views.DisallowTouchEventViewPager
import com.freewheelin.pulley.revision2021.views.LCPatternViewPager
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LCPatternViewModel : BaseViewModel(), LifecycleObserver {
    private val patternRepository: LCPatternRepository by lazy { LCPatternRepository() }

    val patternName by lazy { MutableLiveData<String>() }
    val patternQuizList by lazy { MutableLiveData<List<LCPatternQuiz>>() }

    val patternQuiz1 by lazy { MutableLiveData<LCPatternQuiz>() }
    val patternQuiz2 by lazy { MutableLiveData<LCPatternQuiz>() }
    val patternQuiz3 by lazy { MutableLiveData<LCPatternQuiz>() }

    val selectedQuizIndex by lazy { MutableLiveData<Int>(0) }
    val isHintBtnDisabled by lazy { MutableLiveData<Boolean>(false) }
    val remainingHintSizeLive by lazy { MutableLiveData(0) }

    var patternId = -1

    fun setPatternName(course: SingleCourseDesc) {
        val parentPatternName = course.name
        val sequence = course.sequence
        patternName.postValue("유형 0${sequence}. ${parentPatternName}")
    }

    fun fetchPatternInfo(patternId: Int) {
        this.patternId = patternId
        val studentId = user?.studentID ?: return
        compositeDisposable += patternRepository.fetchPatternInfo(patternId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchPatternInfo =>${response.data}")
                response.data?.let {
                    patternQuizList.postValue(it)
                    setHintBtnText(it.first().hints.size)
                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchPatternInfo error=${error.localizedMessage}")
            })
    }

    fun usePatternQuizHint(callback: () -> Unit) {
        val studentId = user?.studentID ?: return
        if (selectedQuizIndex.value == null) return
        if (patternQuizList.value == null) return
        val patternQuizId = patternQuizList.value!!.get(selectedQuizIndex.value!!).patternQuizId

        compositeDisposable += patternRepository.usePatternQuizHint(patternQuizId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "usePatternQuizHint =>${response.data}")
                CoroutineScope(Dispatchers.Main).launch {
                    callback()
                }
            }, { error ->
                Log.e(javaClass.simpleName, "usePatternQuizHint error=${error.localizedMessage}")
            })

    }

    fun setViewPagerPosition(pagerWrapper: LCPatternViewPager, position: Int) {
        pagerWrapper.pager.currentItem = position
        selectedQuizIndex.postValue(position)

    }

    fun setHintBtnText(size: Int) {
        if (size < 0) return
//        remainingHintSize = size
        remainingHintSizeLive.postValue(size)
//        hintBtnText.postValue("힌트 ${size}")
    }

    fun getPagerPositionOnQuizId(quizId: Int): Int {
        patternQuizList.value?.forEachIndexed { index, quiz ->
            if (quiz.patternQuizId == quizId) {
                return index
            }
        }
        return 0
    }

    fun isLastPagerPosition(position: Int): Boolean {
        return position == patternQuizList.value?.lastIndex
    }
    fun updatePatternQuizList() {
        patternQuizList.postValue(patternQuizList.value)
    }

    fun isPagerLastIndex(): Boolean {
        selectedQuizIndex.value?.let {
            if (it == 3) return true
        }
        return false
    }
    fun isPagerFirstIndex(): Boolean {
        selectedQuizIndex.value?.let {
            if (it == 0) return true
        }
        return false
    }

    fun resetHint() {
        patternQuizList.value?.let { list ->
            selectedQuizIndex.value?.let { index ->
                val hintSize = list[index].hints.size
                setHintBtnText(hintSize)
            }
        }

    }

}


