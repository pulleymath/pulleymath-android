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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide2Binding
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.revision2023.ui.adapter.PurchaseGuideAdapter

class PurchaseGuide2Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide2Binding
    var viewModel: PurchaseGuideViewModel? = null
    private val guideAdapter = PurchaseGuideAdapter (guideImageClickListener = { selected ->
        viewModel?.updateGuides(selected)
    },  compareTextClickListener = {
        viewModel?.setStep?.let { it(3) }
    })
    private lateinit var getResult: ActivityResultLauncher<Intent>
    val screenHeight by lazy { DisplayUtils.getScreenHeight(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initActivityResult()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide2, container, false)
        return binding.root
    }

    fun initReceiver() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            initReceiver()
            guideRv.apply {
                this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = guideAdapter
            }

            actionBtnWrapperCl.setOnClickListener { _ ->
//                 TODO to webview 가능하면 웹뷰로 하고 안되면 크롬으로 넘기

                viewModel?.selectedOfferId?.value?.let { offerId ->
                    getResult.launch(PurchaseWebViewActivity.getIntent(requireContext(), offerId))
                }
            }
        }

        viewModel?.apply {
            guideOffers.observe(viewLifecycleOwner) {
                val tempHeader = it[0]
                val listIncludeHeader = listOf(tempHeader) + it
                guideAdapter.submitList(listIncludeHeader)
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
        Glide.with(requireContext())
            .load(if (isSelectedImg) offer.selectedImageUrl else offer.commonImageUrl)
            .into(view)
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
            PurchaseGuide2Fragment().apply {
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
