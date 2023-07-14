package com.freewheelin.pulley.revision2023.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.DialogStartChallengeInfoBinding
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.viewmodel.StartChallengeInfoViewModel

class StartChallengeInfoDialog(): DialogFragment() {

    private val viewModel: StartChallengeInfoViewModel by viewModels()

    private val binding: DialogStartChallengeInfoBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_start_challenge_info, null, false)
    }
    var startCallback: (Int) -> Unit = {}
    companion object {
        const val CHALLENGE_ID = "CHALLENGE_ID"
        const val IS_CHALLENGE_FINISHED = "IS_CHALLENGE_FINISHED"
        fun newInstance(challengeId: Int, isChallengeFinished: Boolean = false): StartChallengeInfoDialog {
            val args = Bundle().apply {
                putInt(CHALLENGE_ID, challengeId)
                putBoolean(IS_CHALLENGE_FINISHED, isChallengeFinished)
            }
            val instance = StartChallengeInfoDialog()
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {
            viewModel.isChallengeFinished = getBoolean(IS_CHALLENGE_FINISHED)
            viewModel.challengeId = getInt(CHALLENGE_ID)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            showCompletedView = viewModel.isChallengeFinished
            this.isPaidUser = user?.serviceType != PaidServiceType.NONE
            scrollRootView.isVerticalScrollBarEnabled = false

            viewModel.onExitClickCallback = {
                dismiss()
            }
            startBtn.setOnClickListener {
                dismiss()
                startCallback(viewModel.challengeId)
            }
        }
    }


}