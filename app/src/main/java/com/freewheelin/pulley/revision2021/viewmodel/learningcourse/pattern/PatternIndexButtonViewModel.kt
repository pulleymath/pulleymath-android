package com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class PatternIndexButtonViewModel: BaseViewModel(), LifecycleObserver {

    val isSelected by lazy { MutableLiveData<Boolean>(true) }

    fun toggleIsSelected() {
        isSelected.postValue(isSelected.value?.not())
    }
}