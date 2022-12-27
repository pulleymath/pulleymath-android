package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ItemCookingQuizDetailBinding
import com.freewheelin.pulley.databinding.ItemCookingRightViewBinding
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.LCCookingRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import io.channel.plugin.android.extension.doOnElse
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LCCookingViewModel : BaseViewModel(), LifecycleObserver {

    private val cookingRepository by lazy { LCCookingRepository() }
    val cookingInfo by lazy { MutableLiveData<CookingInfo>() }
    val cookingImageUrl by lazy { MutableLiveData<String>() }

    val cookingList by lazy { MutableLiveData<List<CookingInfoItem>>() }

    val currentCookingExercise by lazy { MutableLiveData<CookingExercise>() }
    val selectedExerciseIndex by lazy { MutableLiveData<Int>() }

    val selectionImageUrlList by lazy { MutableLiveData<List<CookingQuizSelection>>() }
    val showSelection by lazy { MutableLiveData(false) }
    val showNumkeyboard by lazy { MutableLiveData(false) }
    val selectedShortQuiz by lazy { MutableLiveData<CookingQuiz>(null) }
    var selectedItemBinding: ItemCookingQuizDetailBinding? = null
    var rightViewBinding: ItemCookingRightViewBinding? = null

    fun fetchCookingGroceries(courseId: Int) {
        val studentId = user?.studentID ?: return
        compositeDisposable += cookingRepository.fetchCookingGroceries(courseId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchCookingGroceries =>${response.data}")
                response.data?.let {

                    cookingInfo.postValue(it)
                    val video = listOf(CookingInfoItem.getVideoItem(0, it.video, it.exerciseGroups))
                    val footer = listOf(CookingInfoItem.getFooter(0))
                    val exerciseList = listOf(CookingInfoItem.getExercise(1, it, it.exerciseGroups)).map { item ->
                        item.exerciseList?.forEach { exec ->
                            exec.exerciseQuizzes?.forEach { quiz ->
                                val isSolved = quiz.userAnswer != null
                                val isCorrectAnswer = quiz.userAnswer == quiz.answer
                                quiz.afterTryAnswered.set(isSolved)
                                quiz.isAnswerEntered.set(isSolved)
                                quiz.isCorrectAnswer.set(isCorrectAnswer)
                                if (quiz.format == QuizFormat.Single) {
                                    quiz.userAnswer?.toInt()?.let { position ->
                                        val selectedImageUrl = quiz.sortedAnswerOptions[position - 1].imageUrl
                                        quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)
                                    }
                                }
                            }
                        }
                        item
                    }

                    val sumList = (video + exerciseList + footer).sortedBy { it.order }

                    cookingList.postValue(sumList)
                    cookingImageUrl.postValue(it.imageUrl)
                    selectedExerciseIndex.postValue(0)

                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchCookingGroceries error=${error.localizedMessage}")
            })
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

}