package com.freewheelin.pulley.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.API.RequestModel.RequestChangePhone
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.utils.isValidPhoneNum
import com.freewheelin.pulley.views.CodeConfirmView
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_confirm_phone.*
import retrofit2.HttpException


class ConfirmPhoneDialog(val activity: Activity, val successCB:()->Unit, val failCB:()->Unit): Dialog(activity), CodeConfirmView.CodeConfirmInterface {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_confirm_phone)
        initUI()
    }

    private fun initUI() {
        setCancelable(false)
        btnClose.setOnClickListener { close() }
        closeBtn.setOnClickListener { close() }

        codeConfirm.codeInterface = this
    }

    private fun close() {
        dismiss()
        failCB()
    }

    private fun success() {
        dismiss()
        successCB()
    }

    override fun requestCode(text: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        if(text.isEmpty()) {
            callback(CodeConfirmView.Status.Fail, "휴대폰 번호를 입력하세요!")
        } else if(!text.isValidPhoneNum()) {
            callback(CodeConfirmView.Status.Fail, "휴대폰 번호가 형식에 맞지 않습니다!")
        }
//        else if(user?.cellPhone == text) {
//            callback(CodeConfirmView.Status.Fail, "변경하는 휴대폰 번호가 기존 번호와 같습니다!")
//        }
        else {
            API_V2.requestChangePhoneCode(text)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        callback(CodeConfirmView.Status.Sucess, "성공")

                    },{
                        if(it is HttpException) {
                            val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                            callback(CodeConfirmView.Status.Fail, error.message)
                        } else {
                            callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
                        }
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
            val request = RequestChangePhone(requestText, confirmCode)
            API_V2.requestChangePhone(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        user?.cellPhone = requestText
                        user?.commit("MyChangePhoneFragment requestConfirm")
                        callback(CodeConfirmView.Status.Sucess, "성공")
                        success()
                    },{
                        if(it is HttpException) {
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