package com.freewheelin.pulley.activities.mypage

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.MyPageActivity
import kotlinx.android.synthetic.main.fragment_my_signup_info.*

interface MyPageActionListener {
    fun onModifyCompleted()
}

open class MyPageBaseFragment: Fragment() {

    var listener: MyPageActionListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backBtn.setOnClickListener {
            onBackBtnClicked()
        }
    }
    fun onBackBtnClicked() {
        Log.d(javaClass.simpleName, "onBackBtnClicked=$this, activity=${activity?.javaClass?.simpleName}")

        val focusView: View? = requireActivity().currentFocus
        if (focusView != null) {
            val imm: InputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            focusView.clearFocus()
        }

        (activity as? LearningTabActivity)?.back(this)
        (activity as? MyPageActivity)?.back(this)
        (activity as? MyRecommendSettingActivity)?.back(this)
    }
}