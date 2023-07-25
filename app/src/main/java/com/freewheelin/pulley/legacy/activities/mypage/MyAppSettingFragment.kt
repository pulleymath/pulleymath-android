package com.freewheelin.pulley.legacy.activities.mypage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.mypage.NotificationSettingRequest
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.databinding.FragmentMyAppSettingBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
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

    lateinit var binding: FragmentMyAppSettingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_app_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        load()
        initUI()
    }

    @SuppressLint("CheckResult")
    fun load() {
        API_APP.getNotificationSetting()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                response.data.apply {

                    binding.alimtalkSwitch.isChecked = isAgreeAlimtalk
                    binding.pushSwitch.isChecked = isAgreePush
                    binding.emailSwitch.isChecked = isAgreeEmail
                    binding.marketingSwitch.isChecked = isAgreeMarketing

                    user.agreeAlimtalk = isAgreeAlimtalk
                    user.agreeAppPush = isAgreePush
                    user.agreeEmail = isAgreeEmail
                    user.agreeMarketing = isAgreeMarketing
                }
            },{
                DialogUtils.confirmDialog(requireContext(), "설정확인", "알림설정을 로드할 수 없습니다.")
            })
    }

    fun initUI() {
        binding.alimtalkSwitch.setOnCheckedChangeListener(this)
        binding.pushSwitch.setOnCheckedChangeListener(this)
        binding.emailSwitch.setOnCheckedChangeListener(this)
        binding.marketingSwitch.setOnCheckedChangeListener(this)
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }

    @SuppressLint("CheckResult")
    fun requestPushUpdate(type: Type) {
        val agreeAlimtalk = binding.alimtalkSwitch.isChecked
        val agreeAppPush = binding.pushSwitch.isChecked
        val agreeEmail = binding.emailSwitch.isChecked
        val agreeMarketing = binding.marketingSwitch.isChecked

        val request = NotificationSettingRequest(agreeAlimtalk, agreeAppPush, agreeEmail, agreeMarketing)

        API_APP.setNotificationSetting(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    DaebakToast.show(requireContext(), "변경되었습니다.", overDialog = true)

                    response.data.apply {
                        user.agreeAlimtalk = isAgreeAlimtalk
                        user.agreeAppPush = isAgreePush
                        user.agreeEmail = isAgreeEmail
                        user.agreeMarketing = isAgreeMarketing
                    }
                },{
                    // 실패일 경우 원복
                    when(type) {
                        Type.Alimtalk -> binding.alimtalkSwitch.isChecked = !agreeAlimtalk
                        Type.Push -> binding.pushSwitch.isChecked = !agreeAppPush
                        Type.Email -> binding.emailSwitch.isChecked = !agreeEmail
                        Type.Marketing -> binding.marketingSwitch.isChecked = !agreeMarketing
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
                binding.alimtalkSwitch -> requestPushUpdate(Type.Alimtalk)
                binding.pushSwitch -> requestPushUpdate(Type.Push)
                binding.emailSwitch -> requestPushUpdate(Type.Email)
                binding.marketingSwitch -> requestPushUpdate(Type.Marketing)
            }
        }
    }
}

