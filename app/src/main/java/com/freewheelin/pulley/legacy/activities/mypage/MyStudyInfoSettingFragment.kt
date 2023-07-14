package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API.RequestModel.SchoolInfo
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyStudyInfoSettingBinding
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.revision2021.activity.dialog.FindSchoolDialog
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.model.response.School
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MyStudyInfoSettingFragment : MyPageBaseFragment() {

    val findCityRepository by lazy { FindCityRepository() }
    internal val disposables = CompositeDisposable()
    var cityList: List<City> = mutableListOf()
    var selectedSchoolID: Int? = null
    var selectedCityID: Int? = null

    lateinit var binding: FragmentMyStudyInfoSettingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_study_info_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initSpinners()
    }

    private fun initUI() {
        with(binding) {
            layoutCitySelect.visibility = View.GONE
            layoutInfoOption.visibility = View.GONE

            selectSchool.setOnClickListener { openFindSchool() }
            findSchoolBtn.setOnClickListener { openFindSchool() }

            switchNoStudent.setOnCheckedChangeListener { buttonView, isChecked ->
                initAllSpinners()
                hideAllSpinners()

                selectedSchoolID = null
                selectedCityID = null
                selectSchool.text = ""

                layoutSchoolSelect.visibility = if(isChecked) View.GONE else View.VISIBLE
                layoutCitySelect.visibility = if(isChecked) View.VISIBLE else View.GONE

                if (layoutInfoOption.visibility == View.VISIBLE) layoutInfoOption.visibility = View.GONE
                registBtn.isEnabled = false

            }
            registBtn.isEnabled = false
            registBtn.setOnClickListener {
                if(registBtn.isEnabled) {
                    val grade = getGradeFromSpinner()
                    update(selectedSchoolID, selectedCityID, grade, selectRate.position, selectMajor.position)
                }
            }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun initAllSpinners() {
        binding.apply {
            listOf(selectElementaryGrade,
                selectMiddleGrade,
                selectHighGrade,
                selectServicesGrade,
                selectMajor,
                selectRate,
                selectCity)
                .forEach{ it.initSpinner() }
        }
    }
    private fun hideAllSpinners() {
        binding.apply {
            listOf(selectElementaryGrade,
                selectMiddleGrade,
                selectHighGrade,
                selectServicesGrade,
                selectMajor,
                selectRate)
                .forEach { it.visibleIf(false) }
        }
    }

    private fun getGradeFromSpinner(): Int {
        return when {
            isElementarySchoolUser() -> {
                binding.selectElementaryGrade.position + 10
            }
            isMiddleSchoolUser() -> {
                binding.selectMiddleGrade.position + 4
            }
            isHighSchoolUser() -> {
                binding.selectHighGrade.position
            }
            isNotSchoolUser() -> {
                when (binding.selectServicesGrade.position) {
                    1, 2, 3 -> binding.selectServicesGrade.position + 4
                    4, 5, 6, 7 -> binding.selectServicesGrade.position - 3
                    8 -> {
                        binding.selectEtc.position + 10
                    }
                    else -> 1
                }
            }
            else -> { 1 }
        }
    }

    private fun initSpinners() {
        setCites()
        setElementaryGrades()
        setMiddleGrades()
        setHighGrades()
        setServicesGrade()
        setMajors()
        setRates()
        setEtc()

        showMajor(false)
        showRates(false)
        showEtcList(false)
    }

    var selectedSchoolType: School.Type? = null
    private fun openFindSchool() {
        val dialog = FindSchoolDialog.newInstance()
        dialog.callback = { selected ->
            Log.d("학교검색","결과:${selected.name}")
            selectedSchoolID = selected.id
            binding.selectSchool.text = selected.name
            binding.layoutInfoOption.visibility = View.VISIBLE
            showElementaryGrade(selected.isElementary())
            showMiddleGrade(selected.isMiddle())
            showHighGrade(selected.isHigh())
            showServicesGrade(false)
            showEtcList(false)
            selectedSchoolType = selected.type
            checkRegist()
        }
        dialog.show(parentFragmentManager, "findSchoolDialog")
    }

    private fun checkRegist() {
        with(binding) {
            if(switchNoStudent.isChecked) {
                if(selectedCityID == null) {
                    registBtn.isEnabled = false
                    return
                }
            } else {
                if(selectedSchoolID == null) {
                    registBtn.isEnabled = false
                    return
                }
            }

            if (isMiddleSchoolUser()) {
                if (!selectMiddleGrade.isSelected) {
                    registBtn.isEnabled = false
                    return
                }
            }
            if (isHighSchoolUser()) {
                if (!selectHighGrade.isSelected) {
                    registBtn.isEnabled = false
                    return
                }
            }
            if (isElementarySchoolUser()) {
                if (!selectElementaryGrade.isSelected) {
                    registBtn.isEnabled = false
                    return
                }

                registBtn.isEnabled = selectElementaryGrade.position != 0
            }


            // 체크 값 변경사항 - 하나라도 변경내역이 있을 때만 반영

            MyApplication.user?.let { user ->
                val mainChanged = user.schoolID != selectedSchoolID || user.regionID != selectedCityID
                val middleGradeChanged = selectMiddleGrade.isSelected && user.grade.value != selectMiddleGrade.position + 4

                val rateChanged = selectRate.isSelected && user.rating != selectRate.position
                val majorChanged = selectMajor.isSelected && user.rawMajorType != Major.getValue(selectMajor.position - 1)

                if (isMiddleSchoolUser()) {
                    when (selectMiddleGrade.position) {
                        1, 2, 3 -> {
                            registBtn.isEnabled = mainChanged || middleGradeChanged
                        }
                        else -> registBtn.isEnabled = false
                    }
                }
                if (isHighSchoolUser()) {
                    when (selectHighGrade.position) {
                        1 -> {
                            registBtn.isEnabled = (mainChanged || rateChanged) && selectHighGrade.isSelected
                        }
                        2, 3, 4 -> {
                            registBtn.isEnabled = (mainChanged || majorChanged || rateChanged) && (selectMajor.isSelected && selectRate.isSelected)
                        }
                        else -> registBtn.isEnabled = false
                    }
                }
                if (isNotSchoolUser()) {
                    when (selectServicesGrade.position) {
                        1, 2, 3 -> {
                            registBtn.isEnabled = mainChanged || middleGradeChanged
                        }
                        4 -> {
                            registBtn.isEnabled = mainChanged && rateChanged && selectServicesGrade.isSelected
                        }
                        5, 6, 7 -> {
                            registBtn.isEnabled = (mainChanged || majorChanged || rateChanged) && ( selectMajor.isSelected && selectRate.isSelected)
                        }
                        8 -> {
                            registBtn.isEnabled = selectEtc.isSelected
                        }
                        else -> registBtn.isEnabled = false
                    }
                }
            }
        }
    }

    private fun setCites() {
        if(cityList.isEmpty()) {
            disposables += findCityRepository.getCities()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        response.data.forEach {
                            Log.d("도시검색", "cities=${it.id}, ${it.name}")
                        }

                        cityList = response.data
                        val data = cityList.map{ it.name }
                        val hint = "도시를 선택해주세요"
                        binding.selectCity.set(data, hint) { position ->
                            if (position > 0) {
                                val city = cityList.get(position-1)
                                selectedCityID = city.id
                                Log.d("도시검색", "position=$position 선택도시=${city.id}, ${city.name}")
                                binding.layoutInfoOption.visibility = View.VISIBLE
                                showServicesGrade(true)
                                showElementaryGrade(false)
                                showMiddleGrade(false)
                                showHighGrade(false)
                                checkRegist()
                            } else {
                                selectedCityID = null
                            }
                        }
                        setInitData()
                    }, {
                        Log.e("도시검색", "error=${it.localizedMessage}")
                    })
        }
    }

    private fun setInitData() {
        with(binding) {
            showElementaryGrade(false)
            showMiddleGrade(false)
            showHighGrade(false)
            showServicesGrade(false)

            MyApplication.user?.let { user ->
                if(user.schoolID != null) {
                    switchNoStudent.isChecked = false

                    selectedSchoolID = user.schoolID
                    selectSchool.text = user.schoolName
                    layoutInfoOption.visibility = View.VISIBLE
                    if (user.schoolName?.contains("고등") == true) {
                        showHighGrade(true)
                    }
                    if (user.schoolName?.contains("중학") == true) {
                        showMiddleGrade(true)
                    }
                    if (user.schoolName?.contains("초등") == true) {
                        showElementaryGrade(true)
                    }
                    setInitOption(user)
                } else if(user.regionID != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        switchNoStudent.isChecked = true
                        selectedCityID = user.regionID
                        val index = cityList.indexOfFirst {
                            it.id == user.regionID
                        }
                        selectCity.position = index + 1
                        setInitOption(user)
                    }
                }
            }
        }
    }

    private fun setInitOption(user: User) {
        with(binding) {

//            MyApplication.user?.let { user ->
                if (user.schoolID == null) { // 학교를 다니지 않습니다.
                    showServicesGrade(true)

                    selectElementaryGrade.position = 0
                    selectMiddleGrade.position = 0
                    selectHighGrade.position = 0
                    selectServicesGrade.position = when (user.grade.value) {
                        in 1 .. 4 -> {
                            user.grade.value + 4
                        }
                        in 5 .. 7 -> {
                            user.grade.value - 4
                        }
                        else -> {
                            showEtcList(true)
                            selectEtc.position = user.grade.value - 10
                            8
                        }
                    }

                } else {
                    if (Grade.isHigh(user.rawGrade)) {
                        selectElementaryGrade.position = 0
                        selectMiddleGrade.position = 0
                        selectHighGrade.position = user.grade.value
                        selectServicesGrade.position = 1

                        showServicesGrade(false)

                    } else if(Grade.isMiddle(user.rawGrade)) {
                        selectElementaryGrade.position = 0
                        selectMiddleGrade.position = user.grade.value - 4
                        selectHighGrade.position = 0
                        selectServicesGrade.position = 1

                        showMiddleGrade(true)

                    } else if (Grade.isElementary(user.rawGrade)) {
                        selectElementaryGrade.position = user.grade.value - 10
                        selectMiddleGrade.position = 0
                        selectHighGrade.position = 0
                        selectServicesGrade.position = 0

                        showElementaryGrade(true)
                    } else {
                        // 가입시 학교를 다니면 성인을 선택할 수 없음
                    }
                }

                selectRate.position = user.rating
                selectMajor.position =  if(user.rawMajorType == "") 0 else Major.list.indexOf(user.major) + 1

//            }
            registBtn.isEnabled = false
        }
    }

    private fun setElementaryGrades() {
        binding.apply {
            val data = Grade.elementaryList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectElementaryGrade.set(data, hint) { position ->
                showMajor(false)
                showRates(false)
                showEtcList(false)
                selectMajor.position = 0
                selectRate.position = 0
                checkRegist()
            }
        }
    }
    private fun setMiddleGrades() {
        binding.apply {
            val data = Grade.middleList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectMiddleGrade.set(data, hint) { position ->
                showMajor(false)
                showRates(false)
                showEtcList(false)
                selectMajor.position = 0
                selectRate.position = 0
                checkRegist()
            }
        }
    }
    private fun setHighGrades() {
        with(binding) {
            var data = Grade.highList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectHighGrade.set(data, hint) { position ->
                when(position) {
                    1 -> {
                        showMajor(false)
                        showRates(true)
                        showEtcList(false)
                        selectMajor.position = 0
                    }
                    2, 3, 4 -> {
                        showMajor(true)
                        showRates(true)
                        showEtcList(false)
                    }
                    else -> {
                        showMajor(false)
                        showRates(false)
                        showEtcList(false)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                }
                checkRegist()
            }
        }
    }
    private fun setServicesGrade() {
        with(binding) {
            var data = Grade.serviceGradeList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectServicesGrade.set(data, hint) { position ->
                when(position) {
                    1, 2, 3 -> {
                        showMajor(false)
                        showRates(false)
                        showEtcList(false)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                    4 -> {
                        showMajor(false)
                        showRates(true)
                        showEtcList(false)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                    5, 6, 7 -> {
                        showMajor(true)
                        showRates(true)
                        showEtcList(false)
                        selectMajor.position = 0
                    }
                    8 -> {
                        showMajor(false)
                        showRates(false)
                        showEtcList(true)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                    else -> {
                        showMajor(false)
                        showRates(false)
                        showEtcList(false)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                }
                checkRegist()
            }
        }
    }
    private fun setMajors() {
        var data = Major.list.map { it.title }
        val hint = "계열을 선택해주세요"
        binding.selectMajor.set(data, hint) { position ->
            checkRegist()
        }
    }

    private fun setRates() {
        val firstData = listOf("모름")
        val secondData = (1..9).toList().map { "$it 등급" }
        val data = firstData + secondData
        val hint = "등급을 선택해주세요"
        binding.selectRate.set(data, hint) { position ->
            checkRegist()
        }
    }
    private fun setEtc() {
        val data = Grade.etcList.map { it.tabTitle }
        val hint = "상세 정보를 선택해주세요"
        binding.selectEtc.set(data, hint) { position ->
            checkRegist()
        }
    }

    private fun showMajor(show:Boolean) {
        binding.selectMajor.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showRates(show:Boolean) {
        binding.selectRate.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showElementaryGrade(show: Boolean) {
        binding.selectElementaryGrade.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showMiddleGrade(show: Boolean) {
        binding.selectMiddleGrade.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showHighGrade(show: Boolean) {
        binding.selectHighGrade.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showServicesGrade(show: Boolean) {
        binding.selectServicesGrade.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun showEtcList(show: Boolean) {
        binding.selectEtc.visibility = if(show) View.VISIBLE else View.GONE
    }
    private fun update(school: Int?, region: Int?, selectedGrade: Int, rate: Int, majorIdx: Int) {
        SchoolInfo().apply {
            schoolID = school
            regionID = region
            grade = selectedGrade
            initMoGrade = rate
            majorType = Major.getValue(majorIdx - 1)

            Log.d(javaClass.simpleName, "major=${majorType} idx=$majorIdx")

            disposables += API_V2.updateSchoolInfo(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Log.d("학업정보", "update response=$response")
                        MyApplication.user?.let { user ->
                            user.schoolID = schoolID
                            user.schoolName = binding.selectSchool.text.toString()
                            user.regionID = regionID
                            user.regionName = cityList.firstOrNull { it.id == regionID }?.name
                            user.grade = Grade.init(grade)
                            user.rating = initMoGrade
                            user.rawMajorType = majorType
                            user.commit("mypage study info update")
                        }
                        setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                        onBackBtnClicked()
                    }, {
                        Log.e("학업정보", "update error=${it.localizedMessage}")
                        DialogUtils.showDialog(requireContext(),"학업정보 변경오류", it.localizedMessage, rightBtnText = "확인")
                    })
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
    private fun isElementarySchoolUser(): Boolean {
        return selectedSchoolType == School.Type.ELEMENTARY
    }
    private fun isMiddleSchoolUser(): Boolean {
        return binding.selectMiddleGrade.visibility == View.VISIBLE
    }
    private fun isHighSchoolUser(): Boolean {
        return binding.selectHighGrade.visibility == View.VISIBLE
    }
    private fun isNotSchoolUser(): Boolean {
        return binding.selectServicesGrade.visibility == View.VISIBLE
    }
}
