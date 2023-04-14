package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogChallengeInduceBinding
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.ui.fragment.*
import com.freewheelin.pulley.revision2023.viewmodel.ChallengeInduceViewModel

class ChallengeInduceDialog(val type: Type, val course: ChallengeCourse? = null, val nextEvent: () -> Unit = {}, val moveEvent: (ChallengeCourse?) -> Unit = {}, val exitEvent: () -> Unit = {}): DialogFragment() {

    enum class Type {
        OneMore, Disappointed
    }
    private val viewModel: ChallengeInduceViewModel by viewModels()

    private val binding: DialogChallengeInduceBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_challenge_induce, null, false)
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

            viewModel.induceType.postValue(type)
            viewModel.onExitClickCallback = {
                exitEvent()
                dismiss()
            }

            continueBtn.setOnClickListener {
                dismiss()
                nextEvent()
                moveEvent(course)
            }

            viewModel.induceType.observe(viewLifecycleOwner) { type ->
                when (type) {
                    Type.OneMore -> {
                        viewModel.title.postValue("얼마 안 남았어!\n1분만 더 하고 할인 쿠폰 받아봐")
                    }
                    Type.Disappointed -> {
                        viewModel.title.postValue("벌써 가려고?\n챌린지 1분이면 끝나는데, 50% 쿠폰 챙겨가!")
                    }
                }
            }
        }
    }
}