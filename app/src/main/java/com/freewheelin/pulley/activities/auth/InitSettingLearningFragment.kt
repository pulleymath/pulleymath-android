package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.databinding.FragmentInitSettingLearningBinding
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener

class InitSettingLearningFragment : Fragment(), DaebakInputSelectionListener {

    var parent:InitSettingActivity? = null
    lateinit var binding: FragmentInitSettingLearningBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is InitSettingActivity) parent = activity as InitSettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_init_setting_learning, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    fun setUI() {
        binding.apply {
            nextBtn.toDisableUI()

            mathTopSelection.buttonTitles = listOf(BigUnitV3.다항식, BigUnitV3.방정식과_부등식, BigUnitV3.도형의_방정식).map { it.title }
            mathBottomSelection.buttonTitles = listOf(BigUnitV3.집합과_명제, BigUnitV3.함수, BigUnitV3.순열과_조합).map { it.title }

            math1Selection.buttonTitles = listOf(BigUnitV3.지수함수와_로그함수, BigUnitV3.삼각함수, BigUnitV3.수열).map { it.title }
            math2Selection.buttonTitles = listOf(BigUnitV3.함수의_극한과_연속, BigUnitV3.미분, BigUnitV3.적분).map { it.title }

            mathTopSelection.listener = this@InitSettingLearningFragment
            mathBottomSelection.listener = this@InitSettingLearningFragment

            math1Selection.listener = this@InitSettingLearningFragment
            math2Selection.listener = this@InitSettingLearningFragment

            prevBtn.setOnClickListener { parent?.prev() }
            nextBtn.setOnClickListener { if(nextBtn.isEnableUI()) parent?.next() }
        }
    }

    fun getSelectedUnit(): Collection<BigUnitV3> {
        binding.apply {
            val selectedBigUnits = hashSetOf<BigUnitV3>()

            selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, SubjectV3.수학_상))
            selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, SubjectV3.수학_하))
            selectedBigUnits.addAll(getSelectedUnits(math1Selection, SubjectV3.수학I))
            selectedBigUnits.addAll(getSelectedUnits(math2Selection, SubjectV3.수학II))

            return selectedBigUnits
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

    override fun onSelectionChanged(view: DaebakInputSelection) {
        binding.apply {
            if(getSelectedUnit().isEmpty()) {
                nextBtn.toDisableUI()
            } else {
                nextBtn.toEnableUI()
            }
        }
    }
}