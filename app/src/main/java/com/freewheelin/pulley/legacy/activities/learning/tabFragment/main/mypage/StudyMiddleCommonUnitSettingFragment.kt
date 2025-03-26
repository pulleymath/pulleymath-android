package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentStudyUnitMiddleSchoolCommonSettingBinding
import com.freewheelin.pulley.legacy.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.view.BigUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.BigUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel

class StudyMiddleCommonUnitSettingFragment : MyPageBaseFragment(),
    BigUnitSelectionListener {
    lateinit var binding: FragmentStudyUnitMiddleSchoolCommonSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_middle_school_common_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
        viewModel.apply {
            fetchRecommendSubject()
            recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
                binding.selectionContainer.let {
                    it.removeAllViews()
                    subjects.forEach { subject ->
                        val unitSelection = BigUnitSelection(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            initSelected(subject)
                            listener = this@StudyMiddleCommonUnitSettingFragment
                        }

                        it.addView(unitSelection)
                    }
                }
            }
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "중등과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedBitUnitIds = getSelectedUnit()
            viewModel.updateCommonSubject(selectedBitUnitIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                DaebakToast.show(requireContext(), "수정되었습니다.")
                onBackBtnClicked()
            }
        }
    }

    override fun onSelectionChanged(view: View) {
        if (getSelectedUnit().isEmpty()) {
            binding.modifyBtn.isEnabled = false
        } else {
            binding.modifyBtn.isEnabled = true
        }
    }

    private fun getSelectedUnit(): List<Int> {
        val selectionList = binding.selectionContainer.children
            .filter { it.isVisible }
            .filterIsInstance<BigUnitSelection>()
            .toList()
        val selectedChapterIds = selectionList.flatMap {
            val isTotalClicked = it.binding.unitTotal.isSelected
            if (isTotalClicked) {
                it.chapterList.map { chapter -> chapter.chapterId }
            }
            else {
                it.btnList.mapIndexedNotNull { index, btn ->
                    if (btn.isSelected) it.chapterList.get(index).chapterId
                    else null
                }
            }
        }
        return selectedChapterIds
    }
}
