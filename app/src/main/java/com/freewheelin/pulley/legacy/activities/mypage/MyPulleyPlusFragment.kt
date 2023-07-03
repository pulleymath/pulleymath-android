package com.freewheelin.pulley.legacy.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage.SummaryPlusItem
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.databinding.FragmentMyPulleyPlusBinding
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.IntentUtils
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers

class MyPulleyPlusFragment : MyPageBaseFragment() {
    lateinit var binding: FragmentMyPulleyPlusBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_pulley_plus, container, false)
        return binding.root
    }
    private val compositeDisposable = CompositeDisposable()
    private val viewModel: MyMainPageFragViewModel by viewModels()

    lateinit var backBtn: ImageButton
    lateinit var btnOpenPulleyPlus: LinearLayout
    lateinit var btnShowPaidList: LinearLayout

    lateinit var freeContainer: LinearLayout
    lateinit var paidContainer: LinearLayout
    lateinit var prodNameTv: TextView
    lateinit var usePeriodTv: TextView
    lateinit var paymentLabel: TextView
    lateinit var paymentTv: TextView
    lateinit var nextPayDateContainer: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            isGuestUser = user?.serviceType?.isGuestUser == true
        }
        freeContainer = view.findViewById(R.id.freeContainer)
        paidContainer = view.findViewById(R.id.paidContainer)

        prodNameTv = view.findViewById(R.id.prodNameTv)
        usePeriodTv = view.findViewById(R.id.usePeriodTv)
        paymentLabel = view.findViewById(R.id.paymentLabel)
        paymentTv = view.findViewById(R.id.paymentTv)

        nextPayDateContainer = view.findViewById(R.id.nextPayDateContainer)

        backBtn = view.findViewById(R.id.backBtn)
        btnOpenPulleyPlus = view.findViewById(R.id.btnOpenPulleyPlus)
        btnShowPaidList = view.findViewById(R.id.btnShowPaidList)

        backBtn.setOnClickListener {
            onBackBtnClicked()
        }
        btnOpenPulleyPlus.setOnClickListener {
            if (user?.serviceType?.isGuestUser == true) {
                val targetUrl = URL.풀리플러스구매
                IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
            } else {
                viewModel.getTempToken { shortToken ->
                    val relativeUrl = URL.풀리플러스구매.substringAfter("https://pulleymath.com")
                    val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                }
            }
        }
        btnShowPaidList.setOnClickListener {
            if (user?.serviceType?.isGuestUser == true) {
                val targetUrl = URL.구매내역
                IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
            } else {
                viewModel.getTempToken { shortToken ->
                    val relativeUrl = URL.구매내역.substringAfter("https://pulleymath.com")
                    val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                }
            }
        }
    }

    private fun load() {
        compositeDisposable += API_APP.summaryPlus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d(javaClass.simpleName, "$result")
                if(result.data?.detail == null) { //
                    freeContainer.visibility = View.VISIBLE
                    paidContainer.visibility = View.GONE
                } else {
                    freeContainer.visibility = View.GONE
                    paidContainer.visibility = View.VISIBLE
                    setPaidContainer(result.data!!)
                }
            }, { })
    }

    private fun setPaidContainer(data: SummaryPlusItem) {
        prodNameTv.text = data.detail.title

        val start = DateTimeUtils.convertServerStr(data.detail.startedAt)
        val end = DateTimeUtils.convertServerStr(data.detail.endAt)
        usePeriodTv.text = "$start ~ $end"

        if(data.detail.nextPaymentAt != null) {
            paymentLabel.text = "다음 결제 예정일"
            paymentTv.text = DateTimeUtils.convertServerStr(data.detail.nextPaymentAt)
        } else {
            nextPayDateContainer.visibility = View.GONE
        }
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}