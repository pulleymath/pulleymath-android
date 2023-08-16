package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentMyStartChallengeBinding

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyCommonUnitSettingFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyMiddleCommonUnitSettingFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.mypage.StudyOptionalUnitSettingFragment
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentMyStudyInfoBinding
import com.freewheelin.pulley.legacy.dialogs.CompleteDialog
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.StartChallengeInfoDialog
import com.freewheelin.pulley.revision2023.ui.view.MissionStampView

class MyStartChallengeFragment : MyPageBaseFragment() {
    lateinit var binding: FragmentMyStartChallengeBinding
    companion object {
        const val CHALLENGE_MENU_REMOVED = "CHALLENGE_MENU_REMOVED"
    }
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_start_challenge, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        initObserve()
    }
    private fun initUI() {
        with(binding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            backBtn.setOnClickListener { onBackBtnClicked() }
            stopChallengeCl.setOnClickListener {
                DialogUtils.confirmV2(
                    context = requireContext(),
                    title = "스타트 챌린지를 그만하시겠습니까?",
                    contents = "지금 종료하면 스타트 챌린지가 삭제되어\n현황을 확인하실 수 없습니다.",
                    rightBtnText = "그만하기",
                    successCb = {
                        viewModel.stopChallenge {
                            setFragmentResult(CHALLENGE_MENU_REMOVED, bundleOf())
                            onBackBtnClicked()
                        }
                    }
                )
            }
            couponBtn.setOnClickListener {
                viewModel.askForRedeemOfChallenge() {
                    val fm = requireActivity().supportFragmentManager
                    if (!fm.isDestroyed) {
                        val infoDialog = StartChallengeInfoDialog.newInstance(viewModel.userChallengeId!!, true)
                        infoDialog.startCallback = {
                            val pgDialog = PurchaseGuideDialog.newInstance(2)
                            fm.let { pgDialog.show(it, "purchaseGuideDialog") }
                        }
                        try {
                            fm.let { infoDialog.show(it, tag) }
                        } catch (e: IllegalStateException) {
                            fm.beginTransaction().add(infoDialog, tag).commitAllowingStateLoss()
                        }
                    }
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.apply {
            joinedChallengeList.observe(viewLifecycleOwner) { list ->
                list.find { it.isStartChallenge }?.let { challenge ->
                    viewModel.userChallengeId = challenge.userChallengeId
                    viewModel.startChallengeCouponStatus.postValue(challenge.rewardCouponStatus)

                    challenge.courses.forEachIndexed { index, course ->
                        when (index) {
                            0 -> binding.mission1 = course
                            1 -> binding.mission2 = course
                            2 -> binding.mission3 = course
                            3 -> binding.mission4 = course
                        }
                    }
                }
            }
            errorAction.observe(viewLifecycleOwner) { type ->
                when (type) {
                    CoroutineExceptionType.HttpException400 -> {
                        DaebakToast.show(requireContext(), "쿠폰 발급이 완료됐어요! 쿠폰함을 확인하세요 :)")
                    }
                    else -> {}
                }
            }
        }
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }
}
