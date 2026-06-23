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
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.legacy.utils.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PurchaseGuide3Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide3Binding
    lateinit var viewModel: PurchaseGuideViewModel
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
        // 재생성(프로세스 사망/구성 변경) 시 newInstance 주입이 누락되므로, 부모(PurchaseGuideDialog)
        // 스코프에서 공유 viewModel을 재획득한다. lateinit 미초기화 크래시 방어.
        if (!::viewModel.isInitialized) {
            viewModel = ViewModelProvider(requireParentFragment())[PurchaseGuideViewModel::class.java]
        }
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            backBtn.setOnClickListener {
                viewModel.removeStep.let { it(this@PurchaseGuide3Fragment) }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: PurchaseGuideViewModel) =
            PurchaseGuide3Fragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {

                }
            }
    }
}