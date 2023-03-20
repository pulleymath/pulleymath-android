package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide3Binding
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PurchaseGuide3Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide3Binding
    var viewModel: PurchaseGuideViewModel? = null
//    var setStep: ((Int) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                val marginSize = 24.toPx()
                val guideIvHeight = guideIv.height
                val totalHeight = marginSize + guideIvHeight + marginSize
//                scrollRoot.layoutParams.height = min(totalHeight, 450.toPx())
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide3, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            backBtn.setOnClickListener {
                viewModel?.removeStep?.let { it(this@PurchaseGuide3Fragment) }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PurchaseGuide3Fragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}