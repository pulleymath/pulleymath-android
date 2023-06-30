package com.freewheelin.pulley.revision2021.viewmodel

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.core.API.RequestModel.SchoolInfo
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2021.model.response.SchoolResponse
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.repository.FindSchoolRepository
import com.freewheelin.pulley.legacy.utils.DialogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateGradeDialogViewModel: BaseViewModel() {
    private val findSchoolRepository by lazy { FindSchoolRepository() }
    val findCityRepository by lazy { FindCityRepository() }

    val userName by lazy { MutableLiveData<String>(user?.fullName) }

    val showUpdateGradeView by lazy { MutableLiveData<Boolean>(true) }
    val showFindSchoolView by lazy { MutableLiveData<Boolean>(false) }

    val currentPage = MutableLiveData<String>("0")
    val totalPage = MutableLiveData<String>("0")
    val showEmptyString = MutableLiveData<Boolean>(false)
    val schoolResponse by lazy { MutableLiveData<SchoolResponse>() }

    val dontGoSchool = MutableLiveData<Boolean>()
    val showSchoolSelect = MutableLiveData(true)
    val showCitySelect = MutableLiveData(false)
    val showInfoOption = MutableLiveData(true)
    val showSelectMiddleGrade = MutableLiveData(false)
    val showSelectHighGrade = MutableLiveData(false)
    val showSelectAllGrade = MutableLiveData(false)
    val showSelectMajor = MutableLiveData(true)
    val showSelectRate = MutableLiveData(true)
    val selectSchoolText = MutableLiveData("")

    var cityList: List<City> = mutableListOf()

    fun openFindSchool() {
        showUpdateGradeView.postValue(false)
        showFindSchoolView.postValue(true)
    }
    fun openUpdateGrade() {
        showUpdateGradeView.postValue(true)
        showFindSchoolView.postValue(false)
    }
    fun exitBtn() {
        onExitClickCallback()
    }
    fun schoolSwitchChecked(flag: Boolean) {
        dontGoSchool.postValue(flag)
        showSchoolSelect.postValue(!flag)
        showCitySelect.postValue(flag)
        showInfoOption.postValue(false)
        showSelectMajor.postValue(false)
        showSelectRate.postValue(false)
        selectSchoolText.postValue("")
        selectedSchoolID = null
        selectedCityID = null

        showSelectHighGrade.postValue(false)
        showSelectMiddleGrade.postValue(false)
    }

    lateinit var onExitClickCallback: (() -> Unit)

    var word = ""
    fun textChanged(s: CharSequence,start: Int,before : Int,count :Int){
        word = s.toString()
        if(word.length > 1) searchSchool()
    }

    fun searchSchool(page:Int=0) {
        compositeDisposable += findSchoolRepository.searchSchool(word, page)
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
    fun onSearchSchoolPrevClick() {
        Log.d("학교검색", "currentPage.value=${currentPage.value} word=$word")
        currentPage.value?.let { page ->
            val current = page.trim().toInt() - 1
            if(current > 0) {
                searchSchool( current - 1)
            }
        }
    }

    fun onSearchSchoolNextClick() {
        Log.d("학교검색", "currentPage.value=${currentPage.value} word=$word total=${totalPage.value}")
        currentPage.value?.let { page ->
            val current = page.trim().toInt() - 1
            val total = totalPage.value?.trim()?.toInt() ?: 0

            if(current in 0 until total-1) {
                searchSchool( current  + 1)
            }
        }
    }
    fun updateGrade(schoolId: Int?, region: Int?, year: Int, rate: Int, major: String, cb: () -> Unit) {

        SchoolInfo().apply {
            schoolID = schoolId
            regionID = region
            grade = year
            initMoGrade = rate
            majorType = major

            compositeDisposable += API_V2.updateSchoolInfo(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Log.d("학업정보", "update response=$response")
                    cb()
                }, {
                    Log.e("학업정보", "update error=${it.localizedMessage}")
//                    DialogUtils.showDialog(requireContext(),"학업정보 변경오류", it.localizedMessage, rightBtnText = "확인")
                })
        }
    }
    var selectedCityID: Int? = 0
    var selectedSchoolID: Int? = 0

    fun setCites(cb: (List<City>) -> Unit) {
        if(cityList.isEmpty()) {
            compositeDisposable += findCityRepository.getCities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.data.forEach {
                        Log.d("도시검색", "cities=${it.id}, ${it.name}")
                    }
                    cityList = response.data
                    cb(cityList)
                    showInfoOption.postValue(cityList.isNotEmpty())

                }, {
                    Log.e("도시검색", "error=${it.localizedMessage}")
                })
        }
    }

}