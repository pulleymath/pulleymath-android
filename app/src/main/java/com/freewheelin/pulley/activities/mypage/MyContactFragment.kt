package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentMyContactBinding

class MyContactFragment : MyPageBaseFragment() {

    lateinit var binding: FragmentMyContactBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_contact, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contactBtn.setOnClickListener {
            onContactBtnClicked()
        }
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }

    fun onContactBtnClicked() {
        val user = requireActivity().application.user!!
        val dialog = MyContactWritingDialog(requireContext(), user)
        dialog.show()
    }
}
