package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentSubjectExcludeModifyBinding
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.PulleyInputSelection
import com.freewheelin.pulley.views.PulleyInputSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SnackTestSubjectExcludeModifyFragment : Fragment(), PulleyInputSelectionListener {
    private lateinit var binding: FragmentSubjectExcludeModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subject_exclude_modify, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            initSelection()

            val recentUnits = viewModel.getRecentStudyBigUnits(null)
            initRecentUnit(recentUnits)
            initExcludedUnit(recentUnits)

            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }

        }

        arguments?.let {
//            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    private fun initSelection() {
        binding.apply {
            val thisFragment = this@SnackTestSubjectExcludeModifyFragment
            selectionWrapperLl.children.forEach {
                (it as? PulleyInputSelection)?.let {
                    it.listener = thisFragment
                }
            }
        }
    }
    private fun initRecentUnit(recentUnits: List<BigUnitV3>) {
        with(binding) {
            selectionWrapperLl.children.forEach {
                (it as? PulleyInputSelection)?.let {
                    it.setBigUnit(recentUnits)
                }
            }
        }
    }

    private fun initExcludedUnit(includedUnits: List<BigUnitV3>) { // recentUnits
        with(binding) {
            selectionWrapperLl.children.forEach {
                (it as? PulleyInputSelection)?.let {
                    it.excludeBigUnit(includedUnits)
                }
            }
        }
    }

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
    private fun getSelectedUnit(): Collection<BigUnitV3> {
        val selectedBigUnits = hashSetOf<BigUnitV3>()
        if (viewModel.schoolType.value?.isHigh == true) {
            with(binding) {
                selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
                selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
                selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
                selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))
                selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, SubjectV3.확률과통계))
                selectedBigUnits.addAll(getSelectedUnits(calculusSelection, SubjectV3.미적분))
                selectedBigUnits.addAll(getSelectedUnits(geometrySelection, SubjectV3.기하))
            }
        } else {
            with(binding) {
                selectedBigUnits.addAll(getExcludedUnits(m11Selection, SubjectV3.중1_1))
                selectedBigUnits.addAll(getExcludedUnits(m12Selection, SubjectV3.중1_2))
                selectedBigUnits.addAll(getExcludedUnits(m21Selection, SubjectV3.중2_1))
                selectedBigUnits.addAll(getExcludedUnits(m22Selection, SubjectV3.중2_2))
                selectedBigUnits.addAll(getExcludedUnits(m31Selection, SubjectV3.중3_1))
                selectedBigUnits.addAll(getExcludedUnits(m32Selection, SubjectV3.중3_2))
            }
        }
        return selectedBigUnits
    }

    private fun getExcludedUnits(view: PulleyInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
        val unitMap = subject.bigUnits.toList().zip(view.getExcluded())
        return unitMap.filter { it.second }.map { it.first }
    }

    private fun getSelectedUnits(view: PulleyInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
//        val selectionResult = view.result.subList(1, view.result.size)
        val unitMap = subject.bigUnits.toList().zip(view.result)
        return unitMap.filter { it.second }.map { it.first }
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestSubjectExcludeModifyFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }

    override fun onSelectionChanged(view: PulleyInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            binding.modifyBtn.toDisableUI()
        } else {
            binding.modifyBtn.toEnableUI()
        }
    }
    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "단원")
        if(binding.modifyBtn.isEnableUI()) {
            val units = getCalcExcludedUnits()
            println("zxpzxp excluding units : ${units}")
            viewModel.excludeSubjects(units.map { it.id }) {
                onBackBtnClicked()
            }
        }
    }
    private fun getCalcExcludedUnits(): Collection<BigUnitV3> {
        val excludedBigUnits = hashSetOf<BigUnitV3>()
        if (viewModel.schoolType.value?.isHigh == true) {
            with(binding) {
                excludedBigUnits.addAll(getExcludedUnits(mathTopSelection, SubjectV3.수학_상))
                excludedBigUnits.addAll(getExcludedUnits(mathBottomSelection, SubjectV3.수학_하))
                excludedBigUnits.addAll(getExcludedUnits(math1Selection, SubjectV3.수학I))
                excludedBigUnits.addAll(getExcludedUnits(math2Selection, SubjectV3.수학II))
                excludedBigUnits.addAll(getExcludedUnits(probAnsStatSelection, SubjectV3.확률과통계))
                excludedBigUnits.addAll(getExcludedUnits(calculusSelection, SubjectV3.미적분))
                excludedBigUnits.addAll(getExcludedUnits(geometrySelection, SubjectV3.기하))
            }
        } else {
            with(binding) {
                excludedBigUnits.addAll(getExcludedUnits(m11Selection, SubjectV3.중1_1))
                excludedBigUnits.addAll(getExcludedUnits(m12Selection, SubjectV3.중1_2))
                excludedBigUnits.addAll(getExcludedUnits(m21Selection, SubjectV3.중2_1))
                excludedBigUnits.addAll(getExcludedUnits(m22Selection, SubjectV3.중2_2))
                excludedBigUnits.addAll(getExcludedUnits(m31Selection, SubjectV3.중3_1))
                excludedBigUnits.addAll(getExcludedUnits(m32Selection, SubjectV3.중3_2))
            }
        }
        return excludedBigUnits
    }
    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestSubjectExcludeModifyFragment)
    }
}