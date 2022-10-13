package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.cooking

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingQuiz
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class CookingQuizViewModel : BaseViewModel(), LifecycleObserver {
    val cookingExercise by lazy { MutableLiveData<CookingExercise>() }

//    val cookingQuizList by lazy { MutableLiveData<List<CookingQuiz>>() }
    val cookingQuiz0 by lazy { MutableLiveData<CookingQuiz>() }
    val cookingQuiz1 by lazy { MutableLiveData<CookingQuiz>(null) }
    val cookingQuiz2 by lazy { MutableLiveData<CookingQuiz>(null) }
    val cookingQuiz3 by lazy { MutableLiveData<CookingQuiz>(null) }
    val cookingQuiz4 by lazy { MutableLiveData<CookingQuiz>(null) }
    val cookingQuiz5 by lazy { MutableLiveData<CookingQuiz>(null) }

    fun initCookingExercise(exercise: CookingExercise) {
        cookingExercise.value = exercise

        cookingExercise.value?.exerciseQuizzes?.forEachIndexed { index, quiz ->
            when (index) {
                0 -> { cookingQuiz0.value = quiz }
                1 -> { cookingQuiz1.value = quiz }
                2 -> { cookingQuiz2.value = quiz }
                3 -> { cookingQuiz3.value = quiz }
                4 -> { cookingQuiz4.value = quiz }
                else -> { cookingQuiz5.value = quiz }
            }
        }
    }
}