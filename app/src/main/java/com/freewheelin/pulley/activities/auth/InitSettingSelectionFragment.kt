package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.databinding.FragmentInitSettingSelectionBinding
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener

class InitSettingSelectionFragment : Fragment(), DaebakInputSelectionListener {

    var parent:InitSettingActivity? = null
    lateinit var binding: FragmentInitSettingSelectionBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is InitSettingActivity) parent = activity as InitSettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_init_setting_selection, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    private fun setUI() {
        binding.apply {
            completeBtn.toDisableUI()

            probAnsStatSelection.buttonTitles = listOf(BigUnit.경우의_수, BigUnit.확률, BigUnit.통계).map { it.title }
            calculusSelection.buttonTitles = listOf(BigUnit.수열의_극한, BigUnit.미분법, BigUnit.적분법).map { it.title }
            geometrySelection.buttonTitles = listOf(BigUnit.이차곡선, BigUnit.벡터, BigUnit.공간도형).map { it.title }

            noneSelection.setOnClickListener { onNoneSelection() }
            probAnsStatSelection.listener = this@InitSettingSelectionFragment
            calculusSelection.listener = this@InitSettingSelectionFragment
            geometrySelection.listener = this@InitSettingSelectionFragment

            prevBtn.setOnClickListener { parent?.prev() }
            completeBtn.setOnClickListener { if(completeBtn.isEnableUI()) parent?.complete() }
        }
    }

    private fun onNoneSelection() {
        binding.apply {
            noneSelection.isSelected = !noneSelection.isSelected
            if(noneSelection.isSelected) {
                probAnsStatSelection.release()
                calculusSelection.release()
                geometrySelection.release()
            }

            setComplete()
        }
    }

    fun getSelectedUnit(): Collection<BigUnit> {
        binding.apply {
            val selectedBigUnits = hashSetOf<BigUnit>()

            selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, Subject.확률과통계))
            selectedBigUnits.addAll(getSelectedUnits(calculusSelection, Subject.미적분))
            selectedBigUnits.addAll(getSelectedUnits(geometrySelection, Subject.기하))

            return selectedBigUnits
        }
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
        if(getSelectedUnit().isNotEmpty()){
            binding.noneSelection.isSelected = false
        }
        setComplete()
    }

    private fun setComplete() {
        binding.apply {
            if(getSelectedUnit().isEmpty() && !noneSelection.isSelected) {
                completeBtn.toDisableUI()
            } else {
                completeBtn.toEnableUI()
            }
        }
    }
}