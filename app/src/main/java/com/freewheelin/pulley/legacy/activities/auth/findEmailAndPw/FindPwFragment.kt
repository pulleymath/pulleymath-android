package com.freewheelin.pulley.legacy.activities.auth.findEmailAndPw


import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestCheckCode
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestReset
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestResetPassword
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.FragmentFindPwBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

class FindPwFragment : Fragment() {

    enum class From : Serializable{
        MYPAGE, SIGNUP;

        companion object {
            fun get(value: String?): From {
                return if (value == null) MYPAGE else valueOf(value)
            }
        }
    }

    companion object {
        const val KEY_FROM = "key_from"
        val IS_GUEST_USER = "IS_GUEST_USER"

        fun newInstance(isGuestUser: Boolean = false): FindPwFragment {
            val fragment = FindPwFragment()
            val bundle = Bundle()
            bundle.putString(KEY_FROM, From.SIGNUP.name)
            bundle.putBoolean(IS_GUEST_USER, isGuestUser)
            fragment.arguments = bundle
            return fragment
        }
    }

    var from:From = From.MYPAGE
    lateinit var binding: FragmentFindPwBinding
    var isGuestUser = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_pw, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isGuestUser = it.getBoolean(FindEmailFragment.IS_GUEST_USER)
        }
        binding.apply {
            from = From.get(arguments?.getString(KEY_FROM))

            when(from) {
                From.MYPAGE -> {
                    // 상단 정렬로 번경
                    scrollContentsCl.gravity = Gravity.CENTER_HORIZONTAL
                    scrollContentsCl.setPaddingTop(24.toPx())
                    // 완료 화면처리
                    completeContainerCl.setPaddingTop(60.toPx())
                    toLoginBtn.visibility = View.GONE // 로긴 버튼 가리기
                }
                else -> {}
            }

            resultContainerCl.visibility = View.GONE
            completeContainerCl.visibility = View.GONE
            emailDet.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            codeConfirmBtn.isEnabled = false
            codeConfirmBtn.setOnClickListener { if(codeConfirmBtn.isEnabled) onCodeConfirmClicked() }
            sendResetBtn.setOnClickListener { onSendResetClicked() }
            toLoginBtn.setOnClickListener { onToLoginClicked() }

            setValidButton()
            setListeners()
        }
    }

    private fun setListeners() {
        binding.apply {
            emailDet.editText.doAfterTextChanged { editable -> setValidButton() }
            sendBtn.setOnClickListener { if(sendBtn.isEnabled) onSendBtnClicked() }
            selectRadio.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId) {
                    R.id.radioEmail -> {
                        emailDet.label = "이메일"
                        emailDet.editText.hint = "name@yourdomain.com"
                        emailDet.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        emailDet.editText.filters = arrayOf( InputFilter.LengthFilter(350) )
                    }
                    R.id.radioPhone -> {
                        emailDet.label = "휴대폰 번호"
                        emailDet.editText.hint = "- 없이 입력해주세요"
                        emailDet.editText.inputType = InputType.TYPE_CLASS_NUMBER
                        emailDet.editText.filters = arrayOf( InputFilter.LengthFilter(11) )

                    }
                }
                emailDet.text = ""
                backToRequestCodeLayout()
                setValidButton()
            }

            emailDet.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            codeDet.inputType = InputType.TYPE_CLASS_NUMBER
            codeDet.doAfterTextChanged { text ->
                if(text?.length?:0 == 4) {
                    codeConfirmBtn.visibility = View.VISIBLE
                    codeConfirmBtn.isEnabled = true
                } else {
                    codeConfirmBtn.isEnabled = false
                }
            }
        }
    }

    private fun setValidButton() {
        binding.apply {
            val method = emailDet.text

            if(method.isEmpty()) {
                sendBtn.isEnabled = false
                return
            }

            val valid = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> method.isValidEmail()
                else -> method.isValidPhoneNum()
            }

            sendBtn.isEnabled = valid
        }
    }

    private fun onSendBtnClicked() {
        binding.apply {
            val target = emailDet.text

            val errMsg = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> "이메일 주소를 입력해주세요."
                else -> "휴대폰 번호를 입력해주세요."
            }

            val type = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> "EMAIL"
                else -> "CELLPHONE"
            }

            if(target.isEmpty()) {
                emailDet.showErrorMsg(errMsg)
                return
            }

            when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> {
                    if(!target.isValidEmail()){
                        emailDet.showErrorMsg("이메일 주소를 확인해주세요.")
                        return
                    }
                }
                else -> {
                    if(!target.isValidPhoneNum()){
                        emailDet.showErrorMsg("휴대폰 번호를 확인해주세요.")
                        return
                    }
                }
            }

            val request = RequestReset(type, target)

            deinitTimer()

            API_V2.requestReset(request).enqueue(object : Callback<ResponseBody<String>> {
                override fun onFailure(call: Call<ResponseBody<String>>, t: Throwable) {
                    responseFailed(context!!, t)
                }

                override fun onResponse(call: Call<ResponseBody<String>>, response: Response<ResponseBody<String>>) {
                    val code = response.code()
                    when(code) {
                        200 -> requestSuccess()
                        else -> requestFailed(response)
                    }
                }
            })
        }
    }

    private fun requestSuccess() {
        binding.apply {
            runTimer()
            sendBtn.visibility = View.GONE
            codeContainer.visibility = View.VISIBLE
            timerTv.showIfNeed()

            codeDet.setText("")
            codeDet.requestFocus()
            val toastMsg = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> "이메일 주소로 인증번호가 발송되었습니다. 메시지함을 확인해주세요."
                else -> "휴대폰 번호로 인증번호가 발송되었습니다. 문자를 확인해주세요."
            }

            DaebakToast.show(requireContext(), toastMsg, overDialog = true)
        }
    }

    private fun requestFailed(response: Response<ResponseBody<String>>) {
        binding.apply {
            val error = Gson().fromJson(response?.errorBody()?.string(), ResponseBody::class.java)
            val message = error?.message
            Log.d("비번찾기","message=$message")

            val defaultMsg = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> getString(R.string.text_this_email_is_not_registered)
                else -> "등록되지 않은 휴대폰 번호입니다."
            }

            when(error.error) {
                LoginActivity.NOT_FOUND_DATA -> emailDet.showErrorMsg(defaultMsg)
                else -> emailDet.showErrorMsg(message ?: defaultMsg)
            }
        }
    }

    private fun backToRequestCodeLayout() {
        binding.apply {
            codeContainer.visibility = View.GONE
            codeDetErrorContainer.visibility = View.GONE
            timerTv.visibility = View.GONE
            sendBtn.show(1000)
            deinitTimer()
        }
    }

    private fun onCodeConfirmClicked() {
        binding.apply {
            if(codeDet.text.length != 4) {
                codeDetErrorContainer.visibility = View.VISIBLE
                codeDetError.text = "인증번호를 정확하게 입력하세요."
            } else {
                codeDetErrorContainer.visibility = View.GONE
                val request = RequestCheckCode(codeDet.text.toString(), emailDet.text)

                API_V2.checkPhoneCode(request).enqueue(object: Callback<Template<String?>>{
                    override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                        when(response.code()) {
                            200 -> requestPhoneCodeSuccess()
                            else -> requestPhoneCodeFailed(response)
                        }
                    }

                    override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                        DialogUtils.showServerErr(context!!)
                    }
                })
            }
        }
    }

    private fun requestPhoneCodeSuccess() {
        binding.apply {
            inputContainerCl.visibility = View.GONE
            resultContainerCl.visibility = View.VISIBLE
        }
    }

    private fun requestPhoneCodeFailed(response: Response<Template<String?>>) {
        binding.apply {
            codeDetErrorContainer.visibility = View.VISIBLE
            codeDetError.text = "${response.body()?.message?:"인증번호가 일치하지 않습니다."}"
        }
    }

    private fun onSendResetClicked() {
        binding.apply {
            if(password.text.isEmpty()){
                password.isShownError = true
                password.errorMsg = "비밀번호를 입력하세요."
                return
            } else if(!password.text.isValidPW()) {
                password.isShownError = true
                password.errorMsg = getString(R.string.text_please_input_6_between_15_eng_num_symbol)
                return
            } else if(password.text != passwordConfirm.text) {
                password.isShownError = false
                // 비밀번호 확인이 다릅니다.
                passwordConfirm.isShownError = true
                passwordConfirm.errorMsg = "비밀번호와 비밀번호 확인이 일치하지 않습니다."
                return
            }

            password.isShownError = false
            passwordConfirm.isShownError = false

            val target = emailDet.text
            val type = when(selectRadio.checkedRadioButtonId) {
                R.id.radioEmail -> "EMAIL"
                else -> "CELLPHONE"
            }
            val password = password.text
            val code = codeDet.text.toString()
            val request = RequestResetPassword(code, password, type, target)

            API_V2.requestResetPassword(request).enqueue(object: Callback<Template<String?>>{
                override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                    when(response.code()) {
                        200 -> {
                            resultContainerCl.visibility = View.GONE
                            completeContainerCl.visibility = View.VISIBLE
                        }
                        else -> {

                        }
                    }
                }

                override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                    DialogUtils.showServerErr(context!!)
                }
            })
        }
    }

    private fun onToLoginClicked() {
        if (!isGuestUser) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        activity?.finish()
    }

    var remainSec = 180
    var timer: Timer? = null

    private fun runTimer() {
        timer = Timer()
        val task = timerTask {
            tick()
        }
        timer?.schedule(task, 1000, 1000)
    }

    private fun deinitTimer() {
        remainSec = 180
        timer?.cancel()
        timer = null
        val min = (remainSec) / 60
        val sec = remainSec % 60

        binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
    }

    fun tick() {
        activity?.runOnUiThread {
            try {
                if (remainSec > 0) {
                    remainSec = remainSec - 1
                    val min = (remainSec) / 60
                    val sec = remainSec % 60

                    binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
                } else {
                    timer?.cancel()
                    timer = null
                    binding.codeDetErrorContainer.visibility = View.VISIBLE
                    binding.codeDetError.text = "시간이 만료되었습니다."

                    thread(start = true) {
                        Thread.sleep(1000)
                        activity?.runOnUiThread {
                            backToRequestCodeLayout()
                        }
                    }
                }
            }catch (e:Exception) {
                timer?.cancel()
                timer = null
                e.printStackTrace()
            }
        }
    }
}
