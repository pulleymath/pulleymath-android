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
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangePhone
import com.freewheelin.pulley.legacy.core.API.RequestModel.sign.AuthPhoneRequest
import com.freewheelin.pulley.legacy.core.API.ResponseModel.sign.CountryCodeResponse
import com.freewheelin.pulley.legacy.core.API_ANONYMOUS
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyChangePhoneBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.isValidPhoneNum
import com.freewheelin.pulley.legacy.views.CodeConfirmView
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MyChangePhoneFragment : MyPageBaseFragment(), CodeConfirmView.CodeConfirmInterface {

    val user
        get() = requireActivity().application.user!!

    lateinit var countryCodes:List<CountryCodeResponse.CountryCode>
    var countryCode = "82"
    var countryType = "KOR"
    var purposeType = "SIGN_UP"

    lateinit var binding: FragmentMyChangePhoneBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_change_phone, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        load()
    }

    fun initUI() {
        with(binding) {
            codeConfirm.codeInterface = this@MyChangePhoneFragment
            codeConfirm.setText(user.cellPhone)
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun load() {
        API_ANONYMOUS.listCountryCodes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                countryCodes = result.data
                setSpinner()
            }, { error ->
                Log.e(javaClass.simpleName, "mychangephone f load error=${error.localizedMessage}")
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

    override fun requestCode(text: String, type: String, callback:(status: CodeConfirmView.Status, msg:String?)->Unit) {
        when {
            text == user.cellPhone -> callback(CodeConfirmView.Status.Fail, "기존 휴대폰 번호와 동일한 번호는 사용할 수 없습니다.")
            text.isEmpty() -> callback(CodeConfirmView.Status.Fail, "휴대폰 번호를 입력하세요!")
            !text.isValidPhoneNum() -> callback(CodeConfirmView.Status.Fail, "휴대폰 번호가 형식에 맞지 않습니다!")
            else -> {
                val authRequest = AuthPhoneRequest(type, text, countryCode, countryType, purposeType )

                API_ANONYMOUS.getAuthCode(authRequest)
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
                val request = RequestChangePhone(requestText, confirmCode, countryCode)
                API_V2.requestChangePhone(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        if(response.error != null) {
                            requestCodeFailed(Throwable(response.message), callback)
                        } else {
                            requestConfirmSuccess(requestText, callback)
                        }
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

