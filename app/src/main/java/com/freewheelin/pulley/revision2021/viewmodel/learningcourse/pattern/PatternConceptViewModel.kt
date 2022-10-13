package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class PatternConceptViewModel: BaseViewModel(), LifecycleObserver {

    val patternQuiz by lazy { MutableLiveData<LCPatternQuiz>() }

    fun initQuiz(quiz: LCPatternQuiz) {
        patternQuiz.value = quiz

    }
}