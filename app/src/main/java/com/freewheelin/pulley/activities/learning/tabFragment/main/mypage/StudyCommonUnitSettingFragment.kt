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
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.DaebakInputSelection
import com.freewheelin.pulley.views.DaebakInputSelectionListener
import kotlinx.android.synthetic.main.fragment_study_unit_setting.*


class StudyCommonUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study_unit_common_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        mathTopSelection.listener = this
        mathBottomSelection.listener = this
        math1Selection.listener = this
        math2Selection.listener = this

        mathTopSelection.buttonTitles = listOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식).map { it.title }
        mathBottomSelection.buttonTitles = listOf(BigUnit.집합과_명제, BigUnit.함수, BigUnit.순열과_조합).map { it.title }
        math1Selection.buttonTitles = listOf(BigUnit.지수함수와_로그함수, BigUnit.삼각함수, BigUnit.수열).map { it.title }
        math2Selection.buttonTitles = listOf(BigUnit.함수의_극한과_연속, BigUnit.미분, BigUnit.적분).map { it.title }

        setCommonUnit()

        modifyBtn.setOnClickListener { onModifyBtnClicked() }
    }

    private fun setCommonUnit() {
        val studiedUnit = user!!.studiedUnit

        mathTopSelection.result = listOf(
                false,
                studiedUnit.contains(BigUnit.다항식),
                studiedUnit.contains(BigUnit.방정식과_부등식),
                studiedUnit.contains(BigUnit.도형의_방정식)
        )

        mathBottomSelection.result = listOf(
                false,
                studiedUnit.contains(BigUnit.집합과_명제),
                studiedUnit.contains(BigUnit.함수),
                studiedUnit.contains(BigUnit.순열과_조합)
        )

        math1Selection.result = listOf(
                false,
                studiedUnit.contains(BigUnit.지수함수와_로그함수),
                studiedUnit.contains(BigUnit.삼각함수),
                studiedUnit.contains(BigUnit.수열)
        )

        math2Selection.result = listOf(
                false,
                studiedUnit.contains(BigUnit.함수의_극한과_연속),
                studiedUnit.contains(BigUnit.미분),
                studiedUnit.contains(BigUnit.적분)
        )
    }

    private fun getSelectedUnit(): Collection<BigUnit> {
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

    private fun onModifyBtnClicked() {
        MyApplication.user?.let { user ->
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "공통과목", "변경하기")
            if (modifyBtn.isEnableUI()) {
                UserManager.setInitStudy(requireContext(), user, getSelectedUnit(), successCB = {
                    val selected = getSelectedUnit().map { it.id }
                    user.rawInitStudied = selected.joinToString(",")
                    user.commit("update mypage Update [COMMON] subject")
                    setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                    Handler(Looper.getMainLooper()).postDelayed({
                        onBackBtnClicked()
                    }, 0)
                })
            }
        }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        if(getSelectedUnit().isEmpty()) {
            modifyBtn.toDisableUI()
        } else {
            modifyBtn.toEnableUI()
        }
    }
}
