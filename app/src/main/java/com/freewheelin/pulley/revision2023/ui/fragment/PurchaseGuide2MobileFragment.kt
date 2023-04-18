package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide2MobileBinding

class PurchaseGuide2MobileFragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide2MobileBinding
    var viewModel: PurchaseGuideViewModel? = null

    private lateinit var getResult: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initActivityResult()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide2_mobile, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun initReceiver() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            initReceiver()
            step3Tv.text = step3Tv.text
                .partialUnderline(0, 6)
                .partialFontAndColored(Theme.bold(requireContext()), ContextCompat.getColor(requireContext(), R.color.gray_800), 0, 6)

            actionBtnWrapperCl.setOnClickListener { _ ->
//                 TODO to webview 가능하면 웹뷰로 하고 안되면 크롬으로 넘기

                viewModel?.selectedOfferId?.value?.let { offerId ->
                    viewModel?.getTempToken {
                        getResult.launch(PurchaseWebViewActivity.getIntent(requireContext(), offerId))
                    }
                }
                step3Tv.setOnClickListener {
                    viewModel?.setStep?.let { it(3) }
                }
            }
        }

        viewModel?.apply {

            guideOffers.observe(viewLifecycleOwner) {
                binding.apply {
                    println("asoaso guideLl.childCount :${guideLl.childCount}")
                    if (guideLl.childCount > 0) {
//                        it.forEach { offer ->
//                            guideLl.children
//                                .filter { (it as GuideImageView).offerId == offer.offerId }
//                                .forEach {
//                                    (it as GuideImageView).let { view ->
//                                        view.visibleIf(offer.isSelected == view.isSelectedImage)
//                                    }
//                                }
//                        }
                    } else {
                        guideLl.removeAllViews()
                        it.forEach {
                            val commonIv = createOfferIv(it, false)
                            guideLl.addView(commonIv)
                            val selectedIv = createOfferIv(it, true)
                            guideLl.addView(selectedIv)
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            delay(100)
                            guideLl.children.forEach {
                                (it as GuideImageView).let {
                                    it.visibleIf(!it.isSelectedImage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun createOfferIv (offer: PurchaseGuideOffer, isSelectedImg: Boolean): GuideImageView = GuideImageView(requireContext()).also { view ->
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        view.offerId = offer.offerId
        view.isSelectedImage = isSelectedImg
        view.layoutParams = params
        view.adjustViewBounds = true
        view.setMarginBottom(dp = 10)
//        Glide.with(requireContext())
//            .load(if (isSelectedImg) offer.selectedImageUrl else offer.commonImageUrl)
//            .into(view)
        view.setOnClickListener { _ ->
            viewModel?.updateGuides(offer)
        }
    }
    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(it.resultCode) {
                purchaseSuccess -> {
                    viewModel?.exitBtn()
                }
                common -> {}
            }
        }
    }
    companion object {
        const val purchaseSuccess = 200
        const val common = 400
        @JvmStatic
        fun newInstance() =
            PurchaseGuide2MobileFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    inner class GuideImageView: androidx.appcompat.widget.AppCompatImageView {
        constructor(context: Context): super(context)
        constructor(context: Context, attrs: AttributeSet): super(context, attrs)
        var isSelectedImage = false
        var offerId = 0
    }
}
