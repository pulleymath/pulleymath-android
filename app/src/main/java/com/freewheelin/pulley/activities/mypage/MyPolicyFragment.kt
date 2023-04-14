package com.freewheelin.pulley.activities.mypage

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentMyPolicyBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast

class MyPolicyFragment : MyPageBaseFragment() {
    lateinit var binding: FragmentMyPolicyBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_policy, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.servicePolicyBtn.setOnClickListener {
            if (user?.serviceType?.isGuestUser == true) {
                val targetUrl = URL.이용약관
                IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
            } else {
                viewModel.getTempToken { shortToken ->
                    val relativeUrl = URL.이용약관.substringAfter("https://pulleymath.com")
                    val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                }
            }

        }

        binding.personalPolicyBtn.setOnClickListener {
            if (user?.serviceType?.isGuestUser == true) {
                val targetUrl = URL.개인정보취급방침
                IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
            } else {
                viewModel.getTempToken { shortToken ->
                    val relativeUrl = URL.개인정보취급방침.substringAfter("https://pulleymath.com")
                    val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                }
            }
        }
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }
}
