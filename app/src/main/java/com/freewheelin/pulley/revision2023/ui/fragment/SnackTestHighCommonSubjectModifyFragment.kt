package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.BigUnitV3
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentHighCommonSubjectModifyBinding
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener

class SnackTestHighCommonSubjectModifyFragment : Fragment(), DaebakInputSelectionListener {
    private lateinit var binding: FragmentHighCommonSubjectModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_high_common_subject_modify, container, false)
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
            val thisFragment = this@SnackTestHighCommonSubjectModifyFragment
            mathTopSelection.listener = thisFragment
            mathBottomSelection.listener = thisFragment
            math1Selection.listener = thisFragment
            math2Selection.listener = thisFragment

            mathTopSelection.buttonTitles =
                listOf(BigUnitV3.다항식, BigUnitV3.방정식과_부등식, BigUnitV3.도형의_방정식).map { it.title }
            mathBottomSelection.buttonTitles =
                listOf(BigUnitV3.집합과_명제, BigUnitV3.함수, BigUnitV3.순열과_조합).map { it.title }
            math1Selection.buttonTitles =
                listOf(BigUnitV3.지수함수와_로그함수, BigUnitV3.삼각함수, BigUnitV3.수열).map { it.title }
            math2Selection.buttonTitles =
                listOf(BigUnitV3.함수의_극한과_연속, BigUnitV3.미분, BigUnitV3.적분).map { it.title }

            val selectedUnits = viewModel.getUserSelectedCommonBigUnits(null)
            setCommonUnit(selectedUnits)

        }
    }

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestHighCommonSubjectModifyFragment)
    }

    private fun setCommonUnit(selectedUnits: List<BigUnitV3>) {
        with(binding) {
            mathTopSelection.result = listOf(
                false,
                selectedUnits.contains(BigUnitV3.다항식),
                selectedUnits.contains(BigUnitV3.방정식과_부등식),
                selectedUnits.contains(BigUnitV3.도형의_방정식)
            )

            mathBottomSelection.result = listOf(
                false,
                selectedUnits.contains(BigUnitV3.집합과_명제),
                selectedUnits.contains(BigUnitV3.함수),
                selectedUnits.contains(BigUnitV3.순열과_조합)
            )

            math1Selection.result = listOf(
                false,
                selectedUnits.contains(BigUnitV3.지수함수와_로그함수),
                selectedUnits.contains(BigUnitV3.삼각함수),
                selectedUnits.contains(BigUnitV3.수열)
            )

            math2Selection.result = listOf(
                false,
                selectedUnits.contains(BigUnitV3.함수의_극한과_연속),
                selectedUnits.contains(BigUnitV3.미분),
                selectedUnits.contains(BigUnitV3.적분)
            )
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "공통과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit().map { it.id }
            viewModel.updateCommonSubject(selectedIds) {
                onBackBtnClicked()
            }
        }

    }
    private fun getSelectedUnit(): Collection<BigUnitV3> {
        val selectedBigUnits = hashSetOf<BigUnitV3>()
        val getSelectedUnits = viewModel.getSelectedUnits
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))
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
            SnackTestHighCommonSubjectModifyFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            binding.modifyBtn.isEnabled = false
        } else {
            binding.modifyBtn.isEnabled = true
        }
    }
}