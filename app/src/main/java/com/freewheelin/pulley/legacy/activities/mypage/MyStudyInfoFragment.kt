package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyCommonUnitSettingFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyMiddleCommonUnitSettingFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyOptionalUnitSettingFragment
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentMyStudyInfoBinding
import com.freewheelin.pulley.legacy.dialogs.CompleteDialog
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

class MyStudyInfoFragment : MyPageBaseFragment(), MyPageSettingDialogListener {
    lateinit var binding: FragmentMyStudyInfoBinding
    companion object {
        const val RELOAD = "study_info_reload"
    }
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_study_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        val user = requireActivity().application!!.user!!
        configureUI(user)

        setFragmentResultListener(RELOAD) { key, bundle ->
            reload()
            viewModel.fetchRecommendSubject()
        }
        initObserve()
    }
    fun initObserve() {
        viewModel.apply {
            schoolType.observe(viewLifecycleOwner) {
                binding.apply {
//                majorLabel.visibleIf(it.isHigh)

                }
            }
            recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
                val subjectNames = subjects
                    .filter {
                        it.chapters
                            .map { it.isSelected }
                            .reduce { p1, p2 ->
                                p1 || p2
                            }
                    }
                    .map { it.subjectName }
                    .joinTo(StringBuilder(), ", ").toString()

                binding.commonSubjectTv.text = subjectNames
                binding.middleSubjectTv.text = subjectNames
            }
            recommendOptionalSubjects.observe(viewLifecycleOwner) { subjects ->
                val subjectNames = subjects
                    .filter {
                        it.chapters
                            .map { it.isSelected }
                            .reduce { p1, p2 ->
                                p1 || p2
                            }
                    }
                    .map { it.subjectName }
                    .joinTo(StringBuilder(), ", ").toString()
                binding.optionalSubjectTv.text = subjectNames
            }
        }

    }

    private fun reload() {
        val user = MyApplication.user!!
        Log.d(javaClass.simpleName, "user=${user.fullName}")
        configureUI(user)
    }

    private fun initUI() {
        with(binding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            viewModel.fetchRecommendSubject()
            modifyBtn.setOnClickListener { moveTo(MyStudyInfoSettingFragment()) }
            modifyCommonBtn.setOnClickListener { moveTo(StudyCommonUnitSettingFragment()) }
            modifySelectBtn.setOnClickListener { moveTo(StudyOptionalUnitSettingFragment()) }
            middleSubjectModifyBtn.setOnClickListener { moveTo(StudyMiddleCommonUnitSettingFragment()) }
//        deleteAllBtn.setOnClickListener { deleteAll() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    // 이번에 보류
    private fun deleteAll() {
        DialogUtils.confirmDeleteAllStudy(requireActivity()) {

        }
    }

    private fun configureUI(user: UserV4) {
        with(binding) {
            gradeTv.text = user.userGrade.text

            configureSchool(user)

            if (user.userGrade.isMiddle) {
                majorTv.text = "-"
                ratingTv.text = "-"
            } else {
                majorTv.text = user.userMajor.title
                val ratingText = when (user.initMoGrade) {
                    0 -> "모름"
                    in 1..9 -> "${user.initMoGrade}등급"
                    else -> null

                }
                ratingTv.text = ratingText
            }

//            configureStudy(user)
        }
    }

    private fun configureSchool(user: UserV4) {
        with(binding) {
            if(user.schoolID?:0 > 0) {
                schoolLabel.text = "학교"
                schoolTv.text = user.schoolName?:""
            }else if(user.regionID?:0 > 0) {
                schoolLabel.text = "지역"
                schoolTv.text = user.regionName?:""
            }
        }
    }

    override fun onModifyCompleted(user: UserV4) {
        CompleteDialog(requireContext(), "수정 완료!\n업데이트되었습니다.", "해당 수정 내역은 추천 문항에 반영됩니다.").showFor()
        configureUI(user)
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }
}
