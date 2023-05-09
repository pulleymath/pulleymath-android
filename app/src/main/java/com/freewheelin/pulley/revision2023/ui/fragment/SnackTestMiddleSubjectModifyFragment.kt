package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.databinding.FragmentHighCommonSubjectModifyBinding
import com.freewheelin.pulley.databinding.FragmentMiddleSubjectModifyBinding
import com.freewheelin.pulley.databinding.FragmentTestExamRangeBinding
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel

class SnackTestMiddleSubjectModifyFragment : Fragment() {
    private lateinit var binding: FragmentMiddleSubjectModifyBinding
    var viewModel: RecommendSettingViewModel? = null

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

//            signUpBtn.setOnClickListener {
//                // TODO 추후에 다이얼로그화
////                viewModel?.replaceStep?.let { it(GuestJoinStep.회원가입) }
//                val intent = SignupActivity.getIntent(requireContext(), true)
//                startActivity(intent)
//            }
//            loginTv.setOnClickListener {
//                viewModel?.setStep?.let { it(ViewType.내가선택한과목) }
//            }
        }

        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SnackTestMiddleSubjectModifyFragment().apply {
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
}