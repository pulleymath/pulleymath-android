package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentStudyUnitSettingBinding
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener


class StudyAllUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {

    lateinit var binding: FragmentStudyUnitSettingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_setting, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        with(binding) {
            mathTopSelection.listener = this@StudyAllUnitSettingFragment
            mathBottomSelection.listener = this@StudyAllUnitSettingFragment
            math1Selection.listener = this@StudyAllUnitSettingFragment
            math2Selection.listener = this@StudyAllUnitSettingFragment
            probAnsStatSelection.listener = this@StudyAllUnitSettingFragment
            calculusSelection.listener = this@StudyAllUnitSettingFragment
            geometrySelection.listener = this@StudyAllUnitSettingFragment

            mathTopSelection.buttonTitles =
                listOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식).map { it.title }
            mathBottomSelection.buttonTitles =
                listOf(BigUnit.집합과_명제, BigUnit.함수, BigUnit.순열과_조합).map { it.title }
            math1Selection.buttonTitles =
                listOf(BigUnit.지수함수와_로그함수, BigUnit.삼각함수, BigUnit.수열).map { it.title }
            math2Selection.buttonTitles =
                listOf(BigUnit.함수의_극한과_연속, BigUnit.미분, BigUnit.적분).map { it.title }

            probAnsStatSelection.buttonTitles =
                listOf(BigUnit.경우의_수, BigUnit.확률, BigUnit.통계).map { it.title }
            calculusSelection.buttonTitles =
                listOf(BigUnit.수열의_극한, BigUnit.미분법, BigUnit.적분법).map { it.title }
            geometrySelection.buttonTitles =
                listOf(BigUnit.이차곡선, BigUnit.벡터, BigUnit.공간도형).map { it.title }


            val userUnits = user!!.studiedUnit
            mathTopSelection.result = listOf(
                false,
                userUnits.contains(BigUnit.다항식),
                userUnits.contains(BigUnit.방정식과_부등식),
                userUnits.contains(BigUnit.도형의_방정식)
            )

            mathBottomSelection.result = listOf(
                false,
                userUnits.contains(BigUnit.집합과_명제),
                userUnits.contains(BigUnit.함수),
                userUnits.contains(BigUnit.순열과_조합)
            )

            math1Selection.result = listOf(
                false,
                userUnits.contains(BigUnit.지수함수와_로그함수),
                userUnits.contains(BigUnit.삼각함수),
                userUnits.contains(BigUnit.수열)
            )

            math2Selection.result = listOf(
                false,
                userUnits.contains(BigUnit.함수의_극한과_연속),
                userUnits.contains(BigUnit.미분),
                userUnits.contains(BigUnit.적분)
            )

            probAnsStatSelection.result = listOf(
                false,
                userUnits.contains(BigUnit.경우의_수),
                userUnits.contains(BigUnit.확률),
                userUnits.contains(BigUnit.통계)
            )

            calculusSelection.result = listOf(
                false,
                userUnits.contains(BigUnit.수열의_극한),
                userUnits.contains(BigUnit.미분법),
                userUnits.contains(BigUnit.적분법)
            )


            geometrySelection.result = listOf(
                false,
                userUnits.contains(BigUnit.이차곡선),
                userUnits.contains(BigUnit.벡터),
                userUnits.contains(BigUnit.공간도형)
            )

            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun getSelectedUnit(): Collection<BigUnit> {
        val selectedBigUnits = hashSetOf<BigUnit>()
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, Subject.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, Subject.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, Subject.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, Subject.수학II))
            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, Subject.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, Subject.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, Subject.기하))
        }
        return selectedBigUnits
    }

    private fun getSelectedUnits(view: DaebakInputSelection, subject: Subject): Collection<BigUnit> {
        if(view.result.first())
            return subject.bigUnits
        else {
            val selectionResult = view.result.subList(1, view.result.size)
            val unitMap = subject.bigUnits.toList().zip(selectionResult)

            return unitMap.filter { it.second }.map { it.first }
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "단원")
        if(binding.modifyBtn.isEnableUI()) {
            UserManager.setInitStudy(requireContext(), user!!, getSelectedUnit(), successCB = {
                CompleteDialog(requireContext(), "수정 완료!\n업데이트되었습니다.", "해당 수정 내역은 추천 문항에 반영됩니다.").showFor()
                listener?.onModifyCompleted()
            })
        }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            binding.modifyBtn.toDisableUI()
        } else {
            binding.modifyBtn.toEnableUI()
        }
    }
}
