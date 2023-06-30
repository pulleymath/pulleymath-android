package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.BigUnitV3
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentStudyUnitOptionalSettingBinding
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener

class StudyOptionalUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {

    lateinit var binding: FragmentStudyUnitOptionalSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_optional_setting, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        with(binding) {
            probAnsStatSelection.listener = this@StudyOptionalUnitSettingFragment
            calculusSelection.listener = this@StudyOptionalUnitSettingFragment
            geometrySelection.listener = this@StudyOptionalUnitSettingFragment

            probAnsStatSelection.buttonTitles =
                listOf(BigUnitV3.경우의_수, BigUnitV3.확률, BigUnitV3.통계).map { it.title }
            calculusSelection.buttonTitles =
                listOf(BigUnitV3.수열의_극한, BigUnitV3.미분법, BigUnitV3.적분법).map { it.title }
            geometrySelection.buttonTitles =
                listOf(BigUnitV3.이차곡선, BigUnitV3.벡터, BigUnitV3.공간도형).map { it.title }


            val userUnits = user!!.optionalUnit
            if (userUnits.isEmpty()) {
                noneSelection.isSelected = true
            } else {
                probAnsStatSelection.result = listOf(
                    false,
                    userUnits.contains(BigUnitV3.경우의_수),
                    userUnits.contains(BigUnitV3.확률),
                    userUnits.contains(BigUnitV3.통계)
                )
                calculusSelection.result = listOf(
                    false,
                    userUnits.contains(BigUnitV3.수열의_극한),
                    userUnits.contains(BigUnitV3.미분법),
                    userUnits.contains(BigUnitV3.적분법)
                )
                geometrySelection.result = listOf(
                    false,
                    userUnits.contains(BigUnitV3.이차곡선),
                    userUnits.contains(BigUnitV3.벡터),
                    userUnits.contains(BigUnitV3.공간도형)
                )
            }

            noneSelection.setOnClickListener { onNoneSelection() }
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun onNoneSelection() {
        with(binding) {
            noneSelection.isSelected = !noneSelection.isSelected
            if (noneSelection.isSelected) {
                probAnsStatSelection.release()
                calculusSelection.release()
                geometrySelection.release()
            } else {
                setModifyBtn()
            }
        }
    }

    private fun getSelectedUnit(): Collection<BigUnitV3> {
        val selectedBigUnits = hashSetOf<BigUnitV3>()
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, SubjectV3.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, SubjectV3.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, SubjectV3.기하))
        }
        return selectedBigUnits
    }

    private fun getSelectedUnits(view: DaebakInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
        if(view.result.first())
            return subject.bigUnits
        else {
            val selectionResult = view.result.subList(1, view.result.size)
            val unitMap = subject.bigUnits.toList().zip(selectionResult)

            return unitMap.filter { it.second }.map { it.first }
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "선택과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit().map { it.id }
            viewModel.updateOptionalSubject(selectedIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackBtnClicked()
                }, 0)
            }
        }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        setModifyBtn()
    }

    private fun setModifyBtn() {
        with(binding) {
            noneSelection.isSelected = getSelectedUnit().isEmpty()

            if(getSelectedUnit().isEmpty() && noneSelection.isSelected == false) {
                modifyBtn.isEnabled = false
            } else {
                modifyBtn.isEnabled = true
            }
        }
    }
}
