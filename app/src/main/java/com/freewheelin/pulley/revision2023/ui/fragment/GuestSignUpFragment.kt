package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentGuestJoinIntroduceBinding
import com.freewheelin.pulley.databinding.FragmentGuestLoginBinding
import com.freewheelin.pulley.databinding.FragmentGuestSignUpBinding
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide1Binding
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseGuideActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog.GuestJoinStep
import com.freewheelin.pulley.revision2023.viewmodel.GuestJoinViewModel
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.utils.visibleIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class GuestSignUpFragment : Fragment() {
    private lateinit var binding: FragmentGuestSignUpBinding
    var viewModel: GuestJoinViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_sign_up, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
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
            GuestSignUpFragment().apply {
                arguments = Bundle().apply {
//                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
}