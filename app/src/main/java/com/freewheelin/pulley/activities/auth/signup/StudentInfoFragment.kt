package com.freewheelin.pulley.activities.auth.signup

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.databinding.FragmentSignupStudentInfoBinding
import com.freewheelin.pulley.revision2021.activity.dialog.FindSchoolDialog
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.revision2021.model.response.School
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
            binding.selectSchool.text = selected?.name?:""
            binding.layoutInfoOption.visibility = View.VISIBLE
            checkRegist()
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "findSchoolDialog")
    }

    private fun checkRegist() {
        binding.apply {
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
    }

    private fun setCites() {
        if(cityList.isEmpty()) {
            disposables += findCityRepository.getCities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Log.d("도시검색", "cities=$response")
                    cityList = response.data
                    val data = cityList.map{ it.name }
                    val hint = "도시를 선택해주세요"
                    binding.selectCity.set(data, hint) { position ->
                        if(position < 0) {
                            selectedCity = null
                            binding.layoutInfoOption.visibility = View.GONE
                        } else {
                            if(position >= cityList.size) {
                                selectedCity = cityList.get(cityList.size - 1)
                            } else {
                                selectedCity = cityList.get(position)
                            }
                            binding.layoutInfoOption.visibility = View.VISIBLE
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
        binding.selectGrade.set(data, hint) { position ->
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
        binding.selectMajor.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun showRates(show:Boolean) {
        binding.selectRate.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun setMajors() {
        var data = Major.list.map { it.title }
        val hint = "계열을 선택해주세요"
        binding.selectMajor.set(data, hint) { position ->
            checkRegist()
        }
    }

    private fun setRates() {
        var data = (1..9).toList().map { "$it 등급" }
        val hint = "등급을 선택해주세요"
        binding.selectRate.set(data, hint) { position ->
            checkRegist()
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}