package com.freewheelin.pulley.activities.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.core.API.ResponseModel.mypage.SummaryBooksItem
import com.freewheelin.pulley.core.API.ResponseModel.mypage.SummaryCouponItem
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.coupon.NewCoupon
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.views.Buttons.PrimaryButton
import com.freewheelin.pulley.views.CodeConfirmView
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.SuccessToast
import com.google.gson.Gson
import com.pulleymath.android.pdf.draw.Line
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MyPulleyCouponFragment : MyPageBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_pulley_coupon, container, false)
    }

    lateinit var backBtn: ImageButton
    lateinit var couponEt: EditText
    lateinit var registBtn: PrimaryButton
    lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {

        backBtn = view.findViewById(R.id.backBtn)
        couponEt = view.findViewById(R.id.couponEt)
        registBtn = view.findViewById(R.id.registBtn)
        recyclerView = view.findViewById(R.id.recyclerView)

        backBtn.setOnClickListener {
            onBackBtnClicked()
        }

        registBtn.setOnClickListener {
            val number = couponEt.text.toString()
            Log.d(javaClass.simpleName, "number=$number")
            if(number.isNotEmpty()) {
                val newCoupon = NewCoupon(number)
                registBtn.toProcessingUI()
                API_APP.addCoupon(newCoupon)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Log.d(javaClass.simpleName, "$result")
                        if (result.error != null) {
                            val msg = if(result.error == "NOT_FOUND_DATA") {
                                "쿠폰이 존재하지 않습니다, 쿠폰 코드를 확인해주세요."
                            } else {
                                "쿠폰을 사용할 수 없습니다."
                            }
                            DialogUtils.confirmDialog(requireContext(), "확인", msg)
                        } else {
                            DaebakToast.show(requireContext(), "쿠폰이 등록되었습니다.")
                            couponEt.setText("")
                            load()
                        }
                        registBtn.toEnableUI()
                    }, { throwable ->
                        val msg = if (throwable is HttpException) {
                            val result = Gson().fromJson(throwable.response()?.errorBody()?.string(), ResponseBody::class.java)
                            if(result.error == "NOT_FOUND_DATA") {
                                "쿠폰이 존재하지 않습니다, 쿠폰 코드를 확인해주세요."
                            } else {
                                "쿠폰을 사용할 수 없습니다."
                            }
                        } else {
                            "알수 없는 오류가 발생하였습니다. 다시 시도하세요!"
                        }
                        DialogUtils.confirmDialog(requireContext(), "확인", msg)
                        registBtn.toEnableUI()
                    })
            }
        }
    }

    private fun load() {
        API_APP.summaryCoupon()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d(javaClass.simpleName, "$result")
                setList(result.data!!.sortedByDescending { it.couponDetailID })
            }, {

            })
    }

    lateinit var adapter: CouponAdapter
    private fun setList(list: List<SummaryCouponItem>) {
        adapter = CouponAdapter(list.toMutableList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun removeItemInList(position: Int) {
        adapter?.list.removeAt(position)
        adapter?.notifyItemRemoved(position)
        DaebakToast.show(requireContext(), "쿠폰이 사용되었습니다")
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).moveTo(frag)
    }

    inner class CouponAdapter(val list: MutableList<SummaryCouponItem>): RecyclerView.Adapter<CouponAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_pulley_coupon, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.set(list.get(position))
        }

        override fun getItemCount() = list.size

        inner class Holder(val view:View): RecyclerView.ViewHolder(view) {

            lateinit var item:SummaryCouponItem
            var textName: TextView
            var textBenefit: TextView
            var textPeriod: TextView
            var useBtn: ConstraintLayout
            var useBtnText: TextView
            var useBtnButton: Button
            var useBtnProgress: FrameLayout

            init {
                textName = view.findViewById(R.id.textName)
                textBenefit = view.findViewById(R.id.textBenefit)
                textPeriod = view.findViewById(R.id.textPeriod)
                useBtn = view.findViewById(R.id.useBtn)
                useBtnText = view.findViewById(R.id.useBtnText)
                useBtnButton = view.findViewById(R.id.useBtnButton)
                useBtnProgress = view.findViewById(R.id.useBtnProgress)

                useBtn.setOnClickListener { useCoupon() }
                useBtnButton.setOnClickListener { useCoupon() }
            }

            fun useCoupon() {
                if(item.canUse()) {
                    useBtnProgress.visibility = View.VISIBLE
                    API_APP.useCoupon(item.couponDetailID)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            Log.d(javaClass.simpleName, "$result")
                            useBtnProgress.visibility = View.GONE
                            removeItemInList(adapterPosition)

                            // 쿠폰적용을 위해 액티비티 재시작
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = requireActivity().intent
                                requireActivity().finish()
                                startActivity(intent)
                            }, 1500)
                        }, {
                            DialogUtils.confirmDialog(requireContext(), "확인", "쿠폰을 사용할 수 없습니다.")
                            useBtnProgress.visibility = View.GONE
                        })
                }
            }

            fun set(item: SummaryCouponItem) {
                this.item = item
                textName.text = item.couponCampaignTitle
                textBenefit.text = item.description

                val end = DateTimeUtils.convertServerStr(item.endAt)
                textPeriod.text = end

                if(item.canUse()) {
                    useBtnText.visibility = View.GONE
                    useBtnButton.visibility = View.VISIBLE
                } else {
                    useBtnText.visibility = View.VISIBLE
                    useBtnButton.visibility = View.GONE
                }
            }
        }
    }
}