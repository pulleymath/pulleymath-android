package com.freewheelin.pulley.activities.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.API.ResponseModel.mypage.SummaryPlusItem
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.utils.DateTimeUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_pulley_plus.*
import kotlinx.android.synthetic.main.fragment_my_pulley_plus.backBtn
import kotlinx.android.synthetic.main.item_my_pulley_product.*
import java.util.*

class MyPulleyPlusFragment : MyPageBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_pulley_plus, container, false)
    }

    lateinit var backBtn: ImageButton
    lateinit var btnOpenPulleyPlus: LinearLayout
    lateinit var btnShowPaidList: LinearLayout

    lateinit var freeContainer: LinearLayout
    lateinit var paidContainer: LinearLayout
    lateinit var prodNameTv: TextView
    lateinit var usePeriodTv: TextView
    lateinit var paymentLabel: TextView
    lateinit var paymentTv: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {

        freeContainer = view.findViewById(R.id.freeContainer)
        paidContainer = view.findViewById(R.id.paidContainer)

        prodNameTv = view.findViewById(R.id.prodNameTv)
        usePeriodTv = view.findViewById(R.id.usePeriodTv)
        paymentLabel = view.findViewById(R.id.paymentLabel)
        paymentTv = view.findViewById(R.id.paymentTv)

        backBtn = view.findViewById(R.id.backBtn)
        btnOpenPulleyPlus = view.findViewById(R.id.btnOpenPulleyPlus)
        btnShowPaidList = view.findViewById(R.id.btnShowPaidList)

        backBtn.setOnClickListener {
            onBackBtnClicked()
        }
        btnOpenPulleyPlus.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.풀리플러스구매)
            startActivity(intent)
        }
        btnShowPaidList.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.구매내역)
            startActivity(intent)
        }
    }

    private fun load() {
        API_APP.summaryPlus()
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
            }, { /* */ })
    }

    private fun setPaidContainer(data: SummaryPlusItem) {
        prodNameTv.text = data.detail?.title

        val start = DateTimeUtils.convertServerStr(data.detail.startedAt)
        val end = DateTimeUtils.convertServerStr(data.detail.endAt)
        usePeriodTv.text = "$start ~ $end"
        paymentLabel.text = "다음 결제 예정일"
        paymentTv.text = DateTimeUtils.convertServerStr(data.detail.nextPaymentAt)
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).moveTo(frag)
    }
}