package com.freewheelin.pulley.revision2021.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.freewheelin.pulley.revision2021.repository.FindSchoolRepository
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2021.model.response.SchoolResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FindSchoolViewModel : BaseViewModel(), LifecycleObserver {

    private val repository by lazy { FindSchoolRepository() }

    val currentPage = MutableLiveData<String>("0")
    val totalPage = MutableLiveData<String>("0")
    val showEmptyString = MutableLiveData<Boolean>(false)

    val schoolResponse by lazy {
        MutableLiveData<SchoolResponse>()
    }

    var word = ""

    fun textChanged(s: CharSequence,start: Int,before : Int,count :Int){
        word = s.toString()
        if(word.length > 1) searchSchool()
    }

    fun searchSchool(page:Int=0) {
        repository.searchSchool(word, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Log.d("학교검색", "response=$response page=$page empty=${response.data.empty}")
                    currentPage.postValue("${page+1} ")
                    totalPage.postValue(" ${response.data.totalPages}")
                    schoolResponse.postValue(response)
                    showEmptyString.postValue( response.data.empty)
                },{
                    showEmptyString.postValue(false)
                    Log.e("학교검색", "error=${it.localizedMessage} page=$page")
                })
    }

    fun onPrevClick() {
        Log.d("학교검색", "currentPage.value=${currentPage.value} word=$word")
        currentPage.value?.let { page ->
            val current = page.trim().toInt() - 1
            if(current > 0) {
                searchSchool( current - 1)
            }
        }
    }

    fun onNextClick() {
        Log.d("학교검색", "currentPage.value=${currentPage.value} word=$word total=${totalPage.value}")
        currentPage.value?.let { page ->
            val current = page.trim().toInt() - 1
            val total = totalPage.value?.trim()?.toInt() ?: 0

            if(current in 0 until total-1) {
                searchSchool( current  + 1)
            }
        }
    }

    var onItemClickCallback: ((school: School?) -> Unit)? = null

    fun onItemClick(school:School) {
        Log.d("학교검색", "onItemClick .school=${school}")
        onItemClickCallback?.let { it(school) }
    }
}