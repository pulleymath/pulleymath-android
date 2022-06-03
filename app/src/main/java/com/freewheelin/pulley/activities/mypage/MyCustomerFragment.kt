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

import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentMyCustomerBinding
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast
import java.lang.Exception

class MyCustomerFragment : MyPageBaseFragment() {

    val user
        get() = requireActivity().application.user!!


    lateinit var binding: FragmentMyCustomerBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_customer, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        with(binding) {
            faqBtn.setOnClickListener {
                IntentUtils.openWebLink(requireContext(), URL.FAQ, requireContext().packageManager)
            }

            termsBtn.setOnClickListener {
                IntentUtils.openWebLink(requireContext(), URL.이용약관, requireContext().packageManager)
            }

            privacyBtn.setOnClickListener {
                IntentUtils.openWebLink(requireContext(), URL.개인정보취급방침, requireContext().packageManager)
            }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }
}

