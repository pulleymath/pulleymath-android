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
import com.freewheelin.pulley.databinding.FragmentElementarySubjectModifyBinding
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelectionListener

class SnackTestElementarySubjectModifyFragment : Fragment(), ElementarySchoolUnitSelectionListener {
    private lateinit var binding: FragmentElementarySubjectModifyBinding
//    var viewModel: RecommendSettingViewModel? = null
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

        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    fun initUI() {
        binding.apply {
            selectionContainer.children.forEach {
                (it as ElementarySchoolUnitSelection).listener = this@SnackTestElementarySubjectModifyFragment
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
//                        SubjectV3.초1_1 -> elementary11Selection
//                        SubjectV3.초1_2 -> elementary12Selection
//                        SubjectV3.초2_1 -> elementary21Selection
//                        SubjectV3.초2_2 -> elementary22Selection
                        SubjectV3.초3_1 -> elementary31Selection
                        SubjectV3.초3_2 -> elementary32Selection
                        SubjectV3.초4_1 -> elementary41Selection
                        SubjectV3.초4_2 -> elementary42Selection
                        SubjectV3.초5_1 -> elementary51Selection
                        SubjectV3.초5_2 -> elementary52Selection
                        SubjectV3.초6_1 -> elementary61Selection
                        SubjectV3.초6_2 -> elementary62Selection
                        else -> { elementary51Selection }
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
            val allBtnList =
//                elementary11Selection.getAllBtn()
//                .plus(elementary12Selection.getAllBtn())
//                .plus(elementary21Selection.getAllBtn())
//                .plus(elementary22Selection.getAllBtn())
                    elementary31Selection.getAllBtn()
                .plus(elementary32Selection.getAllBtn())
                .plus(elementary41Selection.getAllBtn())
                .plus(elementary42Selection.getAllBtn())
//                elementary51Selection.getAllBtn()
                .plus(elementary51Selection.getAllBtn())
                .plus(elementary52Selection.getAllBtn())
                .plus(elementary61Selection.getAllBtn())
                .plus(elementary62Selection.getAllBtn())

            return@getUnitClicked allBtnList.map { it.isSelected }
                .reduce { p1, p2 ->
                    p1 || p2
                }
        }
    }

    private fun onBackBtnClicked() {
        viewModel.removeStep(this@SnackTestElementarySubjectModifyFragment)
    }

    private fun getClickedUnit(): List<Int>? {
        binding.apply {
            val allBtnList =
//                elementary11Selection.getAllBtn()
//                .plus(elementary12Selection.getAllBtn())
//                .plus(elementary21Selection.getAllBtn())
//                .plus(elementary22Selection.getAllBtn())
                elementary31Selection.getAllBtn()
//                .plus(elementary31Selection.getAllBtn())
                .plus(elementary32Selection.getAllBtn())
                .plus(elementary41Selection.getAllBtn())
                .plus(elementary42Selection.getAllBtn())
//                elementary51Selection.getAllBtn()
                .plus(elementary51Selection.getAllBtn())
                .plus(elementary52Selection.getAllBtn())
                .plus(elementary61Selection.getAllBtn())
                .plus(elementary62Selection.getAllBtn())

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
            SnackTestElementarySubjectModifyFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }

    override fun onSelectionChanged(view: View) {
        binding.modifyBtn.isEnabled = getUnitClicked()
    }
}