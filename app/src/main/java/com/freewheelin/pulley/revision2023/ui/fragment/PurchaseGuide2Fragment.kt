package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide2Binding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity
import com.freewheelin.pulley.revision2023.ui.adapter.PurchaseGuideAdapter
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideDialogViewModel
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.utils.partialFontAndColored
import com.freewheelin.pulley.utils.partialUnderline
import com.freewheelin.pulley.utils.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class PurchaseGuide2Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide2Binding
    var viewModel: PurchaseGuideDialogViewModel? = null
    private val guideAdapter = PurchaseGuideAdapter { selected ->
        viewModel?.updateGuides(selected)

    }
    private lateinit var getResult: ActivityResultLauncher<Intent>

    override fun onResume() {
        super.onResume()
        binding.apply {
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(200)
//                val bottomMargin = 24.toPx()
//                val step3TvMarginTop = 2.toPx()
//                val step3TvHeight = step3Tv.height
//                val rvHeight = guideRv.height
//                val totalHeight = step3TvMarginTop + step3TvHeight + rvHeight + bottomMargin
//                scrollRoot.layoutParams.height = min(totalHeight, 420.toPx())
//            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initActivityResult()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide2, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            step3Tv.text = step3Tv.text
                .partialUnderline(0, 6)
                .partialFontAndColored(Theme.bold(requireContext()), ContextCompat.getColor(requireContext(), R.color.gray_800), 0, 6)
            guideRv.apply {
                this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = guideAdapter
            }

            actionBtnWrapperCl.setOnClickListener { _ ->
//                 TODO to webview 가능하면 웹뷰로 하고 안되면 크롬으로 넘기
//                startActivity(PurchaseWebViewActivity.getIntent(requireContext()))

                viewModel?.selectedOfferId?.value?.let { offerId ->
                    viewModel?.getTempToken {
                        val token = it
                        val encodedUri = "/shop/${offerId}/plus"
                        val url = "${Network.purchaseSubscriptionUrl}${token}&uri=${encodedUri}"
                        println("asoaso url ${url}")
                        IntentUtils.openWebLink(requireContext(), url, (activity as AppCompatActivity).packageManager)
                    }
                }
            }
            step3Tv.setOnClickListener {
                viewModel?.setStep?.let { it(3) }
            }
        }
        viewModel?.apply {

            guideOffers.observe(viewLifecycleOwner) {
                guideAdapter.submitList(it)
            }
        }
    }

    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(it.resultCode) {
                success -> {
                // TODO app user refrash
                    viewModel?.exitBtn()
                }
                common -> {}
            }
        }
    }
    companion object {
        const val success = 200
        const val common = 400
        @JvmStatic
        fun newInstance() =
            PurchaseGuide2Fragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}