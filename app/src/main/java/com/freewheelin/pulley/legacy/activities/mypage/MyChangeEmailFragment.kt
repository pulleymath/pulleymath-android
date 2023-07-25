package com.freewheelin.pulley.legacy.activities.mypage


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangeEmail
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyChangeEmailBinding
import com.freewheelin.pulley.legacy.core.API.RequestModel.sign.AuthPhoneRequest
import com.freewheelin.pulley.legacy.core.API.RequestModel.sign.ConfirmCodeRequest
import com.freewheelin.pulley.legacy.core.API_ANONYMOUS
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.isValidEmail
import com.freewheelin.pulley.legacy.utils.show
import com.freewheelin.pulley.legacy.views.CodeConfirmView
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class MyChangeEmailFragment : MyPageBaseFragment(), CodeConfirmView.CodeConfirmInterface {

    val user
        get() = requireActivity().application.user!!

    val viewModel: MyMainPageFragViewModel by viewModels()
    lateinit var binding: FragmentMyChangeEmailBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_change_email, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        viewModel.apply {
            errorAction.observe(viewLifecycleOwner) { type ->
                when (type) {
                    CoroutineExceptionType.HttpException400 -> {
                        Log.e(javaClass.simpleName, "400 ERROR from v4/me/email")
                        requestCallback(CodeConfirmView.Status.Fail, "알수 없는 오류가 발생하였습니다. 다시 시도하세요")
                    }
                    else -> {}
                }
            }
        }
    }

    fun initUI() {
        with(binding) {
            codeConfirm.codeInterface = this@MyChangeEmailFragment
            codeConfirm.setText(user.email ?: "")
            codeConfirm.setConfirmButtonText(if(user.isValidEmail) "변경하기" else "인증하기")
            textTitle.text = if(user.isValidEmail) "이메일 변경" else "이메일 인증"
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    var requestCallback: (status: CodeConfirmView.Status, msg: String) -> Unit = {_, _, -> }
    override fun requestCode(targetEmail: String, type: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        requestCallback = callback
        when {
            targetEmail.isEmpty() -> callback(CodeConfirmView.Status.Fail, "이메일을 입력하세요!")
            !targetEmail.isValidEmail() -> callback(CodeConfirmView.Status.Fail, "이메일이 형식에 맞지 않습니다!")
            user.email == targetEmail && user.isValidEmail -> callback(CodeConfirmView.Status.Fail, "이미 사용 중인 이메일 주소입니다.")

            else -> {
                val authRequest = AuthPhoneRequest("EMAIL", targetEmail, null, null, "CHANGE_EMAIL")
                disposables += API_ANONYMOUS.getAuthCode(authRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        if(result.error != null) {
                            requestCodeFailed(Throwable(result.error), callback)
                        } else {
                            requestCodeSuccess(callback)
                        }
                    }, {
                        requestCodeFailed(it, callback)
                    })
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
    internal val disposables = CompositeDisposable()

    override fun requestConfirm(requestText:String, confirmCode: String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        // 여기서 [코드 인증] 요청후 콜백
        requestCallback = callback
        when {
            confirmCode.isEmpty() -> callback(CodeConfirmView.Status.Fail, "코드를 입력하세요!")
            confirmCode.length < 4 -> callback(CodeConfirmView.Status.Fail, "인증번호는 숫자 4자리입니다.")
            else -> {

                val authCode = ConfirmCodeRequest(confirmCode, requestText)
                disposables += API_ANONYMOUS.confirmAuthCode(authCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        confirmEmail(requestText, confirmCode, callback)
                    }, {
//                        requestConfirmFailed(it, callback)
                        when (it) {
                            is retrofit2.HttpException -> {
                                println("throwable - HttpException : ${it.code()} / ${it.message}")
                                when (it.code()) {
                                    400 -> {
                                        DaebakToast.show(requireContext(), "인증번호가 유효하지 않습니다", overDialog = true)
                                    }
                                    else -> {

                                    }
                                }
                            }
                            else -> {
                                DialogUtils.showServerErr(requireContext())
                            }
                        }
                    })
            }
        }
    }

    private fun confirmEmail(email: String, confirmCode: String, callback: (status: CodeConfirmView.Status, msg: String?) -> Unit) {
        viewModel.changeEmail(email, confirmCode) {
            requestConfirmSuccess(email, "변경되었습니다.", callback)
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

