package com.freewheelin.pulley.revision2021.viewmodel

import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.model.response.PdfLinkAnswerItem
import com.freewheelin.pulley.revision2021.repository.PdfRepository
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class PdfViewModel : BaseViewModel(), LifecycleObserver {

    private val pdfRepository: PdfRepository by lazy { PdfRepository() }

    val pdfOrgList by lazy { MutableLiveData<List<Pdf>>() }
    val pdfList by lazy { MutableLiveData<List<Pdf>>() }
    val showEmpty by lazy { MutableLiveData(false) }

    var categoryFilter: String? = ""
    var subjectFilter: String? = ""

    init {
        listPdf()
    }

    fun listPdf(title: String = "", page: Int = 0, size: Int = 1000, subjectCode: String = "", category: String = "") {
        pdfRepository.pdfList(title, page, size, subjectCode, category)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "listPdf list=>${response.data.content}")
                // 원본
                pdfOrgList.postValue(response.data.content)
                // 필터된거
                pdfList.postValue(response.data.content)
                showEmpty.postValue(response.data.content.isEmpty())
            }, { error ->
                Log.e(javaClass.simpleName, "listPdf error=${error.localizedMessage}")
            })
    }

    fun filter() {
        pdfOrgList.value?.let { orgList ->
            var result = orgList

            Log.d(javaClass.simpleName, "Filter subject=$subjectFilter")
            Log.d(javaClass.simpleName, "Filter category=$categoryFilter")

            if(subjectFilter?.isNotEmpty() == true) {
                Log.d(javaClass.simpleName, "Filter subject=isNotEmpty")
                result = result.filter { it.subject_code == subjectFilter }
            }

            if(categoryFilter?.isNotEmpty() == true) {
                Log.d(javaClass.simpleName, "Filter category=isNotEmpty")
                result = result.filter { it.category == categoryFilter }
            }

            result.forEach { println("${it.category} : ${it.subject_code} - ${it.title} (${it.subject})" ) }

            pdfList.postValue(result)
            showEmpty.postValue(result.isEmpty())
        }
    }

    val subjectListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            subjectFilter = if(position > 0) PdfListFilter.subject.keys.toList().get(position) else ""
            filter()
        }
    }

    val categoryListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            categoryFilter = if(position > 0) PdfListFilter.category.keys.toList().get(position) else ""
            filter()
        }
    }

    fun answer(cmBookId:Int, callback:(List<PdfLinkAnswerItem>)->Unit){
        pdfRepository.answer(cmBookId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                response.data?.let { callback(it) }
            }, { error ->
                Log.e(javaClass.simpleName, "answer=${error.localizedMessage}")
            })
    }
}

object PdfListFilter {
    val subject = mapOf<String, String>(
        "" to "과목 전체",
        "311" to "수학(상)",
        "312" to "수학(하)",
        "321" to "수학1",
        "322" to "수학2",
        "331" to "확률과 통계",
        "332" to "미적분",
        "333" to "기하")

    val subjectList = subject.values.toList()

    val category = mapOf<String, String>(
        "" to "학습 유형 전체",
        "개념서" to "개념서",
        "기출서" to "기출서",
        "심화서" to "심화서",
// TODO:       "연산서" to "연산서", 소정쌤이 빼라고 함
        "유형서" to "유형서"
        )

    val categoryList = category.values.toList()
}