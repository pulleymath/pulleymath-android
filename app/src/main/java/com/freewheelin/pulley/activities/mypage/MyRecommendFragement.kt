package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.User
import kotlinx.android.synthetic.main.fragment_my_recommend_fragement.*


class MyRecommendFragement : MyPageBaseFragment(), MyPageSettingDialogListener {

    val user: User
        get() = activity?.application?.user!!

    val difficultyText = listOf("더 쉽게", "수준에 맞게", "더 어렵게")
    val coverRangeText = listOf("최근 공부한 범위", "수능 전범위", "내가 선택한 과목")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_recommend_fragement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        modifyBtn.setOnClickListener {
            onModifyBtnClicked()
        }
        configureUI(user)
    }

    override fun onModifyCompleted(user: User) {
        configureUI(user)
    }

    fun onModifyBtnClicked() {
        val dialog = MyRecommendSettingDialog(requireContext(), user, this)
        dialog.show()
    }

    fun configureUI(user: User) {
        val recommendLevel = user.recommendLevel
        val recommendChapter = user.recommendChapter

        if(recommendLevel != null && recommendChapter != null) {
            difficultyTv.text = difficultyText[recommendLevel]
            coverRangeTv.text = coverRangeText[recommendChapter]
        }
    }

}
