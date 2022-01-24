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
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.Buttons.PrimaryButton
import com.freewheelin.pulley.views.PulleyInputSelection
import com.freewheelin.pulley.views.PulleyInputSelectionListener


class StudySelectedUnitSettingFragment : MyPageBaseFragment(), PulleyInputSelectionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study_unit_selected_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI(view)
    }

    lateinit var mathTopSelection: PulleyInputSelection
    lateinit var mathBottomSelection: PulleyInputSelection
    lateinit var math1Selection: PulleyInputSelection
    lateinit var math2Selection: PulleyInputSelection
    lateinit var probAnsStatSelection: PulleyInputSelection
    lateinit var calculusSelection: PulleyInputSelection
    lateinit var geometrySelection: PulleyInputSelection
    lateinit var modifyBtn: PrimaryButton
    lateinit var guideTopLabel: TextView
    lateinit var titleLabel: TextView
    lateinit var backBtn: ImageButton

    fun setUpUI(view: View) {
        boilerUI(view)
        setButtonsAndListeners()
        setRecentUnit()
        setExcludedUnit()
        modifyBtn.setOnClickListener { onModifyBtnClicked() }
    }

    fun boilerUI(v: View) {
        mathTopSelection = v.findViewById(R.id.mathTopSelection)
        mathBottomSelection = v.findViewById(R.id.mathBottomSelection)
        math1Selection = v.findViewById(R.id.math1Selection)
        math2Selection = v.findViewById(R.id.math2Selection)
        probAnsStatSelection = v.findViewById(R.id.probAnsStatSelection)
        calculusSelection = v.findViewById(R.id.calculusSelection)
        geometrySelection = v.findViewById(R.id.geometrySelection)
        modifyBtn = v.findViewById(R.id.modifyBtn)
        guideTopLabel = v.findViewById(R.id.guideTopLabel)
        titleLabel = v.findViewById(R.id.titleLabel)
        backBtn = v.findViewById(R.id.backBtn)
    }
    fun setButtonsAndListeners(){
        mathTopSelection.listener = this
        mathBottomSelection.listener = this
        math1Selection.listener = this
        math2Selection.listener = this
        probAnsStatSelection.listener = this
        calculusSelection.listener = this
        geometrySelection.listener = this

        mathTopSelection.buttonTitles = listOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식).map { it.title }
        mathBottomSelection.buttonTitles = listOf(BigUnit.집합과_명제, BigUnit.함수, BigUnit.순열과_조합).map { it.title }
        math1Selection.buttonTitles = listOf(BigUnit.지수함수와_로그함수, BigUnit.삼각함수, BigUnit.수열).map { it.title }
        math2Selection.buttonTitles = listOf(BigUnit.함수의_극한과_연속, BigUnit.미분, BigUnit.적분).map { it.title }

        probAnsStatSelection.buttonTitles = listOf(BigUnit.경우의_수, BigUnit.확률, BigUnit.통계).map { it.title }
        calculusSelection.buttonTitles = listOf(BigUnit.수열의_극한, BigUnit.미분법, BigUnit.적분법).map { it.title }
        geometrySelection.buttonTitles = listOf(BigUnit.이차곡선, BigUnit.벡터, BigUnit.공간도형).map { it.title }
    }

    private fun setRecentUnit() {
        val userUnits = user!!.recentUnit
        mathTopSelection.set( listOf(userUnits.contains(BigUnit.다항식), userUnits.contains(BigUnit.방정식과_부등식), userUnits.contains(BigUnit.도형의_방정식)))
        mathBottomSelection.set( listOf(userUnits.contains(BigUnit.집합과_명제), userUnits.contains(BigUnit.함수), userUnits.contains(BigUnit.순열과_조합)))
        math1Selection.set( listOf(userUnits.contains(BigUnit.지수함수와_로그함수), userUnits.contains(BigUnit.삼각함수), userUnits.contains(BigUnit.수열)))
        math2Selection.set( listOf(userUnits.contains(BigUnit.함수의_극한과_연속), userUnits.contains(BigUnit.미분), userUnits.contains(BigUnit.적분)))
        probAnsStatSelection.set( listOf(userUnits.contains(BigUnit.경우의_수), userUnits.contains(BigUnit.확률), userUnits.contains(BigUnit.통계)))
        calculusSelection.set( listOf(userUnits.contains(BigUnit.수열의_극한), userUnits.contains(BigUnit.미분법), userUnits.contains(BigUnit.적분법)))
        geometrySelection.set( listOf(userUnits.contains(BigUnit.이차곡선), userUnits.contains(BigUnit.벡터), userUnits.contains(BigUnit.공간도형)))
    }

    private fun setExcludedUnit() {
        val excluded = user!!.recentExcludedUnit
        mathTopSelection.exclude( listOf(excluded.contains(BigUnit.다항식), excluded.contains(BigUnit.방정식과_부등식), excluded.contains(BigUnit.도형의_방정식)))
        mathBottomSelection.exclude( listOf(excluded.contains(BigUnit.집합과_명제), excluded.contains(BigUnit.함수), excluded.contains(BigUnit.순열과_조합)))
        math1Selection.exclude( listOf(excluded.contains(BigUnit.지수함수와_로그함수), excluded.contains(BigUnit.삼각함수), excluded.contains(BigUnit.수열)))
        math2Selection.exclude( listOf(excluded.contains(BigUnit.함수의_극한과_연속), excluded.contains(BigUnit.미분), excluded.contains(BigUnit.적분)))
        probAnsStatSelection.exclude( listOf(excluded.contains(BigUnit.경우의_수), excluded.contains(BigUnit.확률), excluded.contains(BigUnit.통계)))
        calculusSelection.exclude( listOf(excluded.contains(BigUnit.수열의_극한), excluded.contains(BigUnit.미분법), excluded.contains(BigUnit.적분법)))
        geometrySelection.exclude( listOf(excluded.contains(BigUnit.이차곡선), excluded.contains(BigUnit.벡터), excluded.contains(BigUnit.공간도형)))
    }

    private fun getSelectedUnits(view: PulleyInputSelection, subject: Subject): Collection<BigUnit> {
//        val selectionResult = view.result.subList(1, view.result.size)
        val unitMap = subject.bigUnits.toList().zip(view.result)
        return unitMap.filter { it.second }.map { it.first }
    }

    private fun getSelectedUnit(): Collection<BigUnit> {
        val selectedBigUnits = hashSetOf<BigUnit>()

        selectedBigUnits.addAll(getSelectedUnits(mathTopSelection, Subject.수학_상))
        selectedBigUnits.addAll(getSelectedUnits(mathBottomSelection, Subject.수학_하))
        selectedBigUnits.addAll(getSelectedUnits(math1Selection, Subject.수학I))
        selectedBigUnits.addAll(getSelectedUnits(math2Selection, Subject.수학II))
        selectedBigUnits.addAll(getSelectedUnits(probAnsStatSelection, Subject.확률과통계))
        selectedBigUnits.addAll(getSelectedUnits(calculusSelection, Subject.미적분))
        selectedBigUnits.addAll(getSelectedUnits(geometrySelection, Subject.기하))

        return selectedBigUnits
    }

    private fun getExcludedUnits(view: PulleyInputSelection, subject: Subject): Collection<BigUnit> {
        val unitMap = subject.bigUnits.toList().zip(view.getExcluded())
        return unitMap.filter { it.second }.map { it.first }
    }

    private fun getCalcExcludedUnits(): Collection<BigUnit> {
        val excludedBigUnits = hashSetOf<BigUnit>()
        excludedBigUnits.addAll(getExcludedUnits(mathTopSelection, Subject.수학_상))
        excludedBigUnits.addAll(getExcludedUnits(mathBottomSelection, Subject.수학_하))
        excludedBigUnits.addAll(getExcludedUnits(math1Selection, Subject.수학I))
        excludedBigUnits.addAll(getExcludedUnits(math2Selection, Subject.수학II))
        excludedBigUnits.addAll(getExcludedUnits(probAnsStatSelection, Subject.확률과통계))
        excludedBigUnits.addAll(getExcludedUnits(calculusSelection, Subject.미적분))
        excludedBigUnits.addAll(getExcludedUnits(geometrySelection, Subject.기하))

        return excludedBigUnits
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "단원")
        if(modifyBtn.isEnableUI()) {
            UserManager.setRecentExclude(requireContext(), user!!, getCalcExcludedUnits(), successCB = {
                listener?.onModifyCompleted()
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackBtnClicked()
                }, 0)
            })
        }
    }

    override fun onSelectionChanged(view: PulleyInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            modifyBtn.toDisableUI()
        } else {
            modifyBtn.toEnableUI()
        }
    }
}
