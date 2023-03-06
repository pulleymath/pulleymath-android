package com.freewheelin.pulley.activities.auth.signup

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.login.LoginActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.*
import com.freewheelin.pulley.core.API.RequestModel.sign.AuthPhoneRequest
import com.freewheelin.pulley.core.API.RequestModel.sign.ConfirmCodeRequest
import com.freewheelin.pulley.core.API.ResponseModel.sign.CountryCodeResponse
import com.freewheelin.pulley.databinding.FragmentSignupBinding
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.revision2023.viewmodel.SignupFragViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.editText.*
import com.freewheelin.pulley.views.v2.PolicyLayoutV2
import com.freewheelin.pulley.views.v2.PolicyLayoutV2Listener
import com.freewheelin.pulley.views.v2.SpinnerV2
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.concurrent.timerTask

class SignupFragment : Fragment(), PasswordFieldV2Listener, InputFieldV2Listener, PolicyLayoutV2Listener, CompoundButton.OnCheckedChangeListener {

    var signupInterface: StudentInfoInterface? = null
    private val viewModel: SignupFragViewModel by viewModels()

    lateinit var countryCodes:List<CountryCodeResponse.CountryCode>
    var countryCode = "82"
    var countryType = "KOR"
    var purposeType = "SIGN_UP"
    lateinit var binding: FragmentSignupBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is StudentInfoInterface) signupInterface = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        load()
    }

    fun isAllEmpty(): Boolean {
        return binding.fullNameDet.text.isEmpty()
            && binding.emailDet.text.isEmpty()
            && binding.pwDet.text.isEmpty()
            && binding.pwConfirmDet.text.isEmpty()
            && binding.phoneNumDet.text.isEmpty()
    }

    fun isAvailableToSecondStep(): Boolean {
        return (
            (binding.fullNameDet.text.isNotEmpty() && binding.fullNameDet.text.isValidName())
                && (binding.emailDet.text.isValidEmail())
                && (binding.pwDet.text.isNotEmpty() && binding.pwDet.text.isValidPW() && binding.pwDet.text == binding.pwConfirmDet.text)
                && binding.codeConfirmIv.visibility == View.VISIBLE
                && isAllCheckedEssentialBox()
            )
    }

    override fun onFieldFocusChanged(view: InputFieldV2, hasFocus: Boolean) {
        binding.apply {
            if(view === fullNameDet && !hasFocus) {
                fullNameDet.text = fullNameDet.text.trim()
                if(fullNameDet.text.isEmpty()) {
                    fullNameDet.showErrorMsg("이름을 입력해주세요.")
                } else if(fullNameDet.text.length < 2) {
                    fullNameDet.showErrorMsg("이름을 2자 이상 입력해주세요.")
                } else if(!fullNameDet.text.isValidName()) {
                    fullNameDet.showErrorMsg("올바른 형식이 아닙니다.")
                }
            }

            if(view == emailDet && !hasFocus) {
//                emailDet.text = emailDet.text.trim()
                if(emailDet.text.isEmpty()) {
                    emailDet.showErrorMsg("이메일을 입력해주세요.")
                } else if(emailDet.text.isValidEmail() == false) {
                    emailDet.showErrorMsg("이메일 형식을 확인해주세요.")
                } else {
                    checkEmail()
                }
            }

            if(view === phoneNumDet && !hasFocus) {
                if(phoneNumDet.text.isEmpty())
                    phoneNumDet.showErrorMsg("휴대폰 번호를 입력해주세요.")
                else if(phoneNumDet.text.isValidPhoneNum() == false)
                    phoneNumDet.showErrorMsg("전화번호 형식을 확인해주세요.")
            }

            verifyNextBtn()
        }
    }

    override fun onFieldFocusChanged(view: PasswordFieldV2, hasFocus: Boolean) {
        binding.apply {
            if(view === pwDet) {
                if (!hasFocus) {
                    if (!view.text.isValidPW()) {
                        view.showErrorMsg(getString(R.string.text_please_input_6_between_15_eng_num_symbol))
                    } else {
                        showPwErrorMsg()
                    }
                }
            }

            if(view === pwConfirmDet && !hasFocus) {
                if(pwConfirmDet.text.isEmpty())
                    pwConfirmDet.showErrorMsg("비밀번호 확인이 필요합니다.")
                else if(pwConfirmDet.text != pwDet.text)
                    pwConfirmDet.showErrorMsg("비밀번호가 일치하지 않습니다.")
            }

            verifyNextBtn()
        }
    }

    private fun showPwErrorMsg() {
        binding.apply {
            var score = 0
            if (pwDet.text.isContainAlphabet()) score += 1
            if (pwDet.text.isContainDigit()) score += 1
            if (pwDet.text.isContainSpecial()) score += 1

            if (score < 2) {
                pwDet.showErrorMsg(getString(R.string.text_please_input_6_between_15_eng_num_symbol))
            } else {
                pwDet.isShownError = false
            }
        }
    }

    override fun onFieldValueChanged(view: InputFieldV2) {
        binding.apply {
            if(view === phoneNumDet) {
                codeConfirmBtn.visibility = View.VISIBLE
//            confirmCompleteCl.visibility = View.INVISIBLE
                codeConfirmIv.visibility = View.INVISIBLE
                requestCodeBtn.toEnableUI()
            }

            verifyNextBtn()
        }
    }

    override fun onFieldValueChanged(view: PasswordFieldV2) {
        binding.apply {
            if (view === pwDet) {
                secureLevelLl.visibility = View.VISIBLE
                if (view.text.length < 6) {
                    levelIv.setImageResource(R.drawable.bar_red)
                    levelTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_fe7b67))
                    levelTv.text = "비밀번호 안정성 : 위험"
                } else {
                    var score = 0
                    if (view.text.isContainAlphabet()) score += 1
                    if (view.text.isContainDigit()) score += 1
                    if (view.text.isContainSpecial()) score += 1

                    when (score) {
                        3 -> {
                            levelIv.setImageResource(R.drawable.bar_green)
                            levelTv.text = "비밀번호 안정성 : 강력"
                            levelTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_70d000))
                            view.isShownError = false
                        }
                        2 -> {
                            levelIv.setImageResource(R.drawable.bar_orange)
                            levelTv.text = "비밀번호 안정성 : 보통"
                            levelTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow_ffb300))
                            view.isShownError = false
                        }
                        else -> {
                            levelIv.setImageResource(R.drawable.ic_x_red_circle)
                            levelTv.text = "비밀번호 안정성 : 위험"
                            levelTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_fe7b67))
                        }
                    }
                }
            }

            if((view === pwDet || view === pwConfirmDet) && pwConfirmDet.text.isNotEmpty()) {
                if(pwDet.text == pwConfirmDet.text) {
                    pwConfirmDet.isShownError = false
                } else {
                    pwConfirmDet.showErrorMsg("비밀번호가 일치하지 않습니다.")
                }
            }

            verifyNextBtn()
        }
    }

    private fun verifyNextBtn() {
        binding.apply {
            if(isAvailableToSecondStep()) {
                SignupActivity.signup.apply {
                    name = fullNameDet.text
                    email = emailDet.text
                    password = pwDet.text
                    cellphone = phoneNumDet.text
                }
                nextBtn.toEnableUI()
            } else {
                nextBtn.toDisableUI()
            }
        }
    }

    private fun load() {
        API_ANONYMOUS.listCountryCodes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                countryCodes = result.data
                setSpinner()
            }, { /* */ })

    }

    private fun setSpinner() {
        binding.apply {
            val items = countryCodes.map { "(+${it.code}) ${it.title}"}
            countrySpinner.set(items) {
                val country = countryCodes.get(it)
                countryCode = country.code
                countryType = country.type
            }
            countrySpinner.position = countryCodes.indexOfFirst { it.code == "82" }
        }
    }

    private fun initUI() {
        binding.apply {
            Handler(Looper.getMainLooper()).postDelayed({
                codeContainer.visibility = View.GONE
            }, 100)

            emailDet.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            pwDet.inputEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            pwConfirmDet.inputEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            phoneNumDet.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            codeDet.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            fullNameDet.editText.toKoreanKeyboard()
            fullNameDet.listener = this@SignupFragment
            emailDet.listener = this@SignupFragment
            pwDet.listener = this@SignupFragment
            allCb.listener = this@SignupFragment
            order14Cb.listener = this@SignupFragment
            serviceAgreeCb.listener = this@SignupFragment
            personalAgreeCb.listener = this@SignupFragment
            marketingAgreeCb.listener = this@SignupFragment
            phoneMessageSwitch.setOnCheckedChangeListener(this@SignupFragment)

            phoneMessageSwitch.setOnCheckedChangeListener(this@SignupFragment)

            codeDet.goneLabel()
            phoneNumDet.goneLabel()
//        confirmCompleteCl.visibility = View.INVISIBLE
            codeConfirmIv.visibility = View.INVISIBLE

            pwDet.inputEt.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                source.toString().filterNot { it.isWhitespace() }
            })
            pwConfirmDet.listener = this@SignupFragment
            pwConfirmDet.inputEt.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                source.toString().filterNot { it.isWhitespace() }
            })
            phoneNumDet.listener = this@SignupFragment

            serviceAgreeCb.binding.allDocuTextTv.movementMethod = LinkMovementMethod.getInstance()
            serviceAgreeCb.isClickable = true
            serviceAgreeCb.allDocuText = serviceAgreeCb.allDocuText
                .partialUnderline("전문 보기") {
                    IntentUtils.openWebLink(requireContext(), URL.이용약관, requireContext().packageManager)
                }
                .partialFontAndColored(Theme.extraBold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_6D6DFF), "전문 보기")

            personalAgreeCb.binding.allDocuTextTv.movementMethod = LinkMovementMethod.getInstance()
            personalAgreeCb.isClickable = true
            personalAgreeCb.allDocuText = personalAgreeCb.allDocuText
                .partialUnderline("전문 보기") {
                    IntentUtils.openWebLink(requireContext(), URL.개인정보취급방침, requireContext().packageManager)
                }
                .partialFontAndColored(Theme.extraBold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_6D6DFF), "전문 보기")

            marketingAgreeCb.binding.allDocuTextTv.movementMethod = LinkMovementMethod.getInstance()
            marketingAgreeCb.isClickable = true
            marketingAgreeCb.allDocuText = marketingAgreeCb.allDocuText
                .partialUnderline("전문 보기") {
                    IntentUtils.openWebLink(requireContext(), URL.마케팅활용동의방안, requireContext().packageManager)
                }
                .partialFontAndColored(Theme.extraBold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_6D6DFF), "전문 보기")


            buttonToLogin.paintFlags = buttonToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            buttonToLogin.setOnClickListener {
                startActivity(LoginActivity.getIntent(requireContext()))
            }

            backBtn.setOnClickListener {
                signupInterface?.goBack()
            }

            nextBtn.toDisableUI()
            nextBtn.setOnClickListener{
//            goNext()
                if(nextBtn.isEnableUI()) onNextBtnClicked()
            }

            setPhoneRequest()

            if (BuildConfig.FLAVOR == "beta") {
                greetingLabel.setOnClickListener {
                    if (emailDet.text.isValidEmail()) {
                        viewModel.sendCreateDummyUser(emailDet.text) {
                            DaebakToast.show(requireContext(), "${emailDet.text} 계정이 생성되었습니다.")
                        }
                    } else {
                        DaebakToast.show(requireContext(), "이메일이 없거나 형식이 맞지 않습니다.")
                    }
                }
            }
        }
    }

    private fun setPhoneRequest() {
        binding.apply {
            requestCodeBtn.setOnClickListener {
                if(requestCodeBtn.isEnableUI())
                    onRequestCodeBtnClicked()
            }

            codeConfirmBtn.setOnClickListener {
                if(codeConfirmBtn.isEnableUI()) onCodeConfirmBtnClicked()
            }

            requestCodeBtn.toDisableUI()
            codeConfirmBtn.toDisableUI()

            phoneNumDet.editText.doAfterTextChanged { text ->
                if(text.toString().isValidPhoneNum()) {
                    requestCodeBtn.toEnableUI()
                } else requestCodeBtn.toDisableUI()
            }

            codeDet.editText.doAfterTextChanged { text ->
                if(text?.length?:0 == 4) codeConfirmBtn.toEnableUI() else codeConfirmBtn.toDisableUI()
            }
        }
    }

    private fun goNext() {
        signupInterface?.goStudentInfo()
    }

    private fun onRequestCodeBtnClicked() {
        binding.apply {
            codeConfirmIv.hideIfNeed()
            codeDet.isShownError = false
            phoneNumDet.isShownError = false

            deinitTimer()
            val phone = phoneNumDet.text
            if(phone.isEmpty()) {
                phoneNumDet.showErrorMsg("휴대폰 번호를 입력해주세요.")
                return
            }
            if(phone.isValidPhoneNum() == false) {
                phoneNumDet.showErrorMsg("전화번호 형식을 확인해주세요.")
                return
            }
            val authRequest = AuthPhoneRequest(authType, phone, countryCode, countryType, purposeType )

            API_ANONYMOUS.getAuthCode(authRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if(result.error != null) {
                        phoneNumDet.showErrorMsg(result.error)
                    } else {
                        startCodeConfirm()
                        DaebakToast.show(
                            requireContext(),
                            "휴대폰 번호로 인증번호가 발송되었습니다. 메시지를 확인해주세요.",
                            overDialog = true
                        )
                    }

                    val imm =  requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(codeDet.editText,0)
                }, {
                    if (it is HttpException) {
                        val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                        phoneNumDet.showErrorMsg(error.message?:"인증번호 오류입니다.")
                    } else {
                        DialogUtils.showServerErr(requireContext())
                    }
                })
        }
    }

    private fun startCodeConfirm() {
        binding.apply {
            runTimer()
            codeContainer.showIfNeed()
            timerTv.showIfNeed()

            countrySpinner.isEnabled = false
            // 인증요청 버튼 비활성화
            phoneNumDet.isEnabled = false
            requestCodeBtn.toDisableUI()
            // 인증확인 버튼은 기본 비활성화
            codeConfirmBtn.visibility = View.VISIBLE
            codeConfirmBtn.toDisableUI()

            codeDet.isEnabled = true
            codeDet.editText.setText("")
            codeDet.editText.requestFocus()
            codeDet.isShownError = false
            phoneMessageSwitch.isEnabled = false
        }
    }

    private fun backToRequestCode() {
        binding.apply {
            codeContainer.visibility = View.GONE
            timerTv.visibility = View.GONE

            countrySpinner.isEnabled = true
            // 인증요청 버튼 비활성화
            phoneNumDet.isEnabled = true
            requestCodeBtn.toEnableUI()
            codeConfirmBtn.visibility = View.GONE

            codeDet.isEnabled = false
            codeDet.editText.setText("")
            codeDet.isShownError = false
            phoneMessageSwitch.isEnabled = true
        }
    }

    private fun onCodeConfirmBtnClicked() {
        binding.apply {
            val code = codeDet.text
            val phone = phoneNumDet.text

            val authCode = ConfirmCodeRequest(code, phone)
            API_ANONYMOUS.confirmAuthCode(authCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if(result.error != null) {
                        codeDet.showErrorMsg(result.error)
                    } else {
                        deinitTimer()
                        codeConfirmIv.show()
                        countrySpinner.isEnabled = false
                        codeDet.isEnabled = false
                        timerTv.visibility = View.INVISIBLE
                        codeConfirmBtn.toDisableUI()

//                    codeConfirmBtn.visibility = View.GONE
//                    confirmCompleteCl.show(200)

                        DaebakToast.show(requireContext(), "휴대폰 인증이 완료되었습니다.", overDialog = true)
                    }
                    verifyNextBtn()
                }, {
                    DialogUtils.showServerErr(requireContext())
                })
        }
    }

    private fun onNextBtnClicked() {
        binding.apply {
            if(isAvailableToSecondStep()) {
                checkEmail {
                    goNext()
                }
            } else {
                if(fullNameDet.text.isEmpty()) {
                    fullNameDet.showErrorMsg("이름을 입력해주세요.")
                } else if(fullNameDet.text.length < 2) {
                    fullNameDet.showErrorMsg("이름을 2자 이상 입력해주세요.")
                } else if(!fullNameDet.text.isValidName()) {
                    fullNameDet.showErrorMsg("올바른 형식이 아닙니다.")
                }

                if(emailDet.text.isEmpty())
                    emailDet.showErrorMsg("이메일을 입력해주세요.")
                else if(!emailDet.text.isValidEmail())
                    emailDet.showErrorMsg("이메일 형식을 확인해주세요.")
                else {
                    checkEmail()
                }

                if(pwDet.text.isEmpty()) {
                    pwDet.showErrorMsg("비밀번호를 입력해주세요.")
                } else if(!pwDet.text.isValidPW()) {
                    showPwErrorMsg()
                } else if(pwConfirmDet.text.isEmpty())
                    pwConfirmDet.showErrorMsg("비밀번호 확인이 필요합니다.")
                else if(pwConfirmDet.text != pwDet.text)
                    pwConfirmDet.showErrorMsg("비밀번호가 일치하지 않습니다.")

            }
        }
    }

    private fun checkEmail(callback:(()->Unit)?=null) {
        binding.apply {
            val email = emailDet.text.trim()
            API_V2.existId(email).enqueue(object: Callback<Template<String?>> {
                override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                    responseFailed(context!!, t)
                }

                override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                    val httpCode = response.code()
                    val statusCode = response.body()?.data?:""
                    val msg = response.body()?.message?:"이메일 형식을 확인해 주세요"
                    when(httpCode) {
                        200 -> {
                            when(statusCode) {
                                LoginActivity.LOGINID_INVALID -> {
                                    emailDet.isShownError = true
                                    emailDet.showErrorMsg(msg)
                                }
                                LoginActivity.LOGINID_EXIST -> {
                                    emailDet.isShownError = true
                                    emailDet.showErrorMsg("이미 사용중인 이메일 주소입니다.")
                                }
                                LoginActivity.ALREADY_WITHDRAW -> {
                                    emailDet.isShownError = true
                                    emailDet.showErrorMsg(msg)
                                }
                                else -> {
                                    emailDet.isShownError = false
                                    callback?.run{ this() }
                                }
                            }
                        }
                        else -> {
                            DialogUtils.showServerErr(context!!)
                        }
                    }
                    verifyNextBtn()
                }
            })
        }
    }

    override fun onPause() {
        pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        restartTimer()
        super.onResume()
    }

    // 시간 체크 및 멈춤처리
    var stoppedTime = 0L
    var stoppedRemainSec = 0

    private fun pauseTimer() {
        stoppedTime = System.currentTimeMillis()
        stoppedRemainSec = remainSec
        Log.d(javaClass.simpleName, "[Timer pause] stopped=$stoppedTime, remain=$stoppedRemainSec")
        timer?.cancel()
        timer = null
    }

    private fun restartTimer() {
        if(stoppedTime > 0) {
            val pausePeriod = (System.currentTimeMillis() - stoppedTime) / 1000 // 흐른시간
            if (stoppedRemainSec > 0) {
                remainSec = stoppedRemainSec - pausePeriod.toInt()
                Log.d(javaClass.simpleName, "[Timer restart] stopped=$stoppedTime, stoppedRemain=$stoppedRemainSec, period=$pausePeriod, setRemain=$remainSec")
                if(stoppedRemainSec > pausePeriod && timer == null) { // 남은 시간이 흐른시간보다 크면
                    runTimer()
                } else {
                    resetCodeRequest()
                }
            }
        }
    }

    // 타이머 코드
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
        stoppedRemainSec = 0
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
                    setTimerTick()
                } else {
                    resetCodeRequest()
                }
            }catch (e:Exception) {
                timer?.cancel()
                timer = null
                e.printStackTrace()
            }
        }
    }

    private fun setTimerTick() {
        remainSec -= 1
        val min = (remainSec) / 60
        val sec = remainSec % 60

        binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
    }

    private fun resetCodeRequest() {
        timer?.cancel()
        timer = null
        binding.apply {
            timerTv.text = "00:00"
            codeDet.editText.setText("")
            codeDet.showErrorMsg("시간이 만료되었습니다.")
            // 요청버튼 활성화
            requestCodeBtn.text = "재전송하기"
            backToRequestCode()
        }
    }

    override fun onCheckChangedListener(cb: CheckBox?, flag: Boolean) {
        binding.apply {
            when (cb) {
                allCb.binding.checkBox -> {
                    setAllCheckBox(!isCheckBoxAllChecked())
                }
                order14Cb.binding.checkBox -> {
                    allCb.checked = isCheckBoxAllChecked()
                }
                serviceAgreeCb.binding.checkBox -> {
                    allCb.checked = isCheckBoxAllChecked()
                }
                personalAgreeCb.binding.checkBox -> {
                    allCb.checked = isCheckBoxAllChecked()
                }
                marketingAgreeCb.binding.checkBox -> {
                    allCb.checked = isCheckBoxAllChecked()
                }
            }
            verifyNextBtn()
        }
    }

    private fun isCheckBoxAllChecked (): Boolean {
        binding.apply {
            return order14Cb.checked && serviceAgreeCb.checked && personalAgreeCb.checked && marketingAgreeCb.checked
        }
    }

    private fun setAllCheckBox(flag: Boolean) {
        binding.apply {
            order14Cb.checked = flag
            serviceAgreeCb.checked = flag
            personalAgreeCb.checked = flag
            marketingAgreeCb.checked = flag
        }
    }

    private fun isAllCheckedEssentialBox (): Boolean {
        binding.apply {
            return order14Cb.checked && serviceAgreeCb.checked && personalAgreeCb.checked
        }
    }

    var authType: String = "ALIMTALK"
    override fun onCheckedChanged(switch: CompoundButton?, flag: Boolean) {
        authType = if (flag) "SMS" else "ALIMTALK"
    }
}