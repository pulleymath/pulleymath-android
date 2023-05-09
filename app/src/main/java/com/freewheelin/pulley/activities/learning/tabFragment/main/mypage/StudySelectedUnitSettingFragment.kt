package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentStudyUnitSelectedSettingBinding
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.PulleyInputSelection
import com.freewheelin.pulley.views.PulleyInputSelectionListener


class StudySelectedUnitSettingFragment : MyPageBaseFragment(), PulleyInputSelectionListener {
    lateinit var binding: FragmentStudyUnitSelectedSettingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_selected_setting, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        setButtonsAndListeners()
        setRecentUnit()
        setExcludedUnit()
        binding.modifyBtn.setOnClickListener { onModifyBtnClicked() }
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }

    fun setButtonsAndListeners(){
        with(binding) {
            mathTopSelection.listener = this@StudySelectedUnitSettingFragment
            mathBottomSelection.listener = this@StudySelectedUnitSettingFragment
            math1Selection.listener = this@StudySelectedUnitSettingFragment
            math2Selection.listener = this@StudySelectedUnitSettingFragment
            probAnsStatSelection.listener = this@StudySelectedUnitSettingFragment
            calculusSelection.listener = this@StudySelectedUnitSettingFragment
            geometrySelection.listener = this@StudySelectedUnitSettingFragment

            mathTopSelection.buttonTitles =
                listOf(BigUnitV3.다항식, BigUnitV3.방정식과_부등식, BigUnitV3.도형의_방정식).map { it.title }
            mathBottomSelection.buttonTitles =
                listOf(BigUnitV3.집합과_명제, BigUnitV3.함수, BigUnitV3.순열과_조합).map { it.title }
            math1Selection.buttonTitles =
                listOf(BigUnitV3.지수함수와_로그함수, BigUnitV3.삼각함수, BigUnitV3.수열).map { it.title }
            math2Selection.buttonTitles =
                listOf(BigUnitV3.함수의_극한과_연속, BigUnitV3.미분, BigUnitV3.적분).map { it.title }

            probAnsStatSelection.buttonTitles =
                listOf(BigUnitV3.경우의_수, BigUnitV3.확률, BigUnitV3.통계).map { it.title }
            calculusSelection.buttonTitles =
                listOf(BigUnitV3.수열의_극한, BigUnitV3.미분법, BigUnitV3.적분법).map { it.title }
            geometrySelection.buttonTitles =
                listOf(BigUnitV3.이차곡선, BigUnitV3.벡터, BigUnitV3.공간도형).map { it.title }
        }
    }

    private fun setRecentUnit() {
        // TODO
//        with(binding) {
//            val userUnits = user!!.recentUnit
//            mathTopSelection.set( listOf(userUnits.contains(BigUnitV3.다항식), userUnits.contains(BigUnitV3.방정식과_부등식), userUnits.contains(BigUnitV3.도형의_방정식)))
//            mathBottomSelection.set( listOf(userUnits.contains(BigUnitV3.집합과_명제), userUnits.contains(BigUnitV3.함수), userUnits.contains(BigUnitV3.순열과_조합)))
//            math1Selection.set( listOf(userUnits.contains(BigUnitV3.지수함수와_로그함수), userUnits.contains(BigUnitV3.삼각함수), userUnits.contains(BigUnitV3.수열)))
//            math2Selection.set( listOf(userUnits.contains(BigUnitV3.함수의_극한과_연속), userUnits.contains(BigUnitV3.미분), userUnits.contains(BigUnitV3.적분)))
//            probAnsStatSelection.set( listOf(userUnits.contains(BigUnitV3.경우의_수), userUnits.contains(BigUnitV3.확률), userUnits.contains(BigUnitV3.통계)))
//            calculusSelection.set( listOf(userUnits.contains(BigUnitV3.수열의_극한), userUnits.contains(BigUnitV3.미분법), userUnits.contains(BigUnitV3.적분법)))
//            geometrySelection.set( listOf(userUnits.contains(BigUnitV3.이차곡선), userUnits.contains(BigUnitV3.벡터), userUnits.contains(BigUnitV3.공간도형)))
//        }
    }

    private fun setExcludedUnit() {
        // TODO
//        with(binding) {
//            val excluded = user!!.recentExcludedUnit
//            mathTopSelection.exclude( listOf(excluded.contains(BigUnitV3.다항식), excluded.contains(BigUnitV3.방정식과_부등식), excluded.contains(BigUnitV3.도형의_방정식)))
//            mathBottomSelection.exclude( listOf(excluded.contains(BigUnitV3.집합과_명제), excluded.contains(BigUnitV3.함수), excluded.contains(BigUnitV3.순열과_조합)))
//            math1Selection.exclude( listOf(excluded.contains(BigUnitV3.지수함수와_로그함수), excluded.contains(BigUnitV3.삼각함수), excluded.contains(BigUnitV3.수열)))
//            math2Selection.exclude( listOf(excluded.contains(BigUnitV3.함수의_극한과_연속), excluded.contains(BigUnitV3.미분), excluded.contains(BigUnitV3.적분)))
//            probAnsStatSelection.exclude( listOf(excluded.contains(BigUnitV3.경우의_수), excluded.contains(BigUnitV3.확률), excluded.contains(BigUnitV3.통계)))
//            calculusSelection.exclude( listOf(excluded.contains(BigUnitV3.수열의_극한), excluded.contains(BigUnitV3.미분법), excluded.contains(BigUnitV3.적분법)))
//            geometrySelection.exclude( listOf(excluded.contains(BigUnitV3.이차곡선), excluded.contains(BigUnitV3.벡터), excluded.contains(BigUnitV3.공간도형)))
//        }
    }

    private fun getSelectedUnits(view: PulleyInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
//        val selectionResult = view.result.subList(1, view.result.size)
        val unitMap = subject.bigUnits.toList().zip(view.result)
        return unitMap.filter { it.second }.map { it.first }
    }

    private fun getSelectedUnit(): Collection<BigUnitV3> {
        val selectedBigUnits = hashSetOf<BigUnitV3>()
        with(binding) {
            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))
            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, SubjectV3.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, SubjectV3.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, SubjectV3.기하))
        }
        return selectedBigUnits
    }

    private fun getExcludedUnits(view: PulleyInputSelection, subject: SubjectV3): Collection<BigUnitV3> {
        val unitMap = subject.bigUnits.toList().zip(view.getExcluded())
        return unitMap.filter { it.second }.map { it.first }
    }

    private fun getCalcExcludedUnits(): Collection<BigUnitV3> {
        val excludedBigUnits = hashSetOf<BigUnitV3>()
        with(binding) {
            excludedBigUnits.addAll(getExcludedUnits(mathTopSelection, SubjectV3.수학_상))
            excludedBigUnits.addAll(getExcludedUnits(mathBottomSelection, SubjectV3.수학_하))
            excludedBigUnits.addAll(getExcludedUnits(math1Selection, SubjectV3.수학I))
            excludedBigUnits.addAll(getExcludedUnits(math2Selection, SubjectV3.수학II))
            excludedBigUnits.addAll(getExcludedUnits(probAnsStatSelection, SubjectV3.확률과통계))
            excludedBigUnits.addAll(getExcludedUnits(calculusSelection, SubjectV3.미적분))
            excludedBigUnits.addAll(getExcludedUnits(geometrySelection, SubjectV3.기하))
        }
        return excludedBigUnits
    }

    val viewModel: MyMainPageFragViewModel by viewModels()

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "단원")
        if(binding.modifyBtn.isEnableUI()) {
            val units = getCalcExcludedUnits()
            viewModel.excludeSubjects(units.map { it.id }) {
                listener?.onModifyCompleted()
//                user?.setExcludeUnit(units) // TODO 어딘가 저장해야하나?
                onBackBtnClicked()
            }
//            UserManager.setRecentExclude(requireContext(), user!!, getCalcExcludedUnits(), successCB = {
//                listener?.onModifyCompleted()
//                Handler(Looper.getMainLooper()).postDelayed({
//                    onBackBtnClicked()
//                }, 0)
//            })
        }
    }

    override fun onSelectionChanged(view: PulleyInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            binding.modifyBtn.toDisableUI()
        } else {
            binding.modifyBtn.toEnableUI()
        }
    }
}
