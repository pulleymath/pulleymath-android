package com.freewheelin.pulley.revision2021.repository

import androidx.lifecycle.MutableLiveData

class PatternQuizRepository {

    companion object {
        val instance: PatternQuizRepository by lazy { PatternQuizRepository() }
    }

    // resumeLCPatternFloatingAnswerSheetLocation 을 대체해 보려고 만들어놓음
    val floatingAnswerSheetLastLocation = MutableLiveData<String>("")

}