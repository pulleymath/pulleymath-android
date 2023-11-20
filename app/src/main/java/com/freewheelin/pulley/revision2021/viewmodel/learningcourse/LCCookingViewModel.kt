package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.app.Application
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ItemCookingQuizDetailBinding
import com.freewheelin.pulley.databinding.ItemCookingRightViewBinding
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCCookingFragment
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.LCCookingRepository
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.memoView.MemoView
import com.squareup.picasso.Picasso
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LCCookingViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {

    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val cookingRepository = LCCookingRepository(getApplication<Application>().applicationContext, viewModelScope)
//    private val _cookingInfo = MutableLiveData<CookingInfo>()
    val cookingInfo : LiveData<CookingInfo> = cookingRepository.cookingInfo

    private val _cookingInfoItems = MutableLiveData<List<CookingInfoItem>>()
    val cookingInfoItems : LiveData<List<CookingInfoItem>> = _cookingInfoItems

    val cookingImageUrl by lazy { MutableLiveData<String>() }
    val currentCookingExercise by lazy { MutableLiveData<CookingExercise>() }
    val selectedExerciseIndex by lazy { MutableLiveData<Int>() }

    val selectionImageUrlList by lazy { MutableLiveData<List<CookingQuizSelection>>() }
    val showSelection by lazy { MutableLiveData(false) }
    val showNumkeyboard by lazy { MutableLiveData(false) }
    val selectedShortQuiz by lazy { MutableLiveData<CookingQuiz>(null) }
    var selectedItemBinding: ItemCookingQuizDetailBinding? = null
    var rightViewBinding: ItemCookingRightViewBinding? = null

    val quizMemoViewList: MutableList<MemoView> = mutableListOf()
    var focusedQuizList: MutableList<CookingQuiz> = mutableListOf()
    val fingerDrawModeMode by lazy { MutableLiveData(false) }

    lateinit var adapter: LCCookingFragment.CookingAdapter

    fun initAdapterItem(courseId: Int) {
        // TODO flow 스펙엔 중복데이터를 stream하지않는데 중복된 자료를 반환함
//        cookingRepository.run {
//            flowAllCookingInfoItem(courseId)
//                .onEach { items ->
//                    println("aspasp flow all cookinginfoitem :${items.size}")
//                    _cookingInfoItems.value = items
//                }
//                .launchIn(viewModelScope)
//        }
        collectCookingInfoItems(courseId)
    }
    private fun collectCookingInfoItems(courseId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            val items = fetchCookingInfoItems(courseId)
            _isLoading.postValue(false)
//            upsertInfoItems(items)
            _cookingInfoItems.postValue(items)
            delay(200)
            selectedExerciseIndex.postValue(0)

        }
    }

    suspend fun fetchCookingInfoItems(courseId: Int): List<CookingInfoItem> {
        val res: Pair<List<CookingInfoItem>, String> = cookingRepository.fetchCookingInfoItems(courseId)
        cookingImageUrl.postValue(res.second)
        return res.first
    }
    suspend fun upsertInfoItems(items: List<CookingInfoItem>) {
        cookingRepository.upsertAllInfoItem(items)
    }


    fun useHint(exerciseQuizId: Int, callback: () -> Unit) {
        val studentId = user?.studentID ?: return
        compositeDisposable += cookingRepository.useHint(exerciseQuizId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "useHint =>${response.data}")
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "useHint error=${error.localizedMessage}")
            })
    }

    fun scoringCookingQuiz(quiz: CookingQuiz, userAnswer: String, callback: (Boolean) -> Unit) {
        val studentId = user?.studentID ?: return
        val scoringReq = ScoringReq(userAnswer)
        compositeDisposable += cookingRepository.scoringCookingQuiz(quiz.exerciseQuizId, studentId, scoringReq)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "scoringCookingQuiz =>${response.data}")
                response.data?.let {
                    if (quiz.exerciseQuizId == it.exerciseQuizId) {
//                        callback(it.isCorrect)
                        CoroutineScope(Dispatchers.Main).launch {
//                                quiz.isCorrectAnswer.set(it.isCorrect)
                            callback(it.isCorrect)
                        }
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "scoringCookingQuiz error=${error.localizedMessage}")
            })
    }

    fun sendExerciseScoringLog(callback: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog()
            if (logResponse.isChallengeCourse.not()) return@launch
            callback()
        }
    }

    suspend fun postLog(): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "문제풀이",
            itemName = "채점",
            itemValue = null,
            itemNote = "개념학습-예제",
        )
    }

    fun setCookingImgIv(src: String, iv: ImageView) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val requestCreator = Picasso.get()
                .load(src)

            val width = requestCreator.get().width
            val height = requestCreator.get().height
            withContext(Dispatchers.Main) {

                requestCreator
                    .resize(if (height > 5000) 3000 else width, 0)
                    .onlyScaleDown()
                    .into(iv)
            }

        }
    }
}