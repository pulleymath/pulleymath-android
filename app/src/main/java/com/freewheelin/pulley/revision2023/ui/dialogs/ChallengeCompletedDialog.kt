package com.freewheelin.pulley.revision2023.ui.dialogs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogChallengeCompletedBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseInduceWebViewActivity
import com.freewheelin.pulley.revision2023.ui.view.MissionStampView
import com.freewheelin.pulley.revision2023.viewmodel.ChallengeCompletedViewModel
import com.freewheelin.pulley.legacy.utils.AnimUtils
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.utils.visibleOrInvisibleIf
import com.freewheelin.pulley.legacy.views.DaebakToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ChallengeCompletedDialog(): DialogFragment() {

    private val viewModel: ChallengeCompletedViewModel by viewModels()

    private val binding: DialogChallengeCompletedBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_challenge_completed, null, false)
    }
    var moveEvent: (course: ChallengeCourse?) -> Unit = {}
    var exitEvent: () -> Unit = {}
    companion object {
        const val COMPLETED_COURSE_ID = "COMPLETED_COURSE_ID"
        const val IS_DELAYED_SHOW_NEXT_BTN = "IS_DELAYED_SHOW_NEXT_BTN"
        const val CHALLENGE = "CHALLENGE"
        fun newInstance(challenge: Challenge, completedCourseId: Int, isDelayedShowNextBtn: Boolean = false): ChallengeCompletedDialog {
            val args = Bundle().apply {
                putInt(COMPLETED_COURSE_ID, completedCourseId)
                putBoolean(IS_DELAYED_SHOW_NEXT_BTN, isDelayedShowNextBtn)
                putSerializable(CHALLENGE, challenge)
            }
            val instance = ChallengeCompletedDialog()
            instance.arguments = args
            return instance
        }
    }

    init {

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setCanceledOnTouchOutside(false)
        arguments?.apply {
            viewModel.completedCourseId = getInt(COMPLETED_COURSE_ID)
            viewModel.challenge = getSerializable(CHALLENGE) as Challenge
            viewModel.isDelayedShowNextBtn = getBoolean(IS_DELAYED_SHOW_NEXT_BTN)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.getChallengeCompleteInfo()
            scrollRootView.isVerticalScrollBarEnabled = true
            stampLottie.playAnimation()
            stampLottie.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (viewModel.isDelayedShowNextBtn) {
                        val intent = PurchaseInduceWebViewActivity.getIntent(requireContext())
                        startActivity(intent)
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(200)
                            viewModel.showStampAnim.postValue(false)
                            viewModel.showStampGl.postValue(true)
                            AnimUtils.smoothAppearAnim(stampGl)
                        }
                    } else {
                        viewModel.showStampAnim.postValue(false)
                        viewModel.showStampGl.postValue(true)
                        AnimUtils.smoothAppearAnim(stampGl)
                    }
                }
            })

            stampGl.rowCount = 2
            stampGl.columnCount = 2
            stampGl.alignmentMode = GridLayout.ALIGN_BOUNDS
            viewModel.challenge.courses.forEachIndexed { index, course ->
                val stampIv = MissionStampView.getMissionStampView(requireContext(), index, course)
                stampGl.addView(stampIv)
            }

            viewModel.onExitClickCallback = {
                exitEvent()
                dismiss()
            }

            val nextCourse = viewModel.challenge.getNextCourse(viewModel.completedCourseId)
            if (nextCourse == null) {
                subTitleTv.text = "모든 미션을 성공하면 50% 할인쿠폰을 드려요 :)"
                nextChallengeTv.text = "쿠폰 발급받기"
                println("couponEnter : 0")
                nextChallengeBtnCl.setOnClickListener {
                    println("couponEnter : 1")
                    println("couponEnter : viewModel.challenge.userChallengeId : ${viewModel.challenge.userChallengeId}")
                    viewModel.challenge.userChallengeId?.let { userChallengeId ->

                        viewModel.askForRedeemOfChallenge(userChallengeId) {
                            dismiss()
                            println("couponEnter : 2 : user?.serviceType : ${user?.serviceType}")
                            if (user?.serviceType?.isNoneUser == true) {
                                println("couponEnter : 3")



                                if (requireActivity() is AppCompatActivity && !requireActivity().isFinishing) {
                                    println("couponEnter : 4")
                                    val fm = requireActivity().supportFragmentManager
                                    if (!fm.isDestroyed) {
                                        println("couponEnter : 5")
                                        val infoDialog = StartChallengeInfoDialog.newInstance(viewModel.challenge.challengeId, true)
                                        infoDialog.startCallback = {
                                            val pgDialog = PurchaseGuideDialog.newInstance(2)
                                            try {
                                                println("couponEnter : 6")
                                                fm.let { pgDialog.show(it, "purchaseGuideDialog") }
                                            } catch (e: IllegalStateException) {
                                                fm.beginTransaction().add(pgDialog, "purchaseGuideDialog")
                                                    .commitAllowingStateLoss()
                                            }

                                        }
                                        try {
                                            println("couponEnter : 6")
                                            fm.let { infoDialog.show(it, tag) }
                                        } catch (e: IllegalStateException) {
                                            fm.beginTransaction().add(infoDialog, tag)
                                                .commitAllowingStateLoss()
                                        }
                                    }
                                }



//
//                                val dialog = StartChallengeInfoDialog.newInstance(viewModel.challenge.challengeId, true)
//                                dialog.startCallback = { _ ->
//                                    val pgDialog = PurchaseGuideDialog.newInstance(2)
//                                    childFragmentManager.let { pgDialog.show(it, "purchaseGuideDialog") }
//                                }
//                                childFragmentManager.let { dialog.show(it, "StartChallengeEndInfoDialog") }








                            } else {
                                println("couponEnter : 7")
                                DaebakToast.show(requireContext(), "쿠폰 발급이 완료됐어요! 마이페이지에서 쿠폰함을 확인하세요 :)")
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                println("couponEnter : 8")
                                delay(500)
                                dismiss()
                                println("couponEnter : 9")
                            }
                        }
                    }
                }

            } else {
                val courseName = nextCourse.courseName
                subTitleTv.text = nextCourse.completedSubTitle
                val partText = if (nextCourse.challengeCourseId == 3) "로" else "으로"
                nextChallengeTv.text = "${courseName}${partText} 이동하기"
                nextChallengeBtnCl.setOnClickListener {
                    dismiss()
                    moveEvent(nextCourse)
                }
            }
        }
    }

    fun getMissionStampView(index: Int, course: ChallengeCourse): MissionStampView {
        return MissionStampView(requireContext(), course).apply {
            val currentCol: Int = index % 2
            val currentRow: Int = index / 2
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(currentCol, 1, 1f)
                rowSpec = GridLayout.spec(currentRow, 1, 1f)
            }
        }
    }
}