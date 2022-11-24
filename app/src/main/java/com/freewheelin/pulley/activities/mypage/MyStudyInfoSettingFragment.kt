package com.freewheelin.pulley.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.core.API.RequestModel.SchoolInfo
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyStudyInfoSettingBinding
import com.freewheelin.pulley.revision2021.activity.dialog.FindSchoolDialog
import com.freewheelin.pulley.revision2021.repository.FindCityRepository
import com.freewheelin.pulley.revision2021.model.response.City
import com.freewheelin.pulley.utils.DialogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers


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
                selectGrade.initSpinner()
                selectMajor.initSpinner()
                selectRate.initSpinner()
                selectCity.initSpinner()

                selectedSchoolID = null
                selectedCityID = null
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
                    update(selectedSchoolID, selectedCityID, selectGrade.position, selectRate.position, selectMajor.position)
                }
            }
            backBtn.setOnClickListener { onBackBtnClicked() }
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
            selectedSchoolID = selected?.id
            binding.selectSchool.text = selected?.name?:""
            binding.layoutInfoOption.visibility = View.VISIBLE
            checkRegist()
        }
        dialog.show(parentFragmentManager, "findSchoolDialog")
    }

    private fun checkRegist() {
        with(binding) {
            if(switchNoStudent.isChecked){
                if(selectedCityID == null) {
                    registBtn.toDisableUI()
                    return
                }
            } else {
                if(selectedSchoolID == null) {
                    registBtn.toDisableUI()
                    return
                }
            }

            if(!selectGrade.isSelected) {
                registBtn.toDisableUI()
                return
            }

            // 체크 값 변경사항 - 하나라도 변경내역이 있을 때만 반영

            MyApplication.user?.let { user ->
                val mainChanged = user.schoolID != selectedSchoolID || user.regionID != selectedCityID
                val gradeChanged = selectGrade.isSelected && user.grade.value != selectGrade.position - 1
                val rateChanged = selectRate.isSelected && user.rating != selectRate.position
                val majorChanged = selectMajor.isSelected && user.rawMajorType != Major.getValue(selectMajor.position - 1)


                when (selectGrade.position) {
                    1 -> {
                        if (mainChanged || gradeChanged) registBtn.toEnableUI() else registBtn.toDisableUI()
                    }
                    2 -> {
                        if ((mainChanged || rateChanged) && selectGrade.isSelected) registBtn.toEnableUI() else registBtn.toDisableUI()
                    }
                    3, 4, 5 -> {
                        if ((mainChanged || majorChanged || rateChanged) && ( selectMajor.isSelected && selectRate.isSelected)) registBtn.toEnableUI() else registBtn.toDisableUI()
                    }
                    else -> registBtn.toDisableUI()
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
                            if(position > 0) {
                                val city = cityList.get(position-1)
                                selectedCityID = city.id
                                Log.d("도시검색", "position=$position 선택도시=${city.id}, ${city.name}")
                                binding.layoutInfoOption.visibility = View.VISIBLE
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
            MyApplication.user?.let { user ->
                if(user.schoolID != null) {
                    switchNoStudent.isChecked = false

                    selectedSchoolID = user.schoolID
                    selectSchool.text = user.schoolName
                    layoutInfoOption.visibility = View.VISIBLE
                    setInitOption()
                } else if(user.regionID != null) {
                    switchNoStudent.isChecked = true

                    selectedCityID = user.regionID
                    val index = cityList.indexOfFirst { it.id == user.regionID }
                    selectCity.position = index
                    setInitOption()
                }
            }
        }
    }

    private fun setInitOption() {
        with(binding) {
            MyApplication.user?.let { user ->
                selectGrade.position = user.grade.value + 1
                selectRate.position = user.rating
                selectMajor.position =  if(user.rawMajorType == "") 0 else Major.list.indexOf(user.major) + 1
            }
            registBtn.toDisableUI()
        }
    }

    private fun setGrades() {
        with(binding) {
            var data = Grade.list.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectGrade.set(data, hint) { position ->
                when(position) {
                    2 -> {
                        showMajor(false)
                        showRates(true)
                        selectMajor.position = 0
                    }
                    3, 4, 5 -> {
                        showMajor(true)
                        showRates(true)
                    }
                    else -> {
                        showMajor(false)
                        showRates(false)
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
        var data = (1..9).toList().map { "$it 등급" }
        val hint = "등급을 선택해주세요"
        binding.selectRate.set(data, hint) { position ->
            checkRegist()
        }
    }

    private fun showMajor(show:Boolean) {
        binding.selectMajor.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun showRates(show:Boolean) {
        binding.selectRate.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun update(school: Int?, region: Int?, year: Int, rate: Int, majorIdx: Int) {
        SchoolInfo().apply {
            schoolID = school
            regionID = region
            grade = year - 1
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
}
