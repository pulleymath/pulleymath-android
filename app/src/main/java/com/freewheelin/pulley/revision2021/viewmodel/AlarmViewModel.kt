package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.response.Alarm
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmViewModel  : BaseViewModel(), LifecycleObserver {
    private val alarmRepository: AlarmRepository by lazy { AlarmRepository() }

    val alarmList by lazy { MutableLiveData<List<Alarm>>() }
    val wantClose = MutableLiveData<Boolean>(false)
    val wantGoAlarmList = MutableLiveData<Boolean>(false)
    val sendLinkUrl = MutableLiveData<String>("")
    var currentTimeString: String? = null
    val showAlarmProgress by lazy { MutableLiveData(false) }

    fun fetchAlarmList(callback: (()->Unit) = {}) {
        showAlarmProgress.postValue(true)
        compositeDisposable += alarmRepository.fetchMessages()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showAlarmProgress.postValue(true) }
            .doOnError { showAlarmProgress.postValue(false) }
            .doOnComplete { showAlarmProgress.postValue(false) }
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchMessages list=>${response.data}")

                currentTimeString = response.current_time
                val resAlarmList = response.data.map {
                    it.isReadObservable.set(it.isRead)
                    it
                }.sortedBy { it.createdAt }.reversed()
                alarmList.postValue(resAlarmList)
                callback()
            }, { error ->

                val sdf by lazy { SimpleDateFormat("yyyy-MM-dd a HH:mm", Locale.KOREA) }
                val currentDate = sdf.format(Date())
                currentTimeString = currentDate.toString()
                Log.e(javaClass.simpleName, "fetchMessages error=${error.localizedMessage}")
                callback()
            })
    }

    fun readMessage(messageID: Int, callback: (()->Unit)) {
        compositeDisposable += alarmRepository.readMessage(messageID)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "readMessage list=>${res.data}")

                currentTimeString = res.current_time
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "readMessage error=${error.localizedMessage}")
                callback()
            })
    }

    fun readAllMessages() {
        compositeDisposable += alarmRepository.readAllMessages()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "readAllMessages list=>${res.data}")

                currentTimeString = res.current_time
                alarmList.value?.forEach {
                    it.isReadObservable.set(true)
                }

            }, { error ->
                Log.e(javaClass.simpleName, "readAllMessages error=${error.localizedMessage}")
            })
    }

    fun closeActivity() {
        wantClose.postValue(true)
    }
    fun backToList() {
        wantGoAlarmList.postValue(true)
    }
    fun goLinkUrl(url: String, v: View) {
        if (url.isNotEmpty()) sendLinkUrl.postValue(url)
    }

}