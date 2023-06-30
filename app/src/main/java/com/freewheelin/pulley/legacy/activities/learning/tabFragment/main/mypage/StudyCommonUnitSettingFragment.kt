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
import com.freewheelin.pulley.databinding.FragmentStudyUnitCommonSettingBinding
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener

class StudyCommonUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {
    lateinit var binding: FragmentStudyUnitCommonSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_common_setting, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        with(binding) {
            mathTopSelection.listener = this@StudyCommonUnitSettingFragment
            mathBottomSelection.listener = this@StudyCommonUnitSettingFragment
            math1Selection.listener = this@StudyCommonUnitSettingFragment
            math2Selection.listener = this@StudyCommonUnitSettingFragment

            mathTopSelection.buttonTitles =
                listOf(BigUnitV3.다항식, BigUnitV3.방정식과_부등식, BigUnitV3.도형의_방정식).map { it.title }
            mathBottomSelection.buttonTitles =
                listOf(BigUnitV3.집합과_명제, BigUnitV3.함수, BigUnitV3.순열과_조합).map { it.title }
            math1Selection.buttonTitles =
                listOf(BigUnitV3.지수함수와_로그함수, BigUnitV3.삼각함수, BigUnitV3.수열).map { it.title }
            math2Selection.buttonTitles =
                listOf(BigUnitV3.함수의_극한과_연속, BigUnitV3.미분, BigUnitV3.적분).map { it.title }

            setCommonUnit()

            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun setCommonUnit() {
        with(binding) {
            val studiedUnit = user!!.studiedUnit

            mathTopSelection.result = listOf(
                false,
                studiedUnit.contains(BigUnitV3.다항식),
                studiedUnit.contains(BigUnitV3.방정식과_부등식),
                studiedUnit.contains(BigUnitV3.도형의_방정식)
            )

            mathBottomSelection.result = listOf(
                false,
                studiedUnit.contains(BigUnitV3.집합과_명제),
                studiedUnit.contains(BigUnitV3.함수),
                studiedUnit.contains(BigUnitV3.순열과_조합)
            )

            math1Selection.result = listOf(
                false,
                studiedUnit.contains(BigUnitV3.지수함수와_로그함수),
                studiedUnit.contains(BigUnitV3.삼각함수),
                studiedUnit.contains(BigUnitV3.수열)
            )

            math2Selection.result = listOf(
                false,
                studiedUnit.contains(BigUnitV3.함수의_극한과_연속),
                studiedUnit.contains(BigUnitV3.미분),
                studiedUnit.contains(BigUnitV3.적분)
            )
        }
    }

    private fun getSelectedUnit(): Collection<BigUnitV3> {

        val selectedBigUnits = hashSetOf<BigUnitV3>()
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))
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
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "공통과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit().map { it.id }
            viewModel.updateCommonSubject(selectedIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackBtnClicked()
                }, 0)
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
