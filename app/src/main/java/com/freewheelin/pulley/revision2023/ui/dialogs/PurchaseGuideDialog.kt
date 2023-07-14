package com.freewheelin.pulley.revision2023.ui.dialogs

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.DialogPurchaseGuideBinding
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide1Fragment
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide2Fragment
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide2MobileFragment
import com.freewheelin.pulley.revision2023.ui.fragment.PurchaseGuide3Fragment
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.toPx

class PurchaseGuideDialog(): DialogFragment() {

    private val viewModel: PurchaseGuideViewModel by viewModels()

    private val binding: DialogPurchaseGuideBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_purchase_guide, null, false)
    }

    companion object {
        const val DIALOG_STEP = "DIALOG_STEP"
        const val WITH_PDF_DESC = "WITH_PDF_DESC"
        fun newInstance(
            step: Int = 1,
            withPdfDesc: Boolean = false
        ): PurchaseGuideDialog {
            val args = Bundle().apply {
                putInt(DIALOG_STEP, step)
                putBoolean(WITH_PDF_DESC, withPdfDesc)
            }
            val instance = PurchaseGuideDialog()
            instance.arguments = args
            return instance
        }
    }

    var step = 0
    var withPdfDesc = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let { bd ->
            step = bd.getInt(DIALOG_STEP)
            withPdfDesc = bd.getBoolean(WITH_PDF_DESC)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            viewModel.fetchGuides()
            viewModel.setStep = { setChildFragment(it) }
            viewModel.removeStep = { removeFragment(it) }
            setChildFragment(step)

            viewModel.onExitClickCallback = {
                dismiss()
            }

        }
    }
    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        lp.width = 440.toPx()
        binding.rootCl.layoutParams = lp
    }
    fun setChildFragment(step: Int) {
        val frag = getFragment(step)
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

    fun sendViewModel(frag: Fragment) {
        (frag as? PurchaseGuide1Fragment)?.apply {
            viewModel = this@PurchaseGuideDialog.viewModel
        }
        (frag as? PurchaseGuide2Fragment)?.apply {
            viewModel = this@PurchaseGuideDialog.viewModel
        }
        (frag as? PurchaseGuide2MobileFragment)?.apply {
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

    fun getFragment(step: Int): Fragment {
        return when (step) {
            1 -> { PurchaseGuide1Fragment.newInstance(viewModel, withPdfDesc) }
            2 -> {
                if (requireContext().isTablet) {
                    PurchaseGuide2Fragment.newInstance(viewModel)
                } else {
                    PurchaseGuide2MobileFragment.newInstance(viewModel)
                }
            }
            3 -> { PurchaseGuide3Fragment.newInstance(viewModel) }
            else -> { PurchaseGuide1Fragment.newInstance(viewModel, withPdfDesc) }
        }
    }


}