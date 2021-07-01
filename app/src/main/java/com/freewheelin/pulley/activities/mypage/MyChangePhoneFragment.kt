package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestChangePhone
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.utils.isValidPhoneNum
import com.freewheelin.pulley.views.CodeConfirmView
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_change_email.*
import retrofit2.HttpException

class MyChangePhoneFragment : MyPageBaseFragment(), CodeConfirmView.CodeConfirmInterface {

    val user
        get() = requireActivity().application.user!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_change_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        codeConfirm.codeInterface = this
        codeConfirm.setText(user.cellPhone)
    }

    override fun requestCode(text: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        when {
            text == user.cellPhone -> callback(CodeConfirmView.Status.Fail, "기존 휴대폰 번호와 동일한 번호는 사용할 수 없습니다.")
            text.isEmpty() -> callback(CodeConfirmView.Status.Fail, "휴대폰 번호를 입력하세요!")
            !text.isValidPhoneNum() -> callback(CodeConfirmView.Status.Fail, "휴대폰 번호가 형식에 맞지 않습니다!")
            else -> {
                API_V2.requestChangePhoneCode(text).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        requestCodeSuccess(callback)
                    }, {
                        requestCodeFailed(it, callback)
                    })
            }
        }
    }

    private fun requestCodeSuccess(callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        callback(CodeConfirmView.Status.Sucess, "성공")
        DaebakToast.show(requireContext(), "휴대폰 번호로 인증번호가 발송되었습니다. 문자메시지를 확인해주세요.", overDialog = true)
    }

    private fun requestCodeFailed(throwable: Throwable, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        if (throwable is HttpException) {
            val error = Gson().fromJson(throwable.response()?.errorBody()?.string(), ResponseBody::class.java)
            callback(CodeConfirmView.Status.Fail, error.message)
        } else {
            callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
        }
    }

    override fun requestConfirm(requestText:String, confirmCode: String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        when {
            confirmCode.isEmpty() -> callback(CodeConfirmView.Status.Fail, "코드를 입력하세요!")
            confirmCode.isEmpty() -> callback(CodeConfirmView.Status.Fail, "코드를 입력하세요!")
            confirmCode.length < 4 -> callback(CodeConfirmView.Status.Fail, "인증번호는 숫자 4자리입니다.")
            else -> {
                val request = RequestChangePhone(requestText, confirmCode)
                API_V2.requestChangePhone(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        requestConfirmSuccess(requestText, callback)
                    }, {
                        requestCodeFailed(it, callback)
                    })
            }
        }
    }

    private fun requestConfirmSuccess(phone:String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        user.cellPhone = phone
        user.isValidPhone = true
        user.commit("MyChangePhoneFragment requestConfirm")
        callback(CodeConfirmView.Status.Sucess, "성공")
        setFragmentResult(MySignUpInfoFragment.RELOAD, bundleOf())
        onBackBtnClicked()
    }

    override fun confirmSuccess() {
        DaebakToast.show(requireContext(),"변경되었습니다.", overDialog = true)
    }
}

