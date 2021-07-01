package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener
import kotlinx.android.synthetic.main.fragment_init_setting_selection.*
import kotlinx.android.synthetic.main.fragment_study_unit_optional_setting.*
import kotlinx.android.synthetic.main.fragment_study_unit_optional_setting.noneSelection
import kotlinx.android.synthetic.main.fragment_study_unit_setting.*
import kotlinx.android.synthetic.main.fragment_study_unit_setting.calculusSelection
import kotlinx.android.synthetic.main.fragment_study_unit_setting.geometrySelection
import kotlinx.android.synthetic.main.fragment_study_unit_setting.modifyBtn
import kotlinx.android.synthetic.main.fragment_study_unit_setting.probAnsStatSelection


class StudyOptionalUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study_unit_optional_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        probAnsStatSelection.listener = this
        calculusSelection.listener = this
        geometrySelection.listener = this

        probAnsStatSelection.buttonTitles = listOf(BigUnit.경우의_수, BigUnit.확률, BigUnit.통계).map { it.title }
        calculusSelection.buttonTitles = listOf(BigUnit.수열의_극한, BigUnit.미분법, BigUnit.적분법).map { it.title }
        geometrySelection.buttonTitles = listOf(BigUnit.이차곡선, BigUnit.벡터, BigUnit.공간도형).map { it.title }


        val userUnits = user!!.optionalUnit
        if(userUnits.isEmpty()) {
            noneSelection.isSelected = true
        } else {
            probAnsStatSelection.result = listOf(false, userUnits.contains(BigUnit.경우의_수), userUnits.contains(BigUnit.확률), userUnits.contains(BigUnit.통계))
            calculusSelection.result = listOf(false, userUnits.contains(BigUnit.수열의_극한), userUnits.contains(BigUnit.미분법), userUnits.contains(BigUnit.적분법))
            geometrySelection.result = listOf(false, userUnits.contains(BigUnit.이차곡선), userUnits.contains(BigUnit.벡터), userUnits.contains(BigUnit.공간도형))
        }

        noneSelection.setOnClickListener { onNoneSelection() }
        modifyBtn.setOnClickListener { onModifyBtnClicked() }
    }

    private fun onNoneSelection() {
        noneSelection.isSelected = !noneSelection.isSelected
        if(noneSelection.isSelected) {
            probAnsStatSelection.release()
            calculusSelection.release()
            geometrySelection.release()
        } else {
            setModifyBtn()
        }
    }

    private fun getSelectedUnit(): Collection<BigUnit> {
        val selectedBigUnits = hashSetOf<BigUnit>()

        selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, Subject.확률과통계))
        selectedBigUnits.addAll(getSelectedUnits(calculusSelection, Subject.미적분))
        selectedBigUnits.addAll(getSelectedUnits(geometrySelection, Subject.기하))

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
        MyApplication.user?.let { user ->
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "선택과목", "변경하기")
            if (modifyBtn.isEnableUI()) {
                UserManager.setInitOptional(requireContext(), user, getSelectedUnit(), successCB = {
                    val selected = getSelectedUnit().map { it.id }
                    user.rawInitOptional = selected.joinToString(",")
                    user.commit("update mypage Update [OPTIONAL] subject")
                    setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                    Handler(Looper.getMainLooper()).postDelayed({
                        onBackBtnClicked()
                    }, 0)
                })
            }
        }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        setModifyBtn()
    }

    private fun setModifyBtn() {
        noneSelection.isSelected = getSelectedUnit().isEmpty()

        if(getSelectedUnit().isEmpty() && noneSelection.isSelected == false) {
            modifyBtn.toDisableUI()
        } else {
            modifyBtn.toEnableUI()
        }
    }
}
