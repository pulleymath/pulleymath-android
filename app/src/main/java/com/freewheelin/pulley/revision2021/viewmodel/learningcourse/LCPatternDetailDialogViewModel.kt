package com.freewheelin.pulley.revision2021.viewmodel.learningcourse

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel

class LCPatternDetailDialogViewModel: BaseViewModel(), LifecycleObserver {

    val imageUrl by lazy { MutableLiveData<String>() }
}