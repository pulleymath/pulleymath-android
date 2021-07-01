package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.fragment_my_app_setting.*
import android.widget.CompoundButton
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestAgreeInfo
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class MyAppSettingFragment : MyPageBaseFragment(), CompoundButton.OnCheckedChangeListener {

    val user
        get() = requireActivity().application.user!!

    enum class Type {
        Push, Marketing
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_app_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        initUI()
    }

    fun configureUI() {
        pushSwitch.isChecked = user.agreeAppPush
        eventNotiSwitch.isChecked = user.agreeMarketing
    }

    fun initUI() {
        pushSwitch.setOnCheckedChangeListener(this)
        eventNotiSwitch.setOnCheckedChangeListener(this)
    }

    fun requestPushUpdate(type: Type) {
        val agreeAppPush = pushSwitch.isChecked
        val agreeMarketing = eventNotiSwitch.isChecked

        val request = RequestAgreeInfo(agreeAppPush, agreeMarketing)
        API_V2.updateAgreeInfo(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    when(type) {
                        Type.Push -> user.agreeAppPush = agreeAppPush
                        Type.Marketing -> user.agreeMarketing = agreeMarketing
                    }
                    user.commit("Updated AppSetting")

                    DaebakToast.show(requireContext(), "변경되었습니다.", overDialog = true)
                },{
                    // 실패일 경우 원복
                    when(type) {
                        Type.Push -> pushSwitch.isChecked = !agreeAppPush
                        Type.Marketing -> eventNotiSwitch.isChecked = !agreeMarketing
                    }

                    if(it is HttpException) {
                        val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                        DaebakToast.show(requireContext(), "${error.message}.", overDialog = true)
                    }
                })
    }

    override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
        if(button.isPressed) {
            when (button) {
                pushSwitch -> requestPushUpdate(Type.Push)
                eventNotiSwitch -> requestPushUpdate(Type.Marketing)
            }
        }
    }
}

