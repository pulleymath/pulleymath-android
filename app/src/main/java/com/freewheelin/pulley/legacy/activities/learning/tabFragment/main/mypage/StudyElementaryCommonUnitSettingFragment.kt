package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentStudyUnitElementarySchoolCommonSettingBinding
import com.freewheelin.pulley.legacy.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.legacy.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.ElementarySchoolUnitSelectionListener

class StudyElementaryCommonUnitSettingFragment : MyPageBaseFragment(),
    ElementarySchoolUnitSelectionListener {
    lateinit var binding: FragmentStudyUnitElementarySchoolCommonSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_elementary_school_common_setting, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
        initObserve()

    }

    fun initObserve () {
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

    fun setUpUI() {
        with(binding) {
            viewModel.fetchRecommendSubject()

            selectionContainer.children.forEach {
                (it as ElementarySchoolUnitSelection).listener = this@StudyElementaryCommonUnitSettingFragment
            }
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
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

    override fun onStop() {
        super.onStop()
    }

    override fun onSelectionChanged(view: View) {
        binding.modifyBtn.isEnabled = getUnitClicked()
    }

    private fun getUnitClicked (): Boolean {
        binding.apply {
            val allBtnList =
//                elementary11Selection.getAllBtn()
//                .plus(elementary12Selection.getAllBtn())
//                .plus(elementary21Selection.getAllBtn())
//                .plus(elementary22Selection.getAllBtn())
//                .plus(elementary31Selection.getAllBtn())
//                elementary51Selection.getAllBtn()
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
    private fun getClickedUnit(): List<Int>? {
        binding.apply {
            val allBtnList =
//                elementary11Selection.getAllBtn()
//                .plus(elementary12Selection.getAllBtn())
//                .plus(elementary21Selection.getAllBtn())
//                .plus(elementary22Selection.getAllBtn())
//                .plus(elementary31Selection.getAllBtn())
                elementary31Selection.getAllBtn()
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
}
