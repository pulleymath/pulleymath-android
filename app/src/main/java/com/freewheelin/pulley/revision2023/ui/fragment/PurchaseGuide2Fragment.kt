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
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide2Binding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseWebViewActivity
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.adapter.PurchaseGuideAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PurchaseGuide2Fragment : Fragment() {
    private lateinit var binding: FragmentPurchaseGuide2Binding
    lateinit var viewModel: PurchaseGuideViewModel
    private val guideAdapter = PurchaseGuideAdapter (guideClickListener = { selected, position ->
        binding.guideRv.smoothScrollToPosition(position)
        viewModel.updateGuides(selected)
    })
    private lateinit var getResult: ActivityResultLauncher<Intent>
    val screenHeight by lazy { DisplayUtils.getScreenHeight(requireContext()) }
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initActivityResult()
        firebaseAnalytics = Firebase.analytics
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_guide2, container, false)
        return binding.root
    }

    fun initReceiver() {

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
            initReceiver()
            guideRv.apply {
                this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = guideAdapter
            }

            actionBtn.setOnClickListener { _ ->
                LogUtils.logEvent(requireContext(),
                    user,
                    PulleyEvent.BUTTON_CLICK,
                    "go_purchase_btn",
                    "구매유도",
                    "offerId_${viewModel.selectedOffer?.offerId}"
                ) {
                    viewModel.selectedOffer?.let { offer ->
                        val offerId = offer.offerId
                        getResult.launch(PurchaseWebViewActivity.getIntent(requireContext(), offerId))
                    }
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
                purchaseEnabled.postValue(false)
                originalGuides.value?.let {
                    it.single.forEach { it.isSelected.set(false) }
                    it.regular.forEach { it.isSelected.set(false) }
                    val list = if (index == 0) {
                        it.single
                    } else {
                        it.regular.sortedByDescending { it.offerId }
                    }
                    guideAdapter.submitList(list)
                }
            }
        }
    }
    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootCl.layoutParams = lp
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
            PurchaseGuide2Fragment().apply {
                this.viewModel = viewModel
                arguments = Bundle().apply {

                }
            }
    }
}
