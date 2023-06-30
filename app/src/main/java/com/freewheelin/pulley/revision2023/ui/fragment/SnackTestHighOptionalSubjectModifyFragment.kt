package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.BigUnitV3
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentHighOptionalSubjectModifyBinding
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener

class SnackTestHighOptionalSubjectModifyFragment : Fragment(), DaebakInputSelectionListener {
    private lateinit var binding: FragmentHighOptionalSubjectModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_high_optional_subject_modify, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()


            initSelection()
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }

        arguments?.let {
//            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    private fun initSelection() {
        binding.apply {
            val thisFragment = this@SnackTestHighOptionalSubjectModifyFragment
            probAnsStatSelection.listener = thisFragment
            calculusSelection.listener = thisFragment
            geometrySelection.listener = thisFragment

            probAnsStatSelection.buttonTitles =
                listOf(BigUnitV3.경우의_수, BigUnitV3.확률, BigUnitV3.통계).map { it.title }
            calculusSelection.buttonTitles =
                listOf(BigUnitV3.수열의_극한, BigUnitV3.미분법, BigUnitV3.적분법).map { it.title }
            geometrySelection.buttonTitles =
                listOf(BigUnitV3.이차곡선, BigUnitV3.벡터, BigUnitV3.공간도형).map { it.title }

            val selectedUnits = viewModel.getUserSelectedOptionalBigUnits(null)
            setOptionalUnit(selectedUnits)
            noneSelection.setOnClickListener { onNoneSelection() }

        }

    }
    private fun setOptionalUnit(selectedUnits: List<BigUnitV3>) {
        binding.apply {
            if (selectedUnits.isEmpty()) {
                noneSelection.isSelected = true
            } else {
                probAnsStatSelection.result = listOf(
                    false,
                    selectedUnits.contains(BigUnitV3.경우의_수),
                    selectedUnits.contains(BigUnitV3.확률),
                    selectedUnits.contains(BigUnitV3.통계)
                )
                calculusSelection.result = listOf(
                    false,
                    selectedUnits.contains(BigUnitV3.수열의_극한),
                    selectedUnits.contains(BigUnitV3.미분법),
                    selectedUnits.contains(BigUnitV3.적분법)
                )
                geometrySelection.result = listOf(
                    false,
                    selectedUnits.contains(BigUnitV3.이차곡선),
                    selectedUnits.contains(BigUnitV3.벡터),
                    selectedUnits.contains(BigUnitV3.공간도형)
                )
            }

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
    private fun setModifyBtn() {
        with(binding) {
            noneSelection.isSelected = getSelectedUnit().isEmpty()

            if(getSelectedUnit().isEmpty() && !noneSelection.isSelected) {
                modifyBtn.isEnabled = false
            } else {
                modifyBtn.isEnabled = true
            }
        }
    }
    private fun getSelectedUnit(): Collection<BigUnitV3> {
        val selectedBigUnits = hashSetOf<BigUnitV3>()
        val getSelectedUnits = viewModel.getSelectedUnits
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, SubjectV3.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, SubjectV3.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, SubjectV3.기하))
        }
        return selectedBigUnits
    }


    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
    companion object {
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestHighOptionalSubjectModifyFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "선택과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit().map { it.id }
            viewModel.updateOptionalSubject(selectedIds) {
                onBackBtnClicked()
            }
        }
    }
    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestHighOptionalSubjectModifyFragment)
    }
    override fun onSelectionChanged(view: DaebakInputSelection) {
        with(binding) {
            noneSelection.isSelected = getSelectedUnit().isEmpty()

            if(getSelectedUnit().isEmpty() && !noneSelection.isSelected) {
                modifyBtn.isEnabled = false
            } else {
                modifyBtn.isEnabled = true
            }
        }
    }
}