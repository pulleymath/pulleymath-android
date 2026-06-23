package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentHighCommonSubjectModifyBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.ui.view.BigUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.BigUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import androidx.lifecycle.ViewModelProvider

class SnackTestHighCommonSubjectModifyFragment : Fragment(), BigUnitSelectionListener {
    private lateinit var binding: FragmentHighCommonSubjectModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_high_common_subject_modify, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 재생성(프로세스 사망/구성 변경) 시 newInstance 주입이 누락되므로, 부모(SnackTestRecommendSettingDialog)
        // 스코프에서 공유 viewModel을 재획득한다. lateinit 미초기화 크래시 방어.
        if (!::viewModel.isInitialized) {
            viewModel = ViewModelProvider(requireParentFragment())[RecommendSettingViewModel::class.java]
        }
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()

            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
        viewModel.apply {
            userRecommendLiveData.observe(viewLifecycleOwner) { recommend ->
                recommend.userSubjects.commonSubjects.let { subjects ->
                    binding.selectionContainer.let {
                        it.removeAllViews()
                        subjects.forEach { subject ->
                            val unitSelection = BigUnitSelection(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                initSelected(subject)
                                listener = this@SnackTestHighCommonSubjectModifyFragment
                            }

                            it.addView(unitSelection)
                        }
                    }
                }
            }
        }

    }

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestHighCommonSubjectModifyFragment)
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "공통과목", "변경하기")
        if (binding.modifyBtn.isEnabled) {
            val selectedBitUnitIds = getSelectedUnit()
            viewModel.updateCommonSubject(selectedBitUnitIds) {
                onBackBtnClicked()
            }
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

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestHighCommonSubjectModifyFragment().apply {
                this.viewModel = viewModel
            }
    }

    override fun onSelectionChanged(view: View) {
        if(getSelectedUnit().isEmpty()) {
            binding.modifyBtn.isEnabled = false
        } else {
            binding.modifyBtn.isEnabled = true
        }
    }
}