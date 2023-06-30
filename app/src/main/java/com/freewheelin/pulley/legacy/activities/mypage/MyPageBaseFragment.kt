package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

interface MyPageActionListener {
    fun onModifyCompleted()
}

open class MyPageBaseFragment: Fragment() {

    var listener: MyPageActionListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        backBtn.setOnClickListener {
//            onBackBtnClicked()
//        }
    }
    fun removeThisPage() {
        (activity as? MainActivity)?.backMyPage(this)
    }
    fun onBackBtnClicked() {
        Log.d(javaClass.simpleName, "onBackBtnClicked=$this, activity=${activity?.javaClass?.simpleName}")

        val focusView: View? = requireActivity().currentFocus
        if (focusView != null) {
            val imm: InputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            focusView.clearFocus()
        }

        (activity as? MainActivity)?.backMyPage(this)
    }
}