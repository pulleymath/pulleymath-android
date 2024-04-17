package com.freewheelin.pulley.legacy.activities.auth.signup

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.databinding.FragmentSignupStudentInfoBinding
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.activity.dialog.FindSchoolDialog
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2023.SchoolType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers

class StudentInfoFragment : Fragment() {

    val findCityRepository by lazy { FindCityRepository() }
    internal val disposables = CompositeDisposable()

    var cityList: List<City> = mutableListOf()

    var infoInterface:StudentInfoInterface? = null

    var selectedSchool:School? = null
    var selectedCity: City? = null
    lateinit var binding: FragmentSignupStudentInfoBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is StudentInfoInterface) infoInterface = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup_student_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initSpinners()
    }

    private fun initUI() {
        binding.apply {
            layoutCitySelect.visibility = View.GONE
            layoutInfoOption.visibility = View.GONE

            selectSchool.setOnClickListener { openFindSchool() }
            findSchoolBtn.setOnClickListener { openFindSchool() }
            backBtn.setOnClickListener { infoInterface?.goBack() }
            backBtnArrow.setOnClickListener { infoInterface?.goBack() }

            switchNoStudent.setOnCheckedChangeListener { buttonView, isChecked ->
                initAllSpinners()
                hideAllSpinners()

                selectedSchool = null
                selectedCity = null
                selectSchool.text = ""


                layoutSchoolSelect.visibility = if(isChecked) View.GONE else View.VISIBLE
                layoutCitySelect.visibility = if(isChecked) View.VISIBLE else View.GONE

                if (layoutInfoOption.visibility == View.VISIBLE) layoutInfoOption.visibility = View.GONE

                registBtn.isEnabled = false

            }

            registBtn.isEnabled = false
            registBtn.setOnClickListener {
                if(registBtn.isEnabled) {
                    val schoolID = selectedSchool?.id
                    val regionID = selectedCity?.id
                    val grade = getGradeFromSpinner()
                    val initMoGrade = selectRate.position - 1
                    val majorType = selectMajor.position - 1
                    infoInterface?.regist(schoolID, regionID, grade, initMoGrade, majorType)
                }
            }
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

    var selectedSchoolType: SchoolType? = null
    private fun openFindSchool() {
        val dialog = FindSchoolDialog.newInstance()
        dialog.callback = { selected ->
            Log.d("학교검색","결과:${selected.name}")
            selectedSchool = selected
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
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "findSchoolDialog")
    }

    private fun checkRegist() {
        binding.apply {
            if(switchNoStudent.isChecked){
                if(selectedCity == null) {
                    registBtn.isEnabled = false
                    return
                }
            } else {
                if(selectedSchool == null) {
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

            if (isMiddleSchoolUser()) {
                when (selectMiddleGrade.position) {
                    1, 2, 3 -> {
                        registBtn.isEnabled = true
                    }
                    else -> registBtn.isEnabled = false
                }
            }
            if (isHighSchoolUser()) {
                when (selectHighGrade.position) {
                    1 -> {
                        registBtn.isEnabled = selectRate.isSelected && selectHighGrade.isSelected
                    }
                    2, 3, 4 -> {
                        registBtn.isEnabled = selectMajor.isSelected && selectRate.isSelected
                    }
                    else -> registBtn.isEnabled = false
                }
            }
            if (isNotSchoolUser()) {
                when (selectServicesGrade.position) {
                    1, 2, 3 -> {
                        registBtn.isEnabled = true
                    }
                    4 -> {
                        registBtn.isEnabled = selectRate.isSelected && selectServicesGrade.isSelected
                    }
                    5, 6, 7 -> {
                        registBtn.isEnabled = selectMajor.isSelected && selectRate.isSelected
                    }
                    8 -> {
                        registBtn.isEnabled = selectEtc.isSelected
                    }
                    else -> registBtn.isEnabled = false
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
                    Log.d("도시검색", "cities=$response")
                    cityList = response.data
                    val data = cityList.map { it.name }
                    val hint = "도시를 선택해주세요"
                    binding.selectCity.set(data, hint) { position ->
                        if (position < 1) {
                            selectedCity = null
                            binding.layoutInfoOption.visibility = View.GONE
                        } else {
                            selectedCity = if(position >= cityList.size) {
                                cityList.get(cityList.size - 1)
                            } else {
                                cityList.get(position - 1)
                            }
                            binding.layoutInfoOption.visibility = View.VISIBLE
                            showServicesGrade(true)
                            showElementaryGrade(false)
                            showMiddleGrade(false)
                            showHighGrade(false)
                        }
                        checkRegist()
                    }
                }, {
                    Log.e("도시검색", "error=${it.localizedMessage}")
                })
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
                    }
                    5, 6, 7 -> {
                        showMajor(true)
                        showRates(true)
                        showEtcList(false)
                    }
                    8 -> {
                        showMajor(false)
                        showRates(false)
                        showEtcList(true)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                    else -> { // etc
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
    private fun setMajors() {
        val data = Major.list.map { it.title }
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

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
    private fun isElementarySchoolUser(): Boolean {
        return selectedSchoolType == SchoolType.ELEMENTARY
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