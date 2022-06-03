package com.freewheelin.pulley.activities.mypage

import android.content.ActivityNotFoundException
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.API.ResponseModel.mypage.SummaryLessonItem
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyPulleyLessonFragment : MyPageBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_pulley_lesson, container, false)
    }

    lateinit var backBtn: ImageButton
    lateinit var btnOpenPulleyLesson: LinearLayout
    lateinit var btnShowPaidList: LinearLayout

    lateinit var freeContainer: LinearLayout
    lateinit var paidContainer: LinearLayout
    lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {

        backBtn = view.findViewById(R.id.backBtn)
        btnOpenPulleyLesson = view.findViewById(R.id.btnOpenPulleyLesson)
        btnShowPaidList = view.findViewById(R.id.btnShowPaidList)

        freeContainer = view.findViewById(R.id.freeContainer)
        paidContainer = view.findViewById(R.id.paidContainer)
        recyclerView = view.findViewById(R.id.recyclerView)

        backBtn.setOnClickListener {
            onBackBtnClicked()
        }
        btnOpenPulleyLesson.setOnClickListener {
            IntentUtils.openWebLink(requireContext(), URL.풀리과외구매, requireContext().packageManager)
        }
        btnShowPaidList.setOnClickListener {
            IntentUtils.openWebLink(requireContext(), URL.구매내역, requireContext().packageManager)
        }
    }

    private fun load() {
        API_APP.summaryLesson()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d(javaClass.simpleName, "$result")
                if(result.data.isEmpty()) { //
                    freeContainer.visibility = View.VISIBLE
                    paidContainer.visibility = View.GONE
                } else {
                    freeContainer.visibility = View.GONE
                    paidContainer.visibility = View.VISIBLE
                    val sortedList = result.data!!.sortedByDescending { it.detail.startedAt }
                    setList(sortedList)
                }
            }, {

            })
    }

    private fun setList(list: List<SummaryLessonItem>) {
        val adapter = LessonAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).moveTo(frag)
    }

    class LessonAdapter(val list: List<SummaryLessonItem>): RecyclerView.Adapter<LessonAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_pulley_product, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.set(list.get(position))
        }

        override fun getItemCount() = list.size

        class Holder(val view:View): RecyclerView.ViewHolder(view) {

            var prodNameTv: TextView
            var usePeriodTv: TextView
            var paymentLabel: TextView
            var paymentTv: TextView
            var nextPayDateContainer: LinearLayout

            init {
                prodNameTv = view.findViewById(R.id.prodNameTv)
                usePeriodTv = view.findViewById(R.id.usePeriodTv)
                paymentLabel = view.findViewById(R.id.paymentLabel)
                paymentTv = view.findViewById(R.id.paymentTv)
                nextPayDateContainer = view.findViewById(R.id.nextPayDateContainer)
            }

            fun set(item: SummaryLessonItem) {
                prodNameTv.text = item.title

                if(item.detail == null) {
                    nextPayDateContainer.visibility = View.GONE
                } else {
                    nextPayDateContainer.visibility = View.VISIBLE
                    paymentTv.text = DateTimeUtils.convertServerStr(item.detail.nextPaymentAt)
                }

                val period = if (item.isWait || item.detail == null) {
                    "미정"
                } else {
                    val start = DateTimeUtils.convertServerStr(item.detail.startedAt)
                    val end = DateTimeUtils.convertServerStr(item.detail.endAt)
                    "$start ~ $end"
                }
                usePeriodTv.text = period
            }
        }
    }
}