package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.databinding.FragmentMyPulleyPaidListBinding
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

class MyPulleyPaidList : MyPageBaseFragment() {
    lateinit var binding: FragmentMyPulleyPaidListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_pulley_paid_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
    }

    private fun setViews() {
        binding.backBtn.setOnClickListener {
            onBackBtnClicked()
        }
        binding.myPageBtn.setOnClickListener {
            onBackBtnClicked()
            onBackBtnClicked()
        }
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }
}