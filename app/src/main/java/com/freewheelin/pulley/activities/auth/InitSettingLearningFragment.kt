package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener
import kotlinx.android.synthetic.main.fragment_init_setting_learning.*
import kotlinx.android.synthetic.main.fragment_init_setting_learning.nextBtn

class InitSettingLearningFragment : Fragment(), DaebakInputSelectionListener {

    var parent:InitSettingActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is InitSettingActivity) parent = activity as InitSettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_init_setting_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    fun setUI() {
        nextBtn.toDisableUI()

        mathTopSelection.buttonTitles = listOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식).map { it.title }
        mathBottomSelection.buttonTitles = listOf(BigUnit.집합과_명제, BigUnit.함수, BigUnit.순열과_조합).map { it.title }

        math1Selection.buttonTitles = listOf(BigUnit.지수함수와_로그함수, BigUnit.삼각함수, BigUnit.수열).map { it.title }
        math2Selection.buttonTitles = listOf(BigUnit.함수의_극한과_연속, BigUnit.미분, BigUnit.적분).map { it.title }

        mathTopSelection.listener = this
        mathBottomSelection.listener = this

        math1Selection.listener = this
        math2Selection.listener = this

        prevBtn.setOnClickListener { parent?.prev() }
        nextBtn.setOnClickListener { if(nextBtn.isEnableUI()) parent?.next() }
    }

    fun getSelectedUnit(): Collection<BigUnit> {
        val selectedBigUnits = hashSetOf<BigUnit>()

        selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, Subject.수학_상))
        selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, Subject.수학_하))
        selectedBigUnits.addAll(getSelectedUnits(math1Selection, Subject.수학I))
        selectedBigUnits.addAll(getSelectedUnits(math2Selection, Subject.수학II))

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

    override fun onSelectionChanged(view: DaebakInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            nextBtn.toDisableUI()
        } else {
            nextBtn.toEnableUI()
        }
    }
}