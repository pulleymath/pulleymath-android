package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.databinding.FragmentGuestJoinIntroduceBinding
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog.GuestJoinStep
import com.freewheelin.pulley.revision2023.viewmodel.GuestJoinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class GuestJoinIntroduceFragment : Fragment() {
    private lateinit var binding: FragmentGuestJoinIntroduceBinding
    var viewModel: GuestJoinViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_join_introduce, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            signUpBtn.setOnClickListener {
                // TODO 추후에 다이얼로그화
//                viewModel?.replaceStep?.let { it(GuestJoinStep.회원가입) }
                val intent = SignupActivity.getIntent(requireContext(), true)
                startActivity(intent)
                viewModel?.exitBtn()
            }
            loginTv.setOnClickListener {
                viewModel?.setStep?.let { it(GuestJoinStep.로그인) }
            }
        }

        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GuestJoinIntroduceFragment().apply {
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
}