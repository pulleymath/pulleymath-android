package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.databinding.FragmentMiddleSubjectModifyBinding
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DaebakToast
import kotlin.math.log

class SnackTestMiddleSubjectModifyFragment : Fragment(), MiddleSchoolUnitSelectionListener {
    private lateinit var binding: FragmentMiddleSubjectModifyBinding
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_middle_subject_modify, container, false)
        return binding.root
    }

    private val selectionList by lazy {
        listOf(
            binding.selection1, binding.selection2, binding.selection3, binding.selection4,
            binding.selection5, binding.selection6, binding.selection7, binding.selection8,
            binding.selection9, binding.selection10, binding.selection11, binding.selection12,
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            viewModel.fetchRecommendSubject()
            setScreen()
            initUI()
            initObserve()

        }

    }

    fun initUI() {
        binding.apply {
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
            selectionList.forEach {
                it.listener = this@SnackTestMiddleSubjectModifyFragment
            }
        }

    }

    private fun initObserve () {
        viewModel.recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
            subjects.forEachIndexed { index, recommendSubject ->
                selectionList.get(index).initSelected(recommendSubject)

                selectionList.forEachIndexed { index,selection ->
                    if (index >= subjects.size) {
                        selection.visibleIf(false)
                    }
                }
            }
        }
    }
    private fun onModifyBtnClicked() {
        if (binding.modifyBtn.isEnabled) {
            val selectedBitUnitIds = getSelectedUnit()
            viewModel.updateCommonSubject(selectedBitUnitIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                DaebakToast.show(requireContext(), "수정되었습니다.")
                onBackBtnClicked()
            }
        }
    }

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestMiddleSubjectModifyFragment)
    }

    private fun getSelectedUnit(): List<Int> {
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
    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
    companion object {
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestMiddleSubjectModifyFragment().apply {
                this.viewModel = viewModel
            }
    }

    override fun onSelectionChanged(view: View) {
        if (getSelectedUnit().isEmpty()) {
            binding.modifyBtn.isEnabled = false
        } else {
            binding.modifyBtn.isEnabled = true
        }
    }
}