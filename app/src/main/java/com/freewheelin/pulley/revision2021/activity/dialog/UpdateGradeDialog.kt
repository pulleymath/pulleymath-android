package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.databinding.DialogUpdateGradeBinding
import com.freewheelin.pulley.databinding.ItemFindSchoolForUpdateGradeBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2021.model.response.SchoolResponse
import com.freewheelin.pulley.revision2021.utils.listener.UpdateGradeSchoolSelectListener
import com.freewheelin.pulley.revision2021.viewmodel.UpdateGradeDialogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlinx.coroutines.*

class UpdateGradeDialog(): DialogFragment() {
    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            UpdateGradeDialogViewModel::class.java
        )
    }

    private val binding: DialogUpdateGradeBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.dialog_update_grade,
            null,
            false
        )
    }
    var callback: () -> Unit = {}
    companion object {
        fun newInstance(): UpdateGradeDialog {
            val args = Bundle().apply {

            }
            val instance = UpdateGradeDialog()
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSpinners()
        initUI()
    }

    private fun initUI() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.onExitClickCallback = {
                if (viewModel.showUpdateGradeView.value == true) {
                    dismiss()
                } else {
                    viewModel.openUpdateGrade()
                }
            }

            recyclerSchool.adapter = FindSchoolAdapter { selectedSchool ->
                viewModel.selectedSchoolID = selectedSchool.id
                viewModel.selectSchoolText.postValue(selectedSchool.name)
                viewModel.showInfoOption.postValue(true)
                viewModel.showSelectMiddleGrade.postValue(selectedSchool.isMiddle())
                viewModel.showSelectHighGrade.postValue(selectedSchool.isHigh())
                viewModel.showSelectAllGrade.postValue(false)
                checkRegist()

                val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(binding.root.windowToken, 0)

                viewModel.openUpdateGrade()
            }

            viewModel.dontGoSchool.observe(viewLifecycleOwner) { isChecked ->
                selectMiddleGrade.initSpinner()
                selectHighGrade.initSpinner()
                selectAllGrade.initSpinner()
                selectMajor.initSpinner()
                selectRate.initSpinner()
                selectCity.initSpinner()
                submitBtn.isEnabled = false
            }

            submitBtn.setOnClickListener {
                if (submitBtn.isEnabled.not()) return@setOnClickListener
                val schoolId = viewModel.selectedSchoolID
                val regionId = viewModel.selectedCityID
                var grade: Int = if (viewModel.showSelectMiddleGrade.value == true) {
                    convertPositionToMiddleGrade(selectMiddleGrade.position)
                } else if (viewModel.showSelectHighGrade.value == true) {
                    selectHighGrade.position
                } else if (viewModel.showSelectAllGrade.value == true) {
                    convertPositionToAllGrade(selectAllGrade.position)
                } else { 1 }

                val initMoGrade = selectRate.position
                val majorType = Major.getValue(selectMajor.position - 1)
                viewModel.updateGrade(schoolId, regionId, grade, initMoGrade, majorType) {
                    MyApplication.user?.let { user ->
                        user.schoolID = schoolId
                        user.schoolName = viewModel.selectSchoolText.value.toString()
                        user.regionID = regionId
                        user.regionName = viewModel.cityList.firstOrNull { it.id == regionId }?.name
                        user.userGrade = Grade.init(grade)
                        user.initMoGrade = initMoGrade
                        user.majorType = majorType
                        user.commit("update grade dialog")
                    }
                    callback()
                    dismiss()
                }
            }
        }
    }

    private fun convertPositionToMiddleGrade(position: Int): Int {
        return when (position) {
            1, 2, 3 -> position + 4
            else -> 1
        }
    }
    private fun convertPositionToAllGrade(position: Int): Int {
        return when (position) {
            1, 2, 3 -> position + 4
            4, 5, 6, 7 -> position - 3
            else -> 1
        }
    }

    private fun initSpinners() {
        viewModel.setCites() { cityList ->
            val data = cityList.map{ it.name }
            val hint = "도시를 선택해주세요"
            binding.selectCity.set(data, hint) { position ->
                if(position > 0) {
                    val city = cityList.get(position - 1)
                    viewModel.selectedCityID = city.id
                    viewModel.showInfoOption.postValue(true)
                    viewModel.showSelectAllGrade.postValue(true)
                    checkRegist()
                } else {
                    viewModel.selectedCityID = null
                }
            }
            setInitData()
        }
        setMiddleGrades()
        setHighGrades()
        setAllGrades()
        setMajors()
        setRates()

        viewModel.showSelectMajor.postValue(false)
        viewModel.showSelectRate.postValue(false)
    }
    private fun setMiddleGrades() {
        binding.apply {
            val data = Grade.middleList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectMiddleGrade.set(data, hint) { position ->
                viewModel.showSelectMajor.postValue(false)
                viewModel.showSelectRate.postValue(false)
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
                        viewModel.showSelectMajor.postValue(false)
                        viewModel.showSelectRate.postValue(true)
                        selectMajor.position = 0
                    }
                    2, 3, 4 -> {
                        viewModel.showSelectMajor.postValue(true)
                        viewModel.showSelectRate.postValue(true)
                    }
                    else -> {
                        viewModel.showSelectMajor.postValue(false)
                        viewModel.showSelectRate.postValue(false)
                        selectMajor.position = 0
                        selectRate.position = 0
                    }
                }
                checkRegist()
            }
        }
    }
    private fun setAllGrades() {
        with(binding) {
            var data = Grade.serviceGradeList.map { it.tabTitle }
            val hint = "학년을 선택해주세요"
            selectAllGrade.set(data, hint) { position ->
                when(position) {
                    4 -> {
                        viewModel.showSelectMajor.postValue(false)
                        viewModel.showSelectRate.postValue(true)
                        selectMajor.position = 0
                    }
                    5, 6, 7 -> {
                        viewModel.showSelectMajor.postValue(true)
                        viewModel.showSelectRate.postValue(true)
                    }
                    else -> {
                        viewModel.showSelectMajor.postValue(false)
                        viewModel.showSelectRate.postValue(false)
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

    private fun checkRegist() {
        with(binding) {
            if(switchNoStudent.isChecked){
                if(viewModel.selectedCityID == null) {
                    submitBtn.isEnabled = false
                    return
                }
            } else {
                if(viewModel.selectedSchoolID == null) {
                    submitBtn.isEnabled = false
                    return
                }
            }

            if (viewModel.showSelectMiddleGrade.value == true) {
                if (!selectMiddleGrade.isSelected) {
                    submitBtn.isEnabled = false
                    return
                }
            }
            if (viewModel.showSelectHighGrade.value == true) {
                if (!selectHighGrade.isSelected) {
                    submitBtn.isEnabled = false
                    return
                }
            }

            // 체크 값 변경사항 - 하나라도 변경내역이 있을 때만 반영

            MyApplication.user?.let { user ->
                val mainChanged = user.schoolID != viewModel.selectedSchoolID || user.regionID != viewModel.selectedCityID
                val middleGradeChanged = selectMiddleGrade.isSelected && user.userGrade.value != selectMiddleGrade.position + 4

                val rateChanged = selectRate.isSelected
                val majorChanged = selectMajor.isSelected && user.majorType != Major.getValue(selectMajor.position - 1)

                if (viewModel.showSelectMiddleGrade.value == true) {
                    when (selectMiddleGrade.position) {
                        1, 2, 3 -> {
                            submitBtn.isEnabled = mainChanged || middleGradeChanged
                        }
                        else -> submitBtn.isEnabled = false
                    }
                }
                if (viewModel.showSelectHighGrade.value == true) {
                    when (selectHighGrade.position) {
                        1 -> {
                            submitBtn.isEnabled = (mainChanged || rateChanged) && selectHighGrade.isSelected
                        }
                        2, 3, 4 -> {
                            submitBtn.isEnabled = (mainChanged || majorChanged || rateChanged) && ( selectMajor.isSelected && selectRate.isSelected)
                        }
                        else -> submitBtn.isEnabled = false
                    }
                }
                if (viewModel.showSelectAllGrade.value == true) {
                    when (selectAllGrade.position) {
                        1, 2, 3 -> {
                            submitBtn.isEnabled = mainChanged || middleGradeChanged
                        }
                        4 -> {
                            submitBtn.isEnabled = mainChanged && rateChanged && selectAllGrade.isSelected
                        }
                        5, 6, 7 -> {
                            submitBtn.isEnabled = (mainChanged || majorChanged || rateChanged) && ( selectMajor.isSelected && selectRate.isSelected)
                        }
                        else -> submitBtn.isEnabled = false
                    }
                }
            }
        }
    }

    private fun setInitData() {
        with(binding) {
            MyApplication.user?.let { user ->
                if(user.schoolID != null && user.schoolID != 0) {
                    switchNoStudent.isChecked = false

                    viewModel.selectedSchoolID = user.schoolID
                    viewModel.selectSchoolText.postValue(user.schoolName)
                    viewModel.showInfoOption.postValue(true)

                    if (user.schoolType?.isHigh == true) {
                        viewModel.showSelectMiddleGrade.postValue(false)
                        viewModel.showSelectHighGrade.postValue(true)
                        viewModel.showSelectAllGrade.postValue(false)
                    }
                    if (user.schoolType?.isMiddle == true) {
                        viewModel.showSelectMiddleGrade.postValue(true)
                        viewModel.showSelectHighGrade.postValue(false)
                        viewModel.showSelectAllGrade.postValue(false)
                    }
                    setInitOption()
                } else if(user.regionID != null && user.regionID != 0) {
                    switchNoStudent.isChecked = true

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        viewModel.selectedCityID = user.regionID
                        viewModel.showSelectMiddleGrade.postValue(false)
                        viewModel.showSelectHighGrade.postValue(false)
                        viewModel.showSelectAllGrade.postValue(true)
                        val index = viewModel.cityList.indexOfFirst { it.id == user.regionID }
                        selectCity.position = index
                        setInitOption()
                    }
                }
            }
        }
    }
    private fun setInitOption() {
        with(binding) {
            MyApplication.user?.let { user ->
                if (Grade.isHigh(user.grade)) {
                    selectMiddleGrade.position = 0
                    selectHighGrade.position = user.userGrade.value
                } else {
                    selectMiddleGrade.position = max(0, user.userGrade.value - 4)
                    selectHighGrade.position = 0
                }

                selectRate.position = user.initMoGrade
                selectMajor.position =  if(user.majorType == "") 0 else Major.list.indexOf(user.userMajor) + 1
            }
            submitBtn.isEnabled = false
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearCompositeDisposable()
    }
    inner class FindSchoolAdapter(
        private val listener: UpdateGradeSchoolSelectListener
    ): ListAdapter<School, FindSchoolAdapter.Holder>(
        DiffCallback<School>()
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_find_school_for_update_grade, parent, false)
            val binding: ItemFindSchoolForUpdateGradeBinding = DataBindingUtil.bind(view)!!
            return Holder(binding, listener)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(
            private val binding: ItemFindSchoolForUpdateGradeBinding,
            private val listener: UpdateGradeSchoolSelectListener
            ): RecyclerView.ViewHolder(binding.root) {
                fun bind(item: School) {
                    binding.item = item
                    binding.listener = listener
                }
        }
    }
}


@BindingAdapter("bind_school")
fun bindSchoolRecyclerView(recyclerView: RecyclerView, item: SchoolResponse?){
    item?.let { response ->
        val adapter = recyclerView.adapter as UpdateGradeDialog.FindSchoolAdapter
        adapter.submitList(response.data.content)
    }
}