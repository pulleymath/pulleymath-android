package com.freewheelin.pulley.legacy.activities.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage.CouponItem
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.databinding.FragmentMyPulleyCouponBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.coupon.NewCoupon
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MyPulleyCouponFragment : MyPageBaseFragment() {
    lateinit var binding: FragmentMyPulleyCouponBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_pulley_coupon, container, false)
        return binding.root
    }
    private val compositeDisposable = CompositeDisposable()
    val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {
        with(binding) {
            backBtn.setOnClickListener {
                onBackBtnClicked()
            }

            registBtn.setOnClickListener {
                val number = couponEt.text.toString()
                Log.d(javaClass.simpleName, "number=$number")
                if(number.isNotEmpty()) {
                    val newCoupon = NewCoupon(number)
                    registBtn.setLoading(true)
                    compositeDisposable += API_APP.addCoupon(newCoupon)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            Log.d(javaClass.simpleName, "$result")
                            DaebakToast.show(requireContext(), "쿠폰이 등록되었습니다.")
                            couponEt.setText("")
                            load()
                            registBtn.isEnabled = true
                            registBtn.setLoading(false)
                        }, { throwable ->

                            var msg = if (throwable is HttpException) {
                                val result = Gson().fromJson(throwable.response()?.errorBody()?.string(), ResponseBody::class.java)
                                result.message?: ""
                            } else {
                                "알수 없는 오류가 발생하였습니다."
                            }
                            msg += "\n\n쿠폰 사용에 문제가 있으신 경우\n카카오톡(@풀리는수학) 이나 1670-2115 로 문의 바랍니다."
                            DialogUtils.confirmDialog(requireContext(), "확인", msg)
                            registBtn.isEnabled = true
                            registBtn.setLoading(false)
                        })

                }
            }
        }
    }

    private fun load() {
        compositeDisposable += API_APP.fetchCoupons()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d(javaClass.simpleName, "$result")
                setList(result.data.sortedByDescending { it.couponDetailID })
            }, {
                println("asoaso ")
            })
    }

    lateinit var adapter: CouponAdapter
    private fun setList(list: List<CouponItem>) {
        with(binding) {
            adapter = CouponAdapter(list.toMutableList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }

    inner class CouponAdapter(val list: MutableList<CouponItem>): RecyclerView.Adapter<CouponAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_pulley_coupon, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.set(list.get(position))
        }

        override fun getItemCount() = list.size

        inner class Holder(val view:View): RecyclerView.ViewHolder(view) {

            lateinit var item:CouponItem
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

            }

            fun useCoupon() {
                useBtnProgress.visibility = View.VISIBLE
                compositeDisposable += API_APP.useCoupon(item.couponDetailID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Log.d(javaClass.simpleName, "useCoupon ${result.data}")
                        useBtnProgress.visibility = View.GONE

                        viewModel.fetchUser {
                            CoroutineScope(Dispatchers.Main).launch {
                                DialogUtils.confirmV2(requireContext(), "쿠폰이 사용되었습니다", "${result.data?.message}", isOneBtn = true)
                            }
                            val reFetchReceiverIntent = Intent(RE_CONFIGURE_UI)
                            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(reFetchReceiverIntent)
                        }
                        load()
                    }, {
                        if(it is HttpException) {
                            val res = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                            println("asoaso response ${res.message}")
                            var message = res.message ?: "쿠폰을 사용할 수 없습니다."
                            message += "\n\n쿠폰 사용에 문제가 있으신 경우\n카카오톡(@풀리는수학) 이나 1670-2115 로 문의 바랍니다."
//                                DialogUtils.confirmDialog(requireContext(), "쿠폰 사용 오류", message)
                            DialogUtils.confirmV2(requireContext(), "쿠폰 사용 오류", message, isOneBtn = true)

                        } else {
                            DialogUtils.showServerErr(requireContext())
                        }
                        useBtnProgress.visibility = View.GONE
                    })

            }

            fun set(item: CouponItem) {
                this.item = item
                textName.text = item.couponCampaignTitle
                textBenefit.text = item.description

                val end = DateTimeUtils.convertServerStr(item.endAt)
                textPeriod.text = end


                useBtn.setOnClickListener { useCouponDialog() }
                useBtnButton.setOnClickListener { useCouponDialog() }

                if(item.canApplyNow) {
                    useBtnText.visibility = View.GONE
                    useBtnButton.visibility = View.VISIBLE
                } else {
                    useBtnText.visibility = View.VISIBLE
                    useBtnButton.visibility = View.GONE
                }
            }
            fun useCouponDialog() {
                if (item.canApplyNow) {
                    val dialogTitle = "${item.couponCampaignTitle}을 지금 사용하시겠습니까?"
                    val dialogContent = "쿠폰은 바로 적용되며, 현재 이용중인 서비스 및 쿠폰의 종류에 따라 이용 또는 할인 예약이 됩니다."
                    DialogUtils.confirmV2(requireContext(), dialogTitle, dialogContent, successCb = { useCoupon() })
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}