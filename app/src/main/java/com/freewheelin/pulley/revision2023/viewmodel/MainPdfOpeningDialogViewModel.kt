package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.model.response.PdfLinkAnswerItem
import com.freewheelin.pulley.revision2021.repository.PdfRepository
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainPdfOpeningDialogViewModel(application: Application): BaseAndroidViewModel(application) {

    private val userRepository by lazy { UserRepository.instance }
    private val myPageRepository by lazy { MyPageRepository.instance }
    private val pdfRepository: PdfRepository by lazy { PdfRepository() }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType
    var pdfId = 0
    val pdf = MutableLiveData<Pdf?>()
    val pdfAnswer = MutableLiveData<Pdf?>()
    val indicatorText = MutableLiveData<String>("다운로드 중이에요.")


    lateinit var onExitClickCallback: (() -> Unit)

    private val _recommendCommonSubjects = MutableLiveData<List<RecommendSubject>>()

    fun exitBtn() {
        onExitClickCallback()
    }

    fun fetchPdfOnId(pdfId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val fetchedPdf = pdfRepository.fetchPdf(pdfId)
            pdfAnswer.postValue(fetchedPdf?.answer)
            pdf.postValue(fetchedPdf)
//            cb(pdf)
        }
    }
    fun fetchPdfAnswer(cmBookId:Int, callback:(List<PdfLinkAnswerItem>?)->Unit) {
        compositeDisposable += pdfRepository.answer(cmBookId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                response.data?.let { callback(it) }
            }, { error ->
                callback(null)
                Log.e(javaClass.simpleName, "answer=${error.localizedMessage}")
            })
    }
}