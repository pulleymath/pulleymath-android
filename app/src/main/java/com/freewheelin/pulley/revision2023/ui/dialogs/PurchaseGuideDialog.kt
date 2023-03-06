package com.freewheelin.pulley.revision2023.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogPurchaseGuideBinding
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide1Fragment
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide2Fragment
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide3Fragment
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideDialogViewModel

class PurchaseGuideDialog(val step: Int = 1, val withPdfDesc: Boolean = false, val startCallback: () -> Unit = {}): DialogFragment() {

    private val viewModel: PurchaseGuideDialogViewModel by viewModels()

    private val binding: DialogPurchaseGuideBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_purchase_guide, null, false)
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

            viewModel.fetchGuides()
            viewModel.setStep = { setChildFragment(it) }
            viewModel.replaceStep = { replaceChildFragment(it) }
            viewModel.removeStep = { removeFragment(it) }
            setChildFragment(step)

            viewModel.onExitClickCallback = {
                dismiss()
            }

        }
    }

    fun setChildFragment(step: Int) {
        val frag = when (step) {
            1 -> { PurchaseGuide1Fragment.newInstance(withPdfDesc) }
            2 -> { PurchaseGuide2Fragment.newInstance() }
            3 -> { PurchaseGuide3Fragment.newInstance() }
            else -> { PurchaseGuide1Fragment.newInstance(withPdfDesc) }
        }
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
    fun replaceChildFragment(step: Int) {
        val frag = when (step) {
            1 -> { PurchaseGuide1Fragment.newInstance(withPdfDesc) }
            2 -> { PurchaseGuide2Fragment.newInstance() }
            3 -> { PurchaseGuide3Fragment.newInstance() }
            else -> { PurchaseGuide1Fragment.newInstance(withPdfDesc) }
        }
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


    fun sendViewModel(frag: Fragment) {
        (frag as? PurchaseGuide1Fragment)?.apply {
            viewModel = this@PurchaseGuideDialog.viewModel
        }
        (frag as? PurchaseGuide2Fragment)?.apply {
            viewModel = this@PurchaseGuideDialog.viewModel
        }
        (frag as? PurchaseGuide3Fragment)?.apply {
            viewModel = this@PurchaseGuideDialog.viewModel
        }
    }

    fun removeFragment(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
            remove(frag)
            commit()
        }
    }


}