package com.freewheelin.pulley.activities.mypage

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.DialogMyStudyInfoSettingBinding
import com.freewheelin.pulley.databinding.DialogMyStudyUnitSettingBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener

//deprecated
class MyStudyUnitSettingDialog(context: Context, override val user: User, listener: MyPageSettingDialogListener): MyPageSettingBaseDialog(context, user, listener), DaebakInputSelectionListener {
    var binding: DialogMyStudyUnitSettingBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_my_study_unit_setting, null, false)

    init {
        setContentView(binding.root)
        configureUI()
    }

    private fun configureUI() {
        with(binding) {
            mathTopSelection.buttonTitles = SubjectV3.수학_상.bigUnits.map { it.title }
            mathBottomSelection.buttonTitles = SubjectV3.수학_하.bigUnits.map { it.title }
            math1Selection.buttonTitles = SubjectV3.수학I.bigUnits.map { it.title }
            math2Selection.buttonTitles = SubjectV3.수학II.bigUnits.map { it.title }

            probAnsStatSelection.buttonTitles = SubjectV3.확률과통계.bigUnits.map { it.title }
            calculusSelection.buttonTitles = SubjectV3.미적분.bigUnits.map { it.title }
            geometrySelection.buttonTitles = SubjectV3.기하.bigUnits.map { it.title }

            modifyBtn.toDisableUI()

            val userUnits = user.studiedUnit
            mathTopSelection.result = listOf(
                false,
                userUnits.contains(BigUnitV3.다항식),
                userUnits.contains(BigUnitV3.방정식과_부등식),
                userUnits.contains(BigUnitV3.도형의_방정식)
            )

            mathBottomSelection.result = listOf(
                false,
                userUnits.contains(BigUnitV3.집합과_명제),
                userUnits.contains(BigUnitV3.함수),
                userUnits.contains(BigUnitV3.순열과_조합)
            )

            math1Selection.result = listOf(
                false,
                userUnits.contains(BigUnitV3.지수함수와_로그함수),
                userUnits.contains(BigUnitV3.삼각함수),
                userUnits.contains(BigUnitV3.수열)
            )

            math2Selection.result = listOf(
                false,
                userUnits.contains(BigUnitV3.함수의_극한과_연속),
                userUnits.contains(BigUnitV3.미분),
                userUnits.contains(BigUnitV3.적분)
            )

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

            convertCheckStateIfAllSelected(mathTopSelection)
            convertCheckStateIfAllSelected(mathBottomSelection)
            convertCheckStateIfAllSelected(math1Selection)
            convertCheckStateIfAllSelected(math2Selection)
            convertCheckStateIfAllSelected(probAnsStatSelection)
            convertCheckStateIfAllSelected(calculusSelection)
            convertCheckStateIfAllSelected(geometrySelection)


            setAllSelectionListener()
            modifyBtn.setOnClickListener {

                if(modifyBtn.isEnableUI())
                    onModifyBtnClicked()
            }
            xBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun convertCheckStateIfAllSelected(view: DaebakInputSelection) {
        val result = view.result
        if(result[1] && result[2] && result[3]) {
            view.result = listOf(true, false, false, false)
        }
    }

    private fun setAllSelectionListener() {
        with(binding) {
            mathTopSelection.listener = this@MyStudyUnitSettingDialog

            mathBottomSelection.listener = this@MyStudyUnitSettingDialog

            math1Selection.listener = this@MyStudyUnitSettingDialog

            math2Selection.listener = this@MyStudyUnitSettingDialog

            probAnsStatSelection.listener = this@MyStudyUnitSettingDialog

            calculusSelection.listener = this@MyStudyUnitSettingDialog

            geometrySelection.listener = this@MyStudyUnitSettingDialog
        }
    }

    private fun getSelectedUnit(): Collection<BigUnitV3> {
        with(binding) {
            val selectedBigUnits = hashSetOf<BigUnitV3>()

            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))
            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, SubjectV3.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, SubjectV3.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, SubjectV3.기하))

            return selectedBigUnits
        }
    }

    private fun onModifyBtnClicked() {
        UserManager.setInitStudy(context, user, getSelectedUnit(), successCB = {
            dismiss()
            listener?.onModifyCompleted(user)
        })
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        with(binding) {
            if(getSelectedUnit().isEmpty()) {
                modifyBtn.toDisableUI()
            } else {
                modifyBtn.toEnableUI()
            }
        }
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
}