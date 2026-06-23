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
import com.freewheelin.pulley.databinding.FragmentSubjectExcludeModifyBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.views.PulleyInputSelection
import com.freewheelin.pulley.legacy.views.PulleyInputSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import androidx.lifecycle.ViewModelProvider

class SnackTestSubjectExcludeModifyFragment : Fragment(), PulleyInputSelectionListener {
    private lateinit var binding: FragmentSubjectExcludeModifyBinding

    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subject_exclude_modify, container, false)
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
                recommend.recentStudySubjects.let { subjects ->
                    binding.selectionWrapperLl.let {
                        it.removeAllViews()
                        subjects.forEach { subject ->
                            val pulleySelection = PulleyInputSelection(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                )
                                init(subject.chapters)
                                label = subject.subjectName
                                listener = this@SnackTestSubjectExcludeModifyFragment
                                val scale = resources.displayMetrics.density
                                val paddingBottom = (24 * scale + 0.5f).toInt()
                                setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                            }
                            it.addView(pulleySelection)
                        }
                    }
                }
            }
        }

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
            SnackTestSubjectExcludeModifyFragment().apply {
                this.viewModel = viewModel
            }
    }

    override fun onSelectionChanged(view: PulleyInputSelection) {
        binding.modifyBtn.isEnabled = true
    }
    private fun onModifyBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "단원")
        if(binding.modifyBtn.isEnabled) {
            val ids = getExcludedUnits()
            viewModel.excludeSubjects(ids) {
                onBackBtnClicked()
            }
        }
    }
    private fun getExcludedUnits(): List<Int> {
        val selectionList = binding.selectionWrapperLl.children
            .filter { it.isVisible }
            .filterIsInstance<PulleyInputSelection>()
            .toList()
        val excludedChapterIds = selectionList.flatMap {
            it.btnList.mapIndexedNotNull { index, btn ->
                if (!btn.isSelected) it.chapterList.get(index).chapterId
                else null
            }
        }
        return excludedChapterIds
    }
    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestSubjectExcludeModifyFragment)
    }
}