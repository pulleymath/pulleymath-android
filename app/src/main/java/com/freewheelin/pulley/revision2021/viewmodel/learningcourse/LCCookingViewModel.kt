package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.LCCookingRepository
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
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

    val cookingExercise0 by lazy { MutableLiveData<CookingExercise>() }
    val cookingExercise1 by lazy { MutableLiveData<CookingExercise>(null) }
    val cookingExercise2 by lazy { MutableLiveData<CookingExercise>(null) }
    val cookingExercise3 by lazy { MutableLiveData<CookingExercise>(null) }
    val cookingExercise4 by lazy { MutableLiveData<CookingExercise>(null) }
    val cookingExercise5 by lazy { MutableLiveData<CookingExercise>(null) }

    @SuppressLint("CheckResult")
    fun fetchCookingGroceries(courseId: Int) {
        val studentId = user?.studentID ?: return
        cookingRepository.fetchCookingGroceries(courseId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchCookingGroceries =>${response.data}")
                response.data?.let {

                    cookingInfo.postValue(it)

                    val video = listOf(CookingInfoItem(0, it.video))
                    val exerciseList = listOf(CookingInfoItem(1, it.exerciseGroups)).map { item ->
                        item.exerciseList?.forEach { exec ->
                            exec.exerciseQuizzes?.forEach { quiz ->
                                val isSolved = quiz.userAnswer != null
                                val isCorrectAnswer = quiz.userAnswer == quiz.answer
                                quiz.afterTryAnswered.set(isSolved)
                                quiz.isAnswerEntered.set(isSolved)
                                quiz.isCorrectAnswer.set(isCorrectAnswer)
                                if (quiz.format == QuizFormat.Single) {
                                    quiz.userAnswer?.toInt()?.let { position ->
                                        val selectedImageUrl = quiz.answerOptions[position - 1].imageUrl
                                        quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)
                                    }
                                }
                            }
                        }
                        item
                    }

                    val sumList = (video + exerciseList).sortedBy { it.order }

                    cookingList.postValue(sumList)
                    sumList.forEach { cookingInfoItem ->
                        if (cookingInfoItem.type == CookingInfoItem.ItemType.Exercise) {
                            cookingInfoItem.exerciseList?.forEachIndexed { index, cookingExercise ->
                                when (index) {
                                    0 -> { cookingExercise0.postValue(cookingExercise) }
                                    1 -> { cookingExercise1.postValue(cookingExercise) }
                                    2 -> { cookingExercise2.postValue(cookingExercise) }
                                    3 -> { cookingExercise3.postValue(cookingExercise) }
                                    4 -> { cookingExercise4.postValue(cookingExercise) }
                                    else -> { cookingExercise5.postValue(cookingExercise) }
                                }
                            }
                        }
                    }

                    cookingImageUrl.postValue(it.imageUrl)

                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchCookingGroceries error=${error.localizedMessage}")
            })
    }

    var exercisePosition: Int = 0
    fun setCurrentExercisePosition(position: Int) {
        exercisePosition = position

    }

    @SuppressLint("CheckResult")
    fun useHint(exerciseQuizId: Int, callback: () -> Unit) {
        val studentId = user?.studentID ?: return
        cookingRepository.useHint(exerciseQuizId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "useHint =>${response.data}")
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "useHint error=${error.localizedMessage}")
            })
    }

    @SuppressLint("CheckResult")
    fun scoringCookingQuiz(quiz: CookingQuiz, userAnswer: String, callback: () -> Unit) {
        val studentId = user?.studentID ?: return
        val scoringReq = ScoringReq(userAnswer)
        cookingRepository.scoringCookingQuiz(quiz.exerciseQuizId, studentId, scoringReq)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "scoringCookingQuiz =>${response.data}")
                response.data?.let {
                    if (quiz.exerciseQuizId == it.exerciseQuizId) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                quiz.isCorrectAnswer.set(it.isCorrect)
                                callback()
                            }
                        }
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "scoringCookingQuiz error=${error.localizedMessage}")
            })
    }

    fun findQuizExercise(quiz: CookingQuiz): CookingExercise? {
        val asd = listOfNotNull(
            cookingExercise0.value,
            cookingExercise1.value,
            cookingExercise2.value,
            cookingExercise3.value,
            cookingExercise4.value,
        ).filter {
            val isContained = it.exerciseQuizzes?.contains(quiz)
            isContained == true
        }
        return if (asd.isNotEmpty()) asd[0] else null
    }
}