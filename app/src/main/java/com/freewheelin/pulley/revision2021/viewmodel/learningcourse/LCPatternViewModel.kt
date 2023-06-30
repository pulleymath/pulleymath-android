package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.repository.LCPatternRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2021.views.DisallowTouchEventViewPager
import com.freewheelin.pulley.revision2021.views.LCPatternViewPager
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.google.gson.Gson
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LCPatternViewModel(application: Application): BaseAndroidViewModel(application) {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val patternRepository = LCPatternRepository(getApplication<Application>().applicationContext, viewModelScope)

    val patternName by lazy { MutableLiveData<String>() }
    private val _patternQuizList by lazy { MutableLiveData<List<LCPatternQuiz>>() }
    val patternQuizList: LiveData<List<LCPatternQuiz>> = _patternQuizList

    val patternQuiz1 by lazy { MutableLiveData<LCPatternQuiz>() }
    val patternQuiz2 by lazy { MutableLiveData<LCPatternQuiz>() }
    val patternQuiz3 by lazy { MutableLiveData<LCPatternQuiz>() }

    val selectedQuizIndex by lazy { MutableLiveData<Int>(0) }
    val isHintBtnDisabled by lazy { MutableLiveData<Boolean>(false) }
    val hintExist by lazy { MutableLiveData<Boolean>(true) }
    val remainingHintSizeLive by lazy { MutableLiveData(0) }

    fun setPatternName(course: SingleCourseDesc) {
        val parentPatternName = course.name
        val sequence = course.sequence
        patternName.postValue("유형 0${sequence}. ${parentPatternName}")
    }

    fun initPatternInfo(patternId: Int) {
        patternRepository.flowAllPatternInfo(patternId)
            .onEach { patterns ->
                _patternQuizList.value = patterns
            }
            .launchIn(viewModelScope)
        fetchPatternInfo(patternId)
    }
    fun fetchPatternInfo(patternId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val patterns = fetchPatterns(patternId)
            _isLoading.postValue(false)
            upsertPatterns(patterns)
            setHintBtnText(patterns.first().hints.size)
        }
    }
    private suspend fun fetchPatterns(patternId: Int): List<LCPatternQuiz> {
        return patternRepository.fetchPatternInfo(patternId)
    }
    suspend fun upsertPatterns(patterns: List<LCPatternQuiz>) {
        patternRepository.upsertAll(patterns)
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
        remainingHintSizeLive.postValue(size)
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
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            patternQuizList.value?.let { upsertPatterns(it) }
        }
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


