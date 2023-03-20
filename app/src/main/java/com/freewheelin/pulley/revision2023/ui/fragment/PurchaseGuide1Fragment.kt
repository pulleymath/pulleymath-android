package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide1Binding
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseGuideActivity
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.utils.visibleIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class PurchaseGuide1Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide1Binding
    var viewModel: PurchaseGuideViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide1, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                val titleMarginTop = 24.toPx()
                val titleHeight = titleTv.height
                val imageHeight = guideIv.height
                val totalHeight = titleMarginTop + titleHeight + imageHeight
                scrollRoot.layoutParams.height = min(totalHeight, 450.toPx())
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            actionBtnWrapperCl.setOnClickListener { _ ->
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "그랜드오픈2023", "구독제상품설명")
                // TODO new page
                viewModel?.exitBtn()
                startActivity(PurchaseGuideActivity.getIntent(requireContext()))
            }
        }
        arguments?.let {
            val withPdfDesc = it.getBoolean("PDF_PURCHASE_DESC")
            binding.pdfDescTv.visibleIf(withPdfDesc)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(withPdfDesc: Boolean) =
            PurchaseGuide1Fragment().apply {
                arguments = Bundle().apply {
                    putBoolean("PDF_PURCHASE_DESC", withPdfDesc)
                }
            }
    }
}