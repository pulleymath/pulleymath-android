package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.databinding.FragmentHighCommonSubjectModifyBinding
import com.freewheelin.pulley.databinding.FragmentMiddleSubjectModifyBinding
import com.freewheelin.pulley.databinding.FragmentTestExamRangeBinding
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog.*
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.views.DaebakToast

class SnackTestMiddleSubjectModifyFragment : Fragment(), MiddleSchoolUnitSelectionListener {
    private lateinit var binding: FragmentMiddleSubjectModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
    lateinit var viewModel: RecommendSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_middle_subject_modify, container, false)
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

        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    fun initUI() {
        binding.apply {
            selectionContainer.children.forEach {
                (it as MiddleSchoolUnitSelection).listener = this@SnackTestMiddleSubjectModifyFragment
            }
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }

        }

    }

    private fun initObserve () {
        viewModel.recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
            subjects.forEach {
                val subject = SubjectV3.idOfNonNull(it.subjectId)
                binding.apply {
                    val selection = when (subject) {
                        SubjectV3.중1_1 -> middle11Selection
                        SubjectV3.중1_2 -> middle12Selection
                        SubjectV3.중2_1 -> middle21Selection
                        SubjectV3.중2_2 -> middle22Selection
                        SubjectV3.중3_1 -> middle31Selection
                        SubjectV3.중3_2 -> middle32Selection
                        else -> { middle11Selection }
                    }
                    selection.initSelected(it)

                    modifyBtn.isEnabled = getUnitClicked()
                }
            }
        }
    }
    private fun onModifyBtnClicked() {
        val selectedIds = getClickedUnit()
        if (selectedIds != null) {
            viewModel.updateCommonSubject(selectedIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                DaebakToast.show(requireContext(), "수정되었습니다.")
                onBackBtnClicked()
            }
        } else {
            DaebakToast.show(requireContext(), "한개 이상 선택해주세요.")
        }
    }
    private fun getUnitClicked (): Boolean {
        binding.apply {
            val allBtnList = middle11Selection.getAllBtn()
                .plus(middle12Selection.getAllBtn())
                .plus(middle21Selection.getAllBtn())
                .plus(middle22Selection.getAllBtn())
                .plus(middle31Selection.getAllBtn())
                .plus(middle32Selection.getAllBtn())

            return@getUnitClicked allBtnList.map { it.isSelected }
                .reduce { p1, p2 ->
                    p1 || p2
                }
        }
    }

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestMiddleSubjectModifyFragment)
    }

    private fun getClickedUnit(): List<Int>? {
        binding.apply {
            val allBtnList = middle11Selection.getAllBtn()
                .plus(middle12Selection.getAllBtn())
                .plus(middle21Selection.getAllBtn())
                .plus(middle22Selection.getAllBtn())
                .plus(middle31Selection.getAllBtn())
                .plus(middle32Selection.getAllBtn())

            return allBtnList.filter { it.isSelected }
                .map { it.bigUnits }
                .takeIf { it.isNotEmpty() }
                ?.reduce { p1, p2 ->
                    p1.plus(p2).toMutableList()
                }
                ?.map { it.id }
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
            SnackTestMiddleSubjectModifyFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }

    override fun onSelectionChanged(view: View) {
        if (getUnitClicked()) {
            binding.modifyBtn.isEnabled = true
        } else {
            binding.modifyBtn.isEnabled = false
        }
    }
}