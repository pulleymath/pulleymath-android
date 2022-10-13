package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.cooking

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.CookingQuizSelection
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class CookingQuizAnswerSelectViewModel: BaseViewModel(), LifecycleObserver {

    val selectionImageUrlList by lazy { MutableLiveData<List<CookingQuizSelection>>() }

    fun initImageUrlList(list: List<String>?) {
        val quizSelectionList = list?.mapIndexed { index, s ->
            CookingQuizSelection(s, index + 1)
        }
        selectionImageUrlList.postValue(quizSelectionList)
    }
}