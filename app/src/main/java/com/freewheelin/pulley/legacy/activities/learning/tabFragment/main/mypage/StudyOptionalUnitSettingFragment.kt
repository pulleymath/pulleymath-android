package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentStudyUnitOptionalSettingBinding
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener

class StudyOptionalUnitSettingFragment : MyPageBaseFragment(), DaebakInputSelectionListener {

    lateinit var binding: FragmentStudyUnitOptionalSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_optional_setting, container, false)
        return binding.root
    }

    private val selectionList by lazy {
        listOf(
            binding.selection1, binding.selection2, binding.selection3,
            binding.selection4, binding.selection6, binding.selection6,
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            noneSelection.setOnClickListener { onNoneSelection() }
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
        viewModel.apply {
            fetchRecommendSubject()
            recommendOptionalSubjects.observe(viewLifecycleOwner) { subjects ->
                subjects.forEachIndexed { index, recommendSubject ->
                    selectionList.get(index).let {
                        it.listener = this@StudyOptionalUnitSettingFragment
                        it.label = recommendSubject.subjectName
                        it.chapterList = recommendSubject.chapters
                        it.buttonTitles = recommendSubject.chapters.map { chapter -> chapter.chapterName }
                        it.result = listOf(false) + recommendSubject.chapters.map { chapter -> chapter.isSelected }
                    }
                }
                selectionList.forEachIndexed { index,selection ->
                    if (index >= subjects.size) {
                        selection.visibleIf(false)
                    }
                }
            }
        }
    }

    private fun onNoneSelection() {
        with(binding) {
            noneSelection.isSelected = !noneSelection.isSelected
            if (noneSelection.isSelected) {
                selectionList.forEach {
                    it.release()
                }
            } else {
                setModifyBtn()
            }
        }
    }

    private fun getSelectedUnit(): List<Int> {
        val selectedChapterIds = selectionList.flatMap {
            val isTotalClicked = it.result.first()
            if (isTotalClicked) {
                it.chapterList.map { chapter -> chapter.chapterId }
            }
            else {
                it.result.subList(1, it.result.size).mapIndexedNotNull { index, bool ->
                    if (bool) it.chapterList.get(index).chapterId
                    else null
                }
            }
        }
        return selectedChapterIds
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "선택과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit()
            viewModel.updateOptionalSubject(selectedIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackBtnClicked()
                }, 0)
            }
        }
    }

    override fun onSelectionChanged(view: DaebakInputSelection) {
        setModifyBtn()
    }

    private fun setModifyBtn() {
        with(binding) {
            noneSelection.isSelected = getSelectedUnit().isEmpty()

            if(getSelectedUnit().isEmpty() && !noneSelection.isSelected) {
                modifyBtn.isEnabled = false
            } else {
                modifyBtn.isEnabled = true
            }
        }
    }
}
