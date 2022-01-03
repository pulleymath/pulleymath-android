package com.freewheelin.pulley.activities.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import android.widget.CompoundButton
import android.widget.Switch
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.mypage.NotificationSettingRequest
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MyAppSettingFragment : MyPageBaseFragment(), CompoundButton.OnCheckedChangeListener {

    val user
        get() = requireActivity().application.user!!

    enum class Type {
        Alimtalk, Push, Email, Marketing
    }

    lateinit var alimtalkSwitch: Switch
    lateinit var pushSwitch: Switch
    lateinit var emailSwitch: Switch
    lateinit var marketingSwitch: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_app_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alimtalkSwitch = view.findViewById(R.id.alimtalkSwitch)
        pushSwitch = view.findViewById(R.id.pushSwitch)
        emailSwitch = view.findViewById(R.id.emailSwitch)
        marketingSwitch = view.findViewById(R.id.marketingSwitch)

        load()
        initUI()
    }

    fun load() {
        API_APP.getNotificationSetting()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                response.data?.apply {

                    alimtalkSwitch.isChecked = isAgreeAlimtalk
                    pushSwitch.isChecked = isAgreePush
                    emailSwitch.isChecked = isAgreeEmail
                    marketingSwitch.isChecked = isAgreeMarketing

                    user?.update(agreeAlimtalk = isAgreeAlimtalk, agreeAppPush = isAgreePush, agreeEmail = isAgreeEmail, agreeMarketing = isAgreeMarketing)
                }
            },{
                DialogUtils.confirmDialog(requireContext(), "설정확인", "알림설정을 로드할 수 없습니다.")
            })
    }

    fun initUI() {
        alimtalkSwitch.setOnCheckedChangeListener(this)
        pushSwitch.setOnCheckedChangeListener(this)
        emailSwitch.setOnCheckedChangeListener(this)
        marketingSwitch.setOnCheckedChangeListener(this)
    }

    fun requestPushUpdate(type: Type) {
        val agreeAlimtalk = alimtalkSwitch.isChecked
        val agreeAppPush = pushSwitch.isChecked
        val agreeEmail = emailSwitch.isChecked
        val agreeMarketing = marketingSwitch.isChecked

        val request = NotificationSettingRequest(agreeAlimtalk, agreeAppPush, agreeEmail, agreeMarketing)

        API_APP.setNotificationSetting(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    DaebakToast.show(requireContext(), "변경되었습니다.", overDialog = true)

                    response.data?.apply {
                        user?.update(agreeAlimtalk = isAgreeAlimtalk, agreeAppPush = isAgreePush, agreeEmail = isAgreeEmail, agreeMarketing = isAgreeMarketing)
                    }
                },{
                    // 실패일 경우 원복
                    when(type) {
                        Type.Alimtalk -> alimtalkSwitch.isChecked = !agreeAlimtalk
                        Type.Push -> pushSwitch.isChecked = !agreeAppPush
                        Type.Email -> emailSwitch.isChecked = !agreeEmail
                        Type.Marketing -> marketingSwitch.isChecked = !agreeMarketing
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
                alimtalkSwitch -> requestPushUpdate(Type.Alimtalk)
                pushSwitch -> requestPushUpdate(Type.Push)
                emailSwitch -> requestPushUpdate(Type.Email)
                marketingSwitch -> requestPushUpdate(Type.Marketing)
            }
        }
    }
}

