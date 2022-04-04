package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentMyRecommendFragementBinding
import com.freewheelin.pulley.model.User


class MyRecommendFragement : MyPageBaseFragment(), MyPageSettingDialogListener {
    lateinit var binding: FragmentMyRecommendFragementBinding
    val user: User
        get() = activity?.application?.user!!

    val difficultyText = listOf("더 쉽게", "수준에 맞게", "더 어렵게")
    val coverRangeText = listOf("최근 공부한 범위", "수능 전범위", "내가 선택한 과목")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_recommend_fragement, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.modifyBtn.setOnClickListener {
            onModifyBtnClicked()
        }
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
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
            binding.difficultyTv.text = difficultyText[recommendLevel]
            binding.coverRangeTv.text = coverRangeText[recommendChapter]
        }
    }

}
