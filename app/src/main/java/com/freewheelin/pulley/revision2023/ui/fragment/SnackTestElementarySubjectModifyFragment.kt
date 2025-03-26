package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentElementarySubjectModifyBinding
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel

class SnackTestElementarySubjectModifyFragment : Fragment(), ElementarySchoolUnitSelectionListener {
    private lateinit var binding: FragmentElementarySubjectModifyBinding
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_elementary_subject_modify, container, false)
        return binding.root
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

        }

    }

    private fun initObserve () {
        viewModel.recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
            binding.selectionContainer.let {
                it.removeAllViews()
                subjects.forEach { subject ->
                    val selection = ElementarySchoolUnitSelection(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        initSelected(subject)
                        listener = this@SnackTestElementarySubjectModifyFragment
                    }
                    it.addView(selection)
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
    private fun getSelectedUnit(): List<Int> {
        val selectionList = binding.selectionContainer.children
            .filter { it.isVisible }
            .filterIsInstance<ElementarySchoolUnitSelection>()
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

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestElementarySubjectModifyFragment)
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
            SnackTestElementarySubjectModifyFragment().apply {
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