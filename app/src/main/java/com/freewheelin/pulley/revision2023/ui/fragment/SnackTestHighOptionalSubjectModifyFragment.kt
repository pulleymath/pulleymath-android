package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentHighOptionalSubjectModifyBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DaebakInputSelection
import com.freewheelin.pulley.legacy.views.DaebakInputSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel

class SnackTestHighOptionalSubjectModifyFragment : Fragment(), DaebakInputSelectionListener {
    private lateinit var binding: FragmentHighOptionalSubjectModifyBinding
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_high_optional_subject_modify, container, false)
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
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()

            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
            noneSelection.setOnClickListener { onNoneSelection() }
        }

        viewModel.apply {
            userRecommendLiveData.observe(viewLifecycleOwner) {
                it.userSubjects.optionalSubjects.forEachIndexed { index, recommendSubject ->
                    selectionList.get(index).let {
                        it.listener = this@SnackTestHighOptionalSubjectModifyFragment
                        it.label = recommendSubject.subjectName
                        it.chapterList = recommendSubject.chapters
                        it.buttonTitles = recommendSubject.chapters.map { chapter -> chapter.chapterName }
                        it.result = listOf(false) + recommendSubject.chapters.map { chapter -> chapter.isSelected }
                    }
                }
                val subjectSize = it.userSubjects.optionalSubjects.size
                selectionList.forEachIndexed { index,selection ->
                    if (index >= subjectSize) {
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


    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
    companion object {
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestHighOptionalSubjectModifyFragment().apply {
                this.viewModel = viewModel
            }
    }
    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "선택과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedIds = getSelectedUnit()
            viewModel.updateOptionalSubject(selectedIds) {
                onBackBtnClicked()
            }
        }
    }
    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestHighOptionalSubjectModifyFragment)
    }
    override fun onSelectionChanged(view: DaebakInputSelection) {
        setModifyBtn()
    }
}