package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestChangeEmail
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.utils.isValidEmail
import com.freewheelin.pulley.views.CodeConfirmView
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_change_email.*
import kotlinx.android.synthetic.main.view_code_confirm.view.*
import retrofit2.HttpException


class MyChangeEmailFragment : MyPageBaseFragment(), CodeConfirmView.CodeConfirmInterface {

    val user
        get() = requireActivity().application.user!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_change_email, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        codeConfirm.codeInterface = this
        codeConfirm.setText(user.email)
        codeConfirm.setConfirmButtonText(if(user.isValidEmail) "변경하기" else "인증하기")
        textTitle.text = if(user.isValidEmail) "이메일 변경" else "이메일 인증"
    }

    override fun requestCode(text: String, type: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        when {
            text.isEmpty() -> callback(CodeConfirmView.Status.Fail, "이메일을 입력하세요!")
            !text.isValidEmail() -> callback(CodeConfirmView.Status.Fail, "이메일이 형식에 맞지 않습니다!")
            user.email == text && user.isValidEmail -> callback(CodeConfirmView.Status.Fail, "이미 사용 중인 이메일 주소입니다.")

            else -> {
                if(!user.isValidEmail && user.email == text) { // 인증시 코드 요청
                    API_V2.requestCertifyMailCode(text)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                requestCodeSuccess(callback)
                            }, {
                                requestCodeFailed(it, callback)
                            })
                } else { // 변경 시 코드 요청
                    API_V2.requestChangeMailCode(text)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                requestCodeSuccess(callback)
                            }, {
                                requestCodeFailed(it, callback)
                            })
                }
            }
        }
    }

    private fun requestCodeSuccess(callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        callback(CodeConfirmView.Status.Sucess, "성공")
        DaebakToast.show(requireContext(), "이메일 주소로 인증번호가 발송되었습니다. 메시지를 확인해주세요.", overDialog = true)
    }

    private fun requestCodeFailed(throwable: Throwable, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        if (throwable is HttpException) {
            val error = Gson().fromJson(throwable.response()?.errorBody()?.string(), ResponseBody::class.java)
            callback(CodeConfirmView.Status.Fail, error.message)
        } else {
            callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
        }
    }

    override fun requestConfirm(requestText:String, confirmCode: String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        // 여기서 [코드 인증] 요청후 콜백
        when {
            confirmCode.isEmpty() -> callback(CodeConfirmView.Status.Fail, "코드를 입력하세요!")
            confirmCode.length < 4 -> callback(CodeConfirmView.Status.Fail, "인증번호는 숫자 4자리입니다.")
            else -> {
                if(!user.isValidEmail && user.email == requestText) {
                    API_V2.requestCertifyMail(RequestChangeEmail(requestText, confirmCode))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                requestConfirmSuccess(requestText, "인증되었습니다.", callback)
                            }, {
                                requestConfirmFailed(it, callback)
                            })
                } else {
                    API_V2.requestChangeMail(RequestChangeEmail(requestText, confirmCode))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                requestConfirmSuccess(requestText, "변경되었습니다.", callback)
                            }, {
                                requestConfirmFailed(it, callback)
                            })
                }
            }
        }
    }

    private fun requestConfirmSuccess(email:String, successMsg:String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        val msg = if (user.isValidEmail) "이메일 주소가 변경되었습니다."
        else "이메일 주소 인증이 완료되었습니다."
        DaebakToast.show(requireContext(), msg, overDialog = true)

        user.email = email
        user.isValidEmail = true
        user.commit("MyChangeEmailFragment requestConfirm")
        callback(CodeConfirmView.Status.Sucess, "성공")
        DaebakToast.show(requireContext(), successMsg, overDialog = true)
        setFragmentResult(MySignUpInfoFragment.RELOAD, bundleOf())
        onBackBtnClicked()
    }

    private fun requestConfirmFailed(throwable:Throwable, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        if (throwable is HttpException) {
            val error = Gson().fromJson(throwable.response()?.errorBody()?.string(), ResponseBody::class.java)
            callback(CodeConfirmView.Status.Fail, error.message)
        } else {
            callback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요!")
        }
    }

    override fun confirmSuccess() {
//        DaebakToast.show(requireContext(), "변경되었습니다.", overDialog = true)
    }
}

