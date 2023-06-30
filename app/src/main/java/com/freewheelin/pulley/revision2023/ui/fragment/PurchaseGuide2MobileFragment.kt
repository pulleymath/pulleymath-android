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
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.legacy.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide2MobileBinding
import com.freewheelin.pulley.revision2023.ui.adapter.PurchaseGuideAdapter

class PurchaseGuide2MobileFragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide2MobileBinding
    lateinit var viewModel: PurchaseGuideViewModel
    private val guideAdapter = PurchaseGuideAdapter (guideClickListener = { selected, position ->
        binding.guideRv.smoothScrollToPosition(position)
        viewModel.updateGuides(selected)
    })

    private lateinit var getResult: ActivityResultLauncher<Intent>
    val screenHeight by lazy { DisplayUtils.getScreenHeight(requireContext()) }

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
    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootCl.layoutParams = lp
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            initReceiver()
            guideRv.apply {
                this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = guideAdapter
            }

            actionBtn.setOnClickListener { _ ->

                viewModel.selectedOffer?.let { offer ->
                    val offerId = offer.offerId
                    getResult.launch(PurchaseWebViewActivity.getIntent(requireContext(), offerId))
                }
            }
            fixedTermBtn.setOnClickListener { viewModel.step2TabIndex.postValue(0) }
            subscriptionBtn.setOnClickListener { viewModel.step2TabIndex.postValue(1) }
            compareCl.setOnClickListener {
                viewModel.setStep(3)
            }
        }

        viewModel.apply {

            originalGuides.observe(viewLifecycleOwner) {
                step2TabIndex.value?.let { index ->
                    if (index == 0) {
                        guideAdapter.submitList(it.single)
                    } else {
                        guideAdapter.submitList(it.regular.sortedByDescending { it.offerId })
                    }
                }
            }
            step2TabIndex.observe(viewLifecycleOwner) { index ->
                originalGuides.value?.let {
                    if (index == 0) {
                        guideAdapter.submitList(it.single)
                        setSelectedItem(it.single)
                    } else {
                        val list = it.regular.sortedByDescending { it.offerId }
                        guideAdapter.submitList(list)
                        setSelectedItem(list)
                    }
                }
            }
        }
    }
    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(it.resultCode) {
                purchaseSuccess -> {
                    viewModel.exitBtn()
                }
                common -> {}
            }
        }
    }
    companion object {
        const val purchaseSuccess = 200
        const val common = 400
        @JvmStatic
        fun newInstance(viewModel: PurchaseGuideViewModel) =
            PurchaseGuide2MobileFragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {

                }
            }
    }
}
