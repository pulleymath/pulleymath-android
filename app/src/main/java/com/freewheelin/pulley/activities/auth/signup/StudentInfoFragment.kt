package com.freewheelin.pulley.activities.auth.signup

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.revision2021.activity.dialog.FindSchoolDialog
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.revision2021.model.response.School
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_signup_student_info.*

class StudentInfoFragment : Fragment() {

    val findCityRepository by lazy { FindCityRepository() }

    var cityList: List<City> = mutableListOf()

    var infoInterface:StudentInfoInterface? = null

    var selectedSchool:School? = null
    var selectedCity: City? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is StudentInfoInterface) infoInterface = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_student_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initSpinners()
    }

    private fun initUI() {

        layoutCitySelect.visibility = View.GONE
        layoutInfoOption.visibility = View.GONE

        selectSchool.setOnClickListener { openFindSchool() }
        findSchoolBtn.setOnClickListener { openFindSchool() }
        backBtn.setOnClickListener { infoInterface?.goBack() }
        backBtnArrow.setOnClickListener { infoInterface?.goBack() }

        switchNoStudent.setOnCheckedChangeListener { buttonView, isChecked ->
            selectGrade.initSpinner()
            selectMajor.initSpinner()
            selectRate.initSpinner()
            selectCity.initSpinner()

            selectedSchool = null
            selectedCity = null
            selectSchool.text = ""

            layoutSchoolSelect.visibility = if(isChecked) View.GONE else View.VISIBLE
            layoutCitySelect.visibility = if(isChecked) View.VISIBLE else View.GONE

            if (layoutInfoOption.visibility == View.VISIBLE) layoutInfoOption.visibility = View.GONE

            registBtn.toDisableUI()

            showMajor(false)
            showRates(false)
        }

        registBtn.toDisableUI()
        registBtn.setOnClickListener {
            if(registBtn.isEnableUI()) {
                val schoolID = selectedSchool?.id
                val regionID = selectedCity?.id
                val grade = selectGrade.position - 1
                val initMoGrade = selectRate.position
                val majorType = selectMajor.position - 1
                infoInterface?.regist(schoolID, regionID, grade, initMoGrade, majorType)
            }
        }
    }

    private fun initSpinners() {
        setCites()
        setGrades()
        setMajors()
        setRates()

        showMajor(false)
        showRates(false)
    }

    private fun openFindSchool() {
        val dialog = FindSchoolDialog { selected ->
            Log.d("학교검색","결과:${selected?.name}")
            selectedSchool = selected
            selectSchool.text = selected?.name?:""
            layoutInfoOption.visibility = View.VISIBLE
            checkRegist()
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "findSchoolDialog")
    }

    private fun checkRegist() {
        if(switchNoStudent.isChecked){
            if(selectedCity == null) {
                registBtn.toDisableUI()
                return
            }
        } else {
            if(selectedSchool == null) {
                registBtn.toDisableUI()
                return
            }
        }
        when(selectGrade.position) {
            1 -> {
                registBtn.toEnableUI()
            }
            2 -> {
                if(selectRate.isSelected) registBtn.toEnableUI() else registBtn.toDisableUI()
            }
            3, 4, 5 -> {
                if(selectMajor.isSelected && selectRate.isSelected) registBtn.toEnableUI() else registBtn.toDisableUI()
            }
            else -> registBtn.toDisableUI()
        }
    }

    private fun setCites() {
        if(cityList.isEmpty()) {
            findCityRepository.getCities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Log.d("도시검색", "cities=$response")
                    cityList = response.data
                    val data = cityList.map{ it.name }
                    val hint = "도시를 선택해주세요"
                    selectCity.set(data, hint) { position ->
                        if(position < 0) {
                            selectedCity = null
                            layoutInfoOption.visibility = View.GONE
                        } else {
                            if(position >= cityList.size) {
                                selectedCity = cityList.get(cityList.size - 1)
                            } else {
                                selectedCity = cityList.get(position)
                            }
                            layoutInfoOption.visibility = View.VISIBLE
                        }
                        checkRegist()
                    }
                }, {
                    Log.e("도시검색", "error=${it.localizedMessage}")
                })
        }
    }

    private fun setGrades() {
        var data = Grade.list.map { it.tabTitle }
        val hint = "학년을 선택해주세요"
        selectGrade.set(data, hint) { position ->
            when(position) {
                2 -> {
                    showMajor(false)
                    showRates(true)
                }
                3, 4, 5 -> {
                    showMajor(true)
                    showRates(true)
                }
                else -> {
                    showMajor(false)
                    showRates(false)
                }
            }
            checkRegist()
        }
    }

    private fun showMajor(show:Boolean) {
        selectMajor.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun showRates(show:Boolean) {
        selectRate.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun setMajors() {
        var data = Major.list.map { it.title }
        val hint = "계열을 선택해주세요"
        selectMajor.set(data, hint) { position ->
            checkRegist()
        }
    }

    private fun setRates() {
        var data = (1..9).toList().map { "$it 등급" }
        val hint = "등급을 선택해주세요"
        selectRate.set(data, hint) { position ->
            checkRegist()
        }
    }
}