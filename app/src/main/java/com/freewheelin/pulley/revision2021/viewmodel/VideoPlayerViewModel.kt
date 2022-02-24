package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.revision2021.model.response.AffiliatedStudentWorkbook
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import com.freewheelin.pulley.utils.DialogUtils
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class VideoPlayerViewModel : BaseViewModel(), LifecycleObserver {
    var isSubmitBtnActive = MutableLiveData(false)

    fun asd () {

    }
}