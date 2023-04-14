package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogGuestJoinInduceBinding
import com.freewheelin.pulley.revision2023.ui.fragment.*
import com.freewheelin.pulley.revision2023.viewmodel.GuestJoinViewModel

class JoinInduceForGuestDialog(val dismissCallback: () -> Unit): DialogFragment() {

    private val viewModel: GuestJoinViewModel by viewModels()

    private val binding: DialogGuestJoinInduceBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_guest_join_induce, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    override fun onDismiss(dialog: DialogInterface) {
        dismissCallback()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.setStep = { setChildFragment(it) }
            viewModel.replaceStep = { replaceChildFragment(it) }
            viewModel.removeStep = { removeFragment(it) }
            setChildFragment(GuestJoinStep.가입유도)

            viewModel.onExitClickCallback = {
                dismiss()
            }



        }
    }
    fun setChildFragment(step: GuestJoinStep) {
        val frag = getFragment(step)
        sendViewModel(frag)
        moveTo(frag)
    }

    fun moveTo(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
            add(R.id.containerFl, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun replaceChildFragment(step: GuestJoinStep) {
        val frag = getFragment(step)
        sendViewModel(frag)
        replaceTo(frag)
    }
    fun replaceTo(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
//            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_right, R.anim.exit_to_left)
            setCustomAnimations(R.anim.enter_to_left, 0)
            replace(R.id.containerFl, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun removeFragment(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
            remove(frag)
            commit()
        }
    }

    enum class GuestJoinStep {
        가입유도,
        로그인,
        회원가입;
    }
    fun getFragment(step: GuestJoinStep): Fragment {
        return when (step) {
            GuestJoinStep.가입유도 -> GuestJoinIntroduceFragment.newInstance()
            GuestJoinStep.로그인 -> GuestLoginFragment.newInstance()
            GuestJoinStep.회원가입 -> GuestSignUpFragment.newInstance()
        }
    }
    fun sendViewModel(frag: Fragment) {
        (frag as? GuestJoinIntroduceFragment)?.apply {
            viewModel = this@JoinInduceForGuestDialog.viewModel
        }
        (frag as? GuestLoginFragment)?.apply {
            viewModel = this@JoinInduceForGuestDialog.viewModel
        }
        (frag as? GuestSignUpFragment)?.apply {
            viewModel = this@JoinInduceForGuestDialog.viewModel
        }
    }

}