package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.partialFont
import kotlinx.android.synthetic.main.fragment_my_contact.*

class MyContactFragment : MyPageBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_contact, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactBtn.setOnClickListener {
            onContactBtnClicked()
        }
    }

    fun onContactBtnClicked() {
        val user = requireActivity().application.user!!
        val dialog = MyContactWritingDialog(requireContext(), user)
        dialog.show()
    }
}
