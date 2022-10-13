package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class PatternSolutionViewModel: BaseViewModel(), LifecycleObserver {
    val quiz by lazy { MutableLiveData<LCPatternQuiz>() }
    val solutionUrl by lazy { MutableLiveData<String>() }

    fun initSolution(quiz: LCPatternQuiz) {
        this.quiz.postValue(quiz)
        solutionUrl.postValue(quiz.solutionImageUrl)
    }
}