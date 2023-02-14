package com.freewheelin.pulley.revision2023.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.DialogStartChallengeInfoBinding
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.viewmodel.StartChallengeInfoViewModel

class StartChallengeInfoDialog(val challengeId: Int, val startCallback: (Int) -> Unit): DialogFragment() {

    private val viewModel: StartChallengeInfoViewModel by viewModels()

    private val binding: DialogStartChallengeInfoBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_start_challenge_info, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            this.isPaidUser = user?.serviceType != PaidServiceType.NONE
            scrollRootView.isVerticalScrollBarEnabled = false

            titleTv.text = if (user?.serviceType != PaidServiceType.NONE) "스타트 챌린지에 참여해 할인 쿠폰을 받으세요!" else "스타트 챌린지에 참여해 쿠폰을 받으세요!"

            viewModel.onExitClickCallback = {
                dismiss()
            }
            startBtn.setOnClickListener {
                dismiss()
                startCallback(challengeId)
            }
        }
    }


}