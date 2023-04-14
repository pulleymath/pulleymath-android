package com.freewheelin.pulley.revision2023.ui.dialogs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogChallengeCompletedBinding
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.ui.view.MissionStampView
import com.freewheelin.pulley.revision2023.viewmodel.ChallengeCompletedViewModel
import com.freewheelin.pulley.utils.AnimUtils


class ChallengeCompletedDialog(val challenge: Challenge, val completedCourseId: Int, val moveEvent: (course: ChallengeCourse?) -> Unit, val exitEvent: () -> Unit = {}): DialogFragment() {

    private val viewModel: ChallengeCompletedViewModel by viewModels()

    private val binding: DialogChallengeCompletedBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_challenge_completed, null, false)
    }

    init {

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setCanceledOnTouchOutside(false)
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
                    viewModel.showStampAnim.postValue(false)
                    viewModel.showStampGl.postValue(true)
                    AnimUtils.smoothAppearAnim(stampGl)
                }
            })

            stampGl.rowCount = 2
            stampGl.columnCount = 2
            stampGl.alignmentMode = GridLayout.ALIGN_BOUNDS
            challenge.courses.forEachIndexed { index, course ->
                val stampIv = getMissionStampView(index, course)
                stampGl.addView(stampIv)
            }

            viewModel.onExitClickCallback = {
                exitEvent()
                dismiss()
            }

            val nextCourse = challenge.getNextCourse(completedCourseId)
            val courseName = nextCourse?.courseName ?: "메인"
            subTitleTv.text = nextCourse?.completedSubTitle ?: "메인으로 이동해 현황을 확인해보세요 :)"
            val partText = if (nextCourse?.challengeCourseId == 3) "로" else "으로"
            nextChallengeTv.text = "${courseName}${partText} 이동하기"
            nextChallengeBtnCl.setOnClickListener {
                dismiss()
                moveEvent(nextCourse)
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