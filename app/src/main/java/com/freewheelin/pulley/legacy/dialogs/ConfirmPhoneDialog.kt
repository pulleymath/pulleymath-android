package com.freewheelin.pulley.legacy.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangePhone
import com.freewheelin.pulley.legacy.core.API.RequestModel.sign.AuthPhoneRequest
import com.freewheelin.pulley.legacy.core.API.ResponseModel.sign.CountryCodeResponse
import com.freewheelin.pulley.legacy.core.API_ANONYMOUS
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.DialogConfirmPhoneBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.isValidPhoneNum
import com.freewheelin.pulley.legacy.views.CodeConfirmView
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class ConfirmPhoneDialog(val activity: Activity, val successCB: () -> Unit, val failCB: () -> Unit): Dialog(activity), CodeConfirmView.CodeConfirmInterface {
    val binding: DialogConfirmPhoneBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_confirm_phone, null, false)
    }
    lateinit var countryCodes:List<CountryCodeResponse.CountryCode>
    var countryCode = "82"
    var countryType = "KOR"
    var purposeType = "SIGN_UP"

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        initUI()
        load()
    }

    private fun load() {
        API_ANONYMOUS.listCountryCodes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                countryCodes = result.data
                setSpinner()
            }, { error ->
                Log.e(javaClass.simpleName, "confirm phone dialog load error=${error.localizedMessage}")
            })
    }

    private fun setSpinner() {
        val items = countryCodes.map { "(+${it.code}) ${it.title}"}
        binding.countrySpinner.set(items) {
            val country = countryCodes.get(it)
            countryCode = country.code
            countryType = country.type
        }
        binding.countrySpinner.position = countryCodes.indexOfFirst { it.code == "82" }
    }

    private fun initUI() {
        setCancelable(false)
        binding.btnClose.setOnClickListener { close() }
        binding.closeBtn.setOnClickListener { close() }

        binding.codeConfirm.codeInterface = this
    }

    private fun close() {
        dismiss()
        failCB()
    }

    private fun success() {
        dismiss()
        successCB()
    }

    override fun requestCode(text: String, type: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        if(text.isEmpty()) {
            callback(CodeConfirmView.Status.Fail, "휴대폰 번호를 입력하세요!")
        } else if(!text.isValidPhoneNum()) {
            callback(CodeConfirmView.Status.Fail, "휴대폰 번호가 형식에 맞지 않습니다!")
        }
//        else if(user?.cellPhone == text) {
//            callback(CodeConfirmView.Status.Fail, "변경하는 휴대폰 번호가 기존 번호와 같습니다!")
//        }
        else {

            val authRequest = AuthPhoneRequest(type, text, countryCode, countryType, purposeType )

            API_ANONYMOUS.getAuthCode(authRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if(result.error != null) {
                        callback(CodeConfirmView.Status.Fail, result.error)
                    } else {
                        callback(CodeConfirmView.Status.Sucess, "성공")
                    }
                }, {
                    callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
                })

        }
    }

    override fun requestConfirm(requestText:String, confirmCode: String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        // 여기서 [코드 인증] 요청후 콜백
        if(confirmCode.isEmpty()) {
            callback(CodeConfirmView.Status.Fail, "코드를 입력하세요!")
        } else if(confirmCode.length < 4) {
            callback(CodeConfirmView.Status.Fail, "인증번호는 숫자 4자리입니다.")
        } else {
//            val authCode = ConfirmCodeRequest(confirmCode, requestText)
            val request = RequestChangePhone(requestText, confirmCode, countryCode)
            API_V2.requestChangePhone(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if(response.error != null) {
                        callback(CodeConfirmView.Status.Fail, response.message)
                    } else {
                        user?.cellPhone = requestText
                        user?.commit("MyChangePhoneFragment requestConfirm")
                        callback(CodeConfirmView.Status.Sucess, "성공")
                        success()
                    }
                }, {
                    if (it is HttpException) {
                        val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                        callback(CodeConfirmView.Status.Fail, error.message)
                    } else {
                        callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
                    }
                })
        }
    }

    override fun confirmSuccess() {
        DaebakToast.show(context,"변경되었습니다.", overDialog = true)
    }
}