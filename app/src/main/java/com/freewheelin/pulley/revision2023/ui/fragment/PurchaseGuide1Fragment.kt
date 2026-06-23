package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide1Binding
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.visibleIf

class PurchaseGuide1Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide1Binding
    lateinit var viewModel: PurchaseGuideViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide1, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 재생성(프로세스 사망/구성 변경) 시 newInstance 주입이 누락되므로, 부모(PurchaseGuideDialog)
        // 스코프에서 공유 viewModel을 재획득한다. lateinit 미초기화 크래시 방어.
        if (!::viewModel.isInitialized) {
            viewModel = ViewModelProvider(requireParentFragment())[PurchaseGuideViewModel::class.java]
        }
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            actionBtn.setOnClickListener { _ ->
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "그랜드오픈2023", "구독제상품설명")
                viewModel.setStep(2)
            }
        }
        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
            binding.pdfDescTv.visibleIf(withPdfDesc)
        }
    }
    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootCl.layoutParams = lp
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: PurchaseGuideViewModel, withPdfDesc: Boolean) =
            PurchaseGuide1Fragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {
                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
}